package com.alfray.conductor;

public interface ThrottleAdapter {
    void setDccAddress(int dccAddress);
    void setSpeed(int speed);
    int getSpeed();
}
