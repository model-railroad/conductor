package com.alfray.conductor;

public interface JmriProvider {
    ThrottleAdapter getThrotlle(int dccAddress);
}
