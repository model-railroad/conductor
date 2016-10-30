package com.alfray.conductor;

public class EntryPoint {
    private IJmriProvider mJmriProvider;
    private IJmriThrottle mThrottle;

    public void setup(IJmriProvider jmriProvider) {
        System.out.println("Setup");
    }

    public boolean handle() {
        System.out.println("Handle");
        return true;
    }

}
