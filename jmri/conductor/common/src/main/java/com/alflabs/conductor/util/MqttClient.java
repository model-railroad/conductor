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
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MqttClient extends ThreadLoop {
    private static final String TAG = MqttClient.class.getSimpleName();

    private static final long IDLE_SLEEP_MS = 1000 / 10;
    private static final int NUM_CONNECT_RETRIES = 3;

    private final ILogger mLogger;
    private final FileOps mFileOps;
    private final ConcurrentLinkedDeque<Payload> mPayloads = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean mCleanShutdownRequested = new AtomicBoolean(false);
    private final CountDownLatch mLatchEndLoop = new CountDownLatch(1);
    private final Map<String, String> mPublishCache = new HashMap<>();

    private Configuration mConfiguration;
    private Mqtt3BlockingClient mBlockingClient;
    private int mMqttConnectRetries = NUM_CONNECT_RETRIES;

    @Inject
    public MqttClient(
            ILogger logger,
            FileOps fileOps) {
        mLogger = logger;
        mFileOps = fileOps;
    }

    /**
     * Performs a clean shutdown, trying to wait up to 10 seconds. The wait ends when the
     * message queue is empty or at the first publish error.
     */
    public void shutdown() throws Exception {
        if (mThread != null) {
            mCleanShutdownRequested.set(true);
            mLatchEndLoop.await(10, TimeUnit.SECONDS);
        }
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
        mLogger.d(TAG, "Stopped");
    }

    @Override
    protected void _afterThreadLoop() {
        mLogger.d(TAG, "End Loop");
        mLatchEndLoop.countDown();
    }

    public void configure(@NonNull String jsonConfigFile) {
        try {
            File file = new File(jsonConfigFile);
            if (jsonConfigFile.startsWith("~") && !mFileOps.isFile(file)) {
                file = new File(System.getProperty("user.home"), jsonConfigFile.substring(1));
            }
            mLogger.d(TAG, "Loading MQTT configuration from " + file);
            String content = mFileOps.toString(file, Charsets.UTF_8);

            mConfiguration = Configuration.fromJsonString(content);

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
        if (mPayloads.isEmpty()) {
            if (mCleanShutdownRequested.get()) {
                throw new EndLoopException();
            }
        } else {
            if (mBlockingClient == null) {
                // Start the client (this handles retries and errors).
                mBlockingClient = createClient();
            }

            if (mBlockingClient != null) {
                Payload payload = mPayloads.pollFirst();
                if (payload != null) {
                    try {
                        _publishPayload(mBlockingClient, payload);
                        // This loop iteration succeeded.
                        mLogger.d(TAG, "Published " + payload.mTopic + " = " + payload.mMessage);
                        return;
                    } catch (MqttClientStateException e) {
                        mLogger.d(TAG, "Error MQTT publish failed: ", e);
                        // Remove the MQTT client and try again with the same payload.
                        mPayloads.offerFirst(payload);
                        mBlockingClient.disconnect();               // MQTT 3
                        // mBlockingClient.disconnectWith().send(); -- MQTT 5
                        mBlockingClient = null;
                    } catch (Exception e) {
                        mLogger.d(TAG, "Error MQTT publish failed: ", e);
                        // Try again with the same payload.
                        mPayloads.offerFirst(payload);
                    }
                }
            }

            // Any error flushes the message queue in case of a publish exception.
            if (mCleanShutdownRequested.get()) {
                throw new EndLoopException();
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
    private Mqtt3BlockingClient createClient() {
        if (mMqttConnectRetries <= 0) {
            // We have exhausted the retry count.
            return null;
        }

        if (mConfiguration == null) {
            mLogger.d(TAG, "Error MQTT.publish() called before MQTT.configure().");
            mMqttConnectRetries = 0;
            return null;
        }

        try {
            Mqtt3BlockingClient blockingClient = _createBlockingClient(mConfiguration);
            mLogger.d(TAG, "Connected to MQTT Broker "
                    + mConfiguration.mIp + ":" + mConfiguration.mPort
                    + ", user " + mConfiguration.mUser);

            // On success, we reset the retry count in case we need to try to reconnect.
            mMqttConnectRetries = NUM_CONNECT_RETRIES;

            return blockingClient;
        } catch (Exception e) {
            mLogger.d(TAG, "Error MQTT connect failed: ", e);
            mMqttConnectRetries--;
            return null;
        }
    }

    /**
     * Builds a new Mqtt5BlockingClient.
     * This is designed to be overriden by unit tests to use a mock.
     * All error and exception handling is done by the caller.
     */
    @NonNull
    @VisibleForTesting
    protected Mqtt3BlockingClient _createBlockingClient(@NonNull Configuration configuration) {
        Mqtt3BlockingClient blockingClient = Mqtt3Client
                .builder()
                .identifier("conductor2")
                .serverHost(configuration.mIp)
                .serverPort(configuration.mPort)
                .buildBlocking();
        blockingClient
                .connectWith()
                .cleanSession(true)
                .simpleAuth()
                .username(configuration.mUser)
                .password(configuration.mPassword.getBytes(Charsets.UTF_8))
                .applySimpleAuth()
                .send();
        return blockingClient;
    }

    @VisibleForTesting
    protected void _publishPayload(@NonNull Mqtt3BlockingClient blockingClient, @NonNull Payload payload) {
        blockingClient
                .publishWith()
                .topic(payload.mTopic)
                .payload(payload.mMessage.getBytes(Charsets.UTF_8))
                .qos(MqttQos.AT_LEAST_ONCE)
                .send();
    }

    protected static class Payload {
        public final String mTopic;
        public final String mMessage;

        public Payload(@NonNull String topic, @NonNull String message) {
            mTopic = topic;
            mMessage = message;
        }

        @Override
        public String toString() {
            return "Payload{" +
                    "mTopic='" + mTopic + '\'' +
                    ", mMessage='" + mMessage + '\'' +
                    '}';
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Payload)) return false;

            Payload payload = (Payload) o;
            return Objects.equals(mTopic, payload.mTopic) && Objects.equals(mMessage, payload.mMessage);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(mTopic);
            result = 31 * result + Objects.hashCode(mMessage);
            return result;
        }
    }

    /** A configuration imported from a JSON config file. */
    protected static class Configuration {
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
                @JsonProperty("ip") @NonNull String ip,
                @JsonProperty("port") int port,
                @JsonProperty("user") @NonNull String user,
                @JsonProperty("password") @NonNull String password) {
            mIp = ip;
            mPort = port;
            mUser = user;
            mPassword = password;
        }

        public static Configuration fromJsonString(@NonNull String content) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, Configuration.class);
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "mIp='" + mIp + '\'' +
                    ", mPort=" + mPort +
                    ", mUser='" + mUser + '\'' +
                    ", mPassword='" + mPassword + '\'' +
                    '}';
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Configuration)) return false;

            Configuration that = (Configuration) o;
            return mPort == that.mPort && Objects.equals(mIp, that.mIp) && Objects.equals(mUser, that.mUser) && Objects.equals(mPassword, that.mPassword);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(mIp);
            result = 31 * result + mPort;
            result = 31 * result + Objects.hashCode(mUser);
            result = 31 * result + Objects.hashCode(mPassword);
            return result;
        }
    }
}

