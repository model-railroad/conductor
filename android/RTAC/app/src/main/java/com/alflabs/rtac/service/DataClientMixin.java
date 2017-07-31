package com.alflabs.rtac.service;

import com.alflabs.utils.ServiceMixin;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataClientMixin extends ServiceMixin<RtacService> {

    @Inject
    public DataClientMixin() {}

    @Override
    public void onCreate(RtacService service) {
        super.onCreate(service);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
