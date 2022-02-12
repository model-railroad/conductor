package com.alflabs.conductor.v2.script

class Block extends BaseActive {
    private final String mSystemName

    Block(String systemName) {
        this.mSystemName = systemName
    }

    String getSystemName() {
        return mSystemName
    }
}
