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
import com.alflabs.utils.ILogger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MqttClientLoop extends ThreadLoop {
    private static final String TAG = Analytics.class.getSimpleName();

    private final ILogger mLogger;
    private final ConcurrentLinkedDeque<Payload> mPayloads = new ConcurrentLinkedDeque<>();
    private final Map<String, String> mPublishCache = new HashMap<>();

    @Inject
    public MqttClientLoop(ILogger logger) {
        mLogger = logger;
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
        super.start("MqttClientLoop");
    }

    @Override
    public void stop() throws Exception {
        mLogger.d(TAG, "Stop");
        super.stop();
    }

    @Override
    protected void _runInThreadLoop() throws EndLoopException {

    }

    @Override
    protected void _afterThreadLoop() {
        mLogger.d(TAG, "End Loop");
    }

    public void publish(@NonNull String topic, @NonNull String value) {
        String publishedValue = mPublishCache.get(topic);
        if (publishedValue != null && publishedValue.equals(value)) {
            // The value has already been published. Don't duplicate it.
            return;
        }

        try {
            start();
        } catch (Exception e) {
            mLogger.d(TAG, "Publish Ignored -- Failed to start MQTT Client thread: " + e);
            return;
        }

        mPayloads.offerFirst(new Payload(topic, value));
        mPublishCache.put(topic, value);
    }

    private static class Payload {
        public final String mTopic;
        public final String mValue;

        public Payload(@NonNull String topic, @NonNull String value) {
            mTopic = topic;
            mValue = value;
        }
    }
}

