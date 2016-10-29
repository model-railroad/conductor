package com.alfray.conductor;

public class EntryPoint {
    private JmriProvider mJmriProvider;
    private ThrottleAdapter mThrottle;

    public void setup(JmriProvider jmriProvider) {
        System.out.println("Setup");
        this.mJmriProvider = jmriProvider;
        mThrottle = jmriProvider.getThrotlle(42);
        mThrottle.setSpeed(10);
        mThrottle.setSpeed(2*mThrottle.getSpeed());
    }

    public boolean handle() {
        System.out.println("Handle");
        return true;
    }

}
