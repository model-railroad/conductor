package com.alflabs.conductor.v2.script

class Throttle extends BaseVar {
    private final int mDccAddress
    private boolean mIsStarted
    private int mSpeed = 0

    Throttle(int dccAddress) {
        this.mDccAddress = dccAddress
    }

    int getDccAddress() {
        return mDccAddress
    }

    int getSpeed() {
        return mSpeed
    }

    // forward as a function: { Throttle forward(speed) }
    void forward(int speed) {
        mSpeed = speed
    }

    // forward as a setter property: { Throttle.forward = speed }
    void setForward(int speed) {
        forward(speed)
    }

    // forward as a getter property: { Throttle.forward }
    boolean isForward() {
        return mSpeed > 0
    }

    void reverse(int speed) {
        mSpeed = -speed
    }

    void setReverse(int speed) {
        reverse(speed)
    }

    boolean isReverse() {
        return mSpeed < 0
    }

    void stop() {
        mSpeed = 0
    }

    boolean isStopped() {
        return mSpeed == 0
    }

}
