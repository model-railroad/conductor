/*
 * Project: Conductor
 * Copyright (C) 2024 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.conductor.util;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.ILogger;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.MqttClientStateException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MqttClient extends ThreadLoop {
    private static final String TAG = Analytics.class.getSimpleName();

    private static final long IDLE_SLEEP_MS = 1000 / 10;
    private static final int NUM_CONNECT_RETRIES = 3;

    private final ILogger mLogger;
    private final FileOps mFileOps;
    private final ConcurrentLinkedDeque<Payload> mPayloads = new ConcurrentLinkedDeque<>();
    private final Map<String, String> mPublishCache = new HashMap<>();

    private Configuration mConfiguration;
    private Mqtt5BlockingClient mMqtt5Client;
    private int mMqttConnectRetries = NUM_CONNECT_RETRIES;

    @Inject
    public MqttClient(
            ILogger logger,
            FileOps fileOps) {
        mLogger = logger;
        mFileOps = fileOps;
    }

    /**
     * Requests termination. Pending tasks will be executed, no new task is allowed.
     * Waiting time is 10 seconds max.
     * <p/>
     * Side effect: The executor is now a dagger singleton. This affects other classes that
     * use the same executor, e.g. {@link JsonSender}.
     */
    public void shutdown() throws Exception {
        stop();
    }

    @Override
    public void start() throws Exception {
        super.start("MqttClient");
    }

    @Override
    public void stop() throws Exception {
        mLogger.d(TAG, "Stop");
        super.stop();
    }

    public void configure(@NonNull String jsonConfigFile) {
        try {
            File file = new File(jsonConfigFile);
            if (jsonConfigFile.startsWith("~") && !mFileOps.isFile(file)) {
                file = new File(System.getProperty("user.home"), jsonConfigFile.substring(1));
            }
            String content = mFileOps.toString(file, Charsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            mConfiguration = mapper.readValue(content, Configuration.class);

        } catch (Exception e) {
            mLogger.d(TAG, "Error MQTT configure failed: ", e);
        }

        // Reset the connect retry count.
        mMqttConnectRetries = NUM_CONNECT_RETRIES;
    }

    public void publish(@NonNull String topic, @NonNull String message) {
        String publishedValue = mPublishCache.get(topic);
        if (publishedValue != null && publishedValue.equals(message)) {
            // The message has already been published. Don't duplicate it.
            return;
        }

        try {
            start();
        } catch (Exception e) {
            mLogger.d(TAG, "Publish Ignored -- Failed to start MQTT Client thread: " + e);
            return;
        }

        mPayloads.offerLast(new Payload(topic, message));
        mPublishCache.put(topic, message);
    }

    @Override
    protected void _runInThreadLoop() throws EndLoopException {
        if (!mPayloads.isEmpty()) {
            if (mMqtt5Client == null) {
                // Start the client (this handles retries and errors).
                mMqtt5Client = createClient();
            }

            if (mMqtt5Client != null) {
                Payload payload = mPayloads.pollFirst();
                if (payload != null) {
                    try {
                        mMqtt5Client
                                .publishWith()
                                .topic(payload.mTopic)
                                .payload(payload.mMessage.getBytes(Charsets.UTF_8))
                                .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.UTF_8)
                                .contentType("text/plain")
                                .qos(MqttQos.AT_LEAST_ONCE)
                                .retain(true)
                                .send();
                        // This loop iteration succeeded.
                        mLogger.d(TAG, "Published " + payload.mTopic + " = " + payload.mMessage);
                        return;
                    } catch (MqttClientStateException e) {
                        mLogger.d(TAG, "Error MQTT publish failed: ", e);
                        // Remove the MQTT client and try again with the same payload.
                        mPayloads.offerFirst(payload);
                        mMqtt5Client.disconnectWith().send();
                        mMqtt5Client = null;
                    } catch (Exception e) {
                        mLogger.d(TAG, "Error MQTT publish failed: ", e);
                        // Try again with the same payload.
                        mPayloads.offerFirst(payload);
                    }
                }
            }
        }

        try {
            if (!mQuit) {
                Thread.sleep(IDLE_SLEEP_MS);
            }
        } catch (Exception e) {
            mLogger.d(TAG, "Stats idle loop interrupted: " + e);
        }
    }

    @Null
    @VisibleForTesting
    Mqtt5BlockingClient createClient() {
        if (mMqttConnectRetries <= 0) {
            // We have exhausted the retry count.
            return null;
        }

        if (mConfiguration == null) {
            mLogger.d(TAG, "Error: MQTT.publish() called before MQTT.configure().");
            mMqttConnectRetries = 0;
            return null;
        }

        try {
            Mqtt5BlockingClient mqtt5Client = Mqtt5Client
                    .builder()
                    .identifier("conductor2")
                    .serverHost(mConfiguration.mIp)
                    .serverPort(mConfiguration.mPort)
                    .buildBlocking();
            mqtt5Client
                    .connectWith()
                    .cleanStart(true)
                    .simpleAuth()
                    .username(mConfiguration.mUser)
                    .password(mConfiguration.mPassword.getBytes(Charsets.UTF_8))
                    .applySimpleAuth()
                    .send();

            // On success, we reset the retry count in case we need to try to reconnect.
            mMqttConnectRetries = NUM_CONNECT_RETRIES;

            return mqtt5Client;
        } catch (Exception e) {
            mLogger.d(TAG, "Error MQTT connect failed: ", e);
            mMqttConnectRetries--;
            return null;
        }
    }

    @Override
    protected void _afterThreadLoop() {
        mLogger.d(TAG, "End Loop");
    }

    private static class Payload {
        public final String mTopic;
        public final String mMessage;

        public Payload(@NonNull String topic, @NonNull String message) {
            mTopic = topic;
            mMessage = message;
        }
    }

    /** A configuration imported from a JSON config file. */
    private static class Configuration {
        @JsonProperty("ip")
        public final String mIp;
        @JsonProperty("port")
        public final int mPort;
        @JsonProperty("user")
        public final String mUser;
        @JsonProperty("password")
        public final String mPassword;

        @JsonCreator
        public Configuration(
                @NonNull String ip,
                int port,
                @NonNull String user,
                @NonNull String password) {
            mIp = ip;
            mPort = port;
            mUser = user;
            mPassword = password;
        }

        public static Configuration fromJsonString(@NonNull String content) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, Configuration.class);
        }
    }
}

