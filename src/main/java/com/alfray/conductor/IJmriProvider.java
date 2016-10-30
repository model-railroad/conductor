package com.alfray.conductor;

public interface IJmriProvider {
    IJmriThrottle getThrotlle(int dccAddress);
    IJmriSensor getSensor(String systemName);
}
