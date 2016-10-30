package com.alfray.conductor.script;

import com.alfray.conductor.IJmriThrottle;

public class Throttle {
    private final IJmriThrottle mJmriThrottle;
    private int mSpeed;
    private boolean mSound;
    private boolean mLight;

    public Throttle(IJmriThrottle jmriThrottle) {
        mJmriThrottle = jmriThrottle;
    }

    private void setSpeed(int speed) {
        mSpeed = speed;
        mJmriThrottle.setSpeed(speed);
    }

    public IFunction.Int createFunctionStop() {
        return new IFunction.Int() {
            @Override
            public void setValue(Integer speed) {
                setSpeed(0);
            }
        };
    }

    public IFunction.Int createFunctionForward() {
        return new IFunction.Int() {
            @Override
            public void setValue(Integer speed) {
                setSpeed(Math.max(0, speed));
            }
        };
    }

    public IFunction.Int createFunctionReverse() {
        return new IFunction.Int() {
            @Override
            public void setValue(Integer speed) {
                setSpeed(-1 * Math.max(0, speed));
            }
        };
    }

    public IFunction.Int createFunctionSound() {
        return new IFunction.Int() {
            @Override
            public void setValue(Integer on) {
                mSound = on != 0;
                mJmriThrottle.setSound(mSound);
            }
        };
    }

    public IFunction.Int createFunctionLight() {
        return new IFunction.Int() {
            @Override
            public void setValue(Integer on) {
                mLight = on != 0;
                mJmriThrottle.setLight(mLight);
            }
        };
    }

    public IFunction.Int createFunctionHorn() {
        return new IFunction.Int() {
            @Override
            public void setValue(Integer on) {
                mJmriThrottle.horn();
            }
        };
    }

    public IConditional createIsStopped() {
        return new IConditional() {
            @Override
            public boolean isActive() {
                return mSpeed == 0;
            }
        };
    }

    public IConditional createIsForward() {
        return new IConditional() {
            @Override
            public boolean isActive() {
                return mSpeed > 0;
            }
        };
    }

    public IConditional createIsReverse() {
        return new IConditional() {
            @Override
            public boolean isActive() {
                return mSpeed < 0;
            }
        };
    }

    public IConditional createIsSound() {
        return new IConditional() {
            @Override
            public boolean isActive() {
                return mSound;
            }
        };
    }

    public IConditional createIsLight() {
        return new IConditional() {
            @Override
            public boolean isActive() {
                return mLight;
            }
        };
    }

}
