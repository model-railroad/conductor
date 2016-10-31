package com.alfray.conductor.script;

public class Timer implements IConditional {

    private final int mDurationSec;
    private long mEndTS;

    public Timer(int durationSec) {
        mDurationSec = durationSec;
        mEndTS = 0;
    }

    public int getDurationSec() {
        return mDurationSec;
    }

    public IFunction.Int createFunctionStart() {
        return new IFunction.Int() {
            @Override
            public void setValue(Integer ignored) {
                mEndTS = now() + mDurationSec * 1000;
            }
        };
    }

    public IFunction.Int createFunctionEnd() {
        return new IFunction.Int() {
            @Override
            public void setValue(Integer ignored) {
                mEndTS = 0;
            }
        };
    }

    @Override
    public boolean isActive() {
        return mEndTS != 0 && now() >= mEndTS;
    }

    public long now() {
        return System.currentTimeMillis();
    }
}
