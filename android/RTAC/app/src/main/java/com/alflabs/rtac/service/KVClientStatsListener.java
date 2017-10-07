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
