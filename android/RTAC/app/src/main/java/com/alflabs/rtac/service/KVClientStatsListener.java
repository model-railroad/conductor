/*
 * Project: RTAC
 * Copyright (C) 2017 alf.labs gmail com,
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

package com.alflabs.rtac.service;

import com.alflabs.annotations.Null;
import com.alflabs.kv.KeyValueClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class KVClientStatsListener implements KeyValueClient.IStatsListener {
    private int mBandwidthTX;
    private int mBandwidthRX;

    @Inject
    public KVClientStatsListener() {
    }

    @Override
    public void addBandwidthTXBytes(int count) {
        mBandwidthTX += count;

    }

    @Override
    public void addBandwidthRXBytes(int count) {
        mBandwidthRX += count;
    }

    @Override
    public void setMessage(@Null String msg) {
        // TODO report status
    }

    @Override
    public void HBLatencyRequestSent() {
        // TODO
    }

    @Override
    public void HBLatencyReplyReceived() {
        // TODO
    }
}
