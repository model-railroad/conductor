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

import com.alflabs.utils.FakeFileOps;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.StringLogger;
import com.google.common.base.Charsets;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MqttClientTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock private Mqtt3BlockingClient mMockBlockingClient;

    private final FileOps mFileOps = new FakeFileOps();
    private final StringLogger mLogger = new StringLogger();
    private final CountDownLatch mStarted = new CountDownLatch(1);
    private MqttClient mMqttClient;

    private static final String configJsonString = "{\n" +
            "\"ip\": \"127.0.0.1\",\n" +
            "\"port\": 1883,\n" +
            "\"user\": \"the_user\",\n" +
            "\"password\": \"the_password\"\n" +
            "}\n";

    @Before
    public void setUp() {
        mMqttClient = spy(new MqttClient(mLogger, mFileOps) {
            @Override
            protected void _runInThreadLoop() throws EndLoopException {
                mStarted.countDown();
                super._runInThreadLoop();
            }
        });

        doReturn(mMockBlockingClient).when(mMqttClient)._createBlockingClient(any());
        doNothing().when(mMqttClient)._publishPayload(any(), any());
    }

    @After
    public void tearDown() throws Exception {
        mMqttClient.shutdown();
    }

    @Test
    public void loadConfigurationFromString() throws IOException {
        MqttClient.Configuration conf = MqttClient.Configuration.fromJsonString(configJsonString);
        assertThat(conf).isNotNull();
        assertThat(conf.mIp).isEqualTo("127.0.0.1");
        assertThat(conf.mPort).isEqualTo(1883);
        assertThat(conf.mUser).isEqualTo("the_user");
        assertThat(conf.mPassword).isEqualTo("the_password");
    }

    @Test
    public void publish_MissingConfiguration() throws Exception {
        mMqttClient.publish("some/topic", "the_message");
        mStarted.await(500, TimeUnit.MILLISECONDS);
        mMqttClient.shutdown();
        assertThat(mLogger.getString())
                .contains("MqttClient: Error MQTT.publish() called before MQTT.configure()");
        assertThat(mLogger.getString())
                .doesNotContain("MqttClient: Loading MQTT configuration from");
    }

    @Test
    public void publish_WithConfiguration() throws Exception {
        File configFile = new File("/tmp/config.json");
        mFileOps.writeBytes(configJsonString.getBytes(Charsets.UTF_8), configFile);
        mMqttClient.configure(configFile.getPath());

        mMqttClient.publish("some/topic", "the_message");

        mStarted.await(500, TimeUnit.MILLISECONDS);
        mMqttClient.shutdown();

        // Validate expected configuration was passed to client create/connect method.
        verify(mMqttClient, times(1))._createBlockingClient(
                eq(new MqttClient.Configuration("127.0.0.1", 1883, "the_user", "the_password"))
        );

        // Validate publish was called with expected payload
        verify(mMqttClient, times(1))._publishPayload(
                eq(mMockBlockingClient),
                eq(new MqttClient.Payload("some/topic", "the_message")));

        assertThat(mLogger.getString().replace(File.separatorChar, '/'))
                .contains("MqttClient: Loading MQTT configuration from /tmp/config.json");
        assertThat(mLogger.getString())
                .contains("MqttClient: Connected to MQTT Broker 127.0.0.1:1883, user the_user");
        assertThat(mLogger.getString())
                .contains("MqttClient: Published some/topic = the_message");
    }

    @Test
    public void publish_DuplicatedMessages() throws Exception {
        File configFile = new File("/tmp/config.json");
        mFileOps.writeBytes(configJsonString.getBytes(Charsets.UTF_8), configFile);
        mMqttClient.configure(configFile.getPath());

        // Verify that we don't send duplicates.
        mMqttClient.publish("some/topic", "the_message");
        mMqttClient.publish("some/topic", "the_message");       // dup should be ignored
        mMqttClient.publish("some/topic", "another_message");
        mMqttClient.publish("new/topic", "another_message");
        mMqttClient.publish("new/topic", "another_message");    // dup should be ignored
        mMqttClient.publish("some/topic", "the_message");       // this is new again

        // Let the thread start and wait for it to complete before terminating
        mStarted.await(500, TimeUnit.MILLISECONDS);
        mMqttClient.shutdown();

        // Validate publish was called with expected number of payloads,
        // and validate the order via the log.
        verify(mMqttClient, times(2))._publishPayload(
                eq(mMockBlockingClient),
                eq(new MqttClient.Payload("some/topic", "the_message")));
        verify(mMqttClient, times(1))._publishPayload(
                eq(mMockBlockingClient),
                eq(new MqttClient.Payload("some/topic", "another_message")));
        verify(mMqttClient, times(1))._publishPayload(
                eq(mMockBlockingClient),
                eq(new MqttClient.Payload("new/topic", "another_message")));

        assertThat(mLogger.getString())
                .contains(
                        "MqttClient: Published some/topic = the_message\n" +
                        "MqttClient: Published some/topic = another_message\n" +
                        "MqttClient: Published new/topic = another_message\n" +
                        "MqttClient: Published some/topic = the_message"
                );
    }

}
