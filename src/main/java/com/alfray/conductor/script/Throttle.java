package com.alfray.conductor.script;

import com.alfray.conductor.IJmriProvider;
import com.alfray.conductor.IJmriThrottle;

public class Throttle {
    private IJmriThrottle mJmriThrottle;
    private int mSpeed;
    private boolean mSound;
    private boolean mLight;

    public Throttle() {}

    public void init(IJmriProvider provider, int dccAddress) {
        mJmriThrottle = provider.getThrotlle(dccAddress);
    }

    private void setSpeed(int speed) {
        mSpeed = speed;
        if (mJmriThrottle != null) {
            mJmriThrottle.setSpeed(speed);
        }
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
                if (mJmriThrottle != null) {
                    mJmriThrottle.setSound(mSound);
                }
            }
        };
    }

    public IFunction.Int createFunctionLight() {
        return new IFunction.Int() {
            @Override
            public void setValue(Integer on) {
                mLight = on != 0;
                if (mJmriThrottle != null) {
                    mJmriThrottle.setLight(mLight);
                }
            }
        };
    }

    public IFunction.Int createFunctionHorn() {
        return new IFunction.Int() {
            @Override
            public void setValue(Integer on) {
                if (mJmriThrottle != null) {
                    mJmriThrottle.horn();
                }
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
