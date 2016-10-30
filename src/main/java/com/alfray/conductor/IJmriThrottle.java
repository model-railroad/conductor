package com.alfray.conductor;

public interface IJmriThrottle {
    void setSpeed(int speed);
    void setSound(boolean on);
    void setLight(boolean on);
    void horn();
}
