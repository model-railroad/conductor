package com.alflabs.conductor.v2.script

class Sensor extends  BaseActive {
    private final String mSystemName

    Sensor(String systemName) {
        this.mSystemName = systemName
    }

    String getSystemName() {
        return mSystemName
    }
}
