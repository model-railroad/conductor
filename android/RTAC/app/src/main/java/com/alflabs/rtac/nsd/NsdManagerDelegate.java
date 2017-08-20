package com.alflabs.rtac.nsd;

import android.annotation.TargetApi;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

/**
 * Delegate to NSD Manager.
 * The NsdManager class is final and thus cannot be overridden for unit tests.
 * This class can be mocked in unit-tests instead.
 */
@TargetApi(16)
public class NsdManagerDelegate {
    private final NsdManager mRealNsdManager;

    public NsdManagerDelegate() {
        mRealNsdManager = null;
    }

    public NsdManagerDelegate(NsdManager realNsdManager) {
        mRealNsdManager = realNsdManager;
    }

    public void resolveService(NsdServiceInfo service, NsdManager.ResolveListener resolveListener) {
        mRealNsdManager.resolveService(service, resolveListener);
    }

    public void discoverServices(String serviceType, int protocolDnsSd, NsdManager.DiscoveryListener discoveryListener) {
        mRealNsdManager.discoverServices(serviceType, protocolDnsSd, discoveryListener);
    }

    public void stopServiceDiscovery(NsdManager.DiscoveryListener discoveryListener) {
        mRealNsdManager.stopServiceDiscovery(discoveryListener);
    }

    public void registerService(NsdServiceInfo serviceInfo, int protocolDnsSd, NsdManager.RegistrationListener registrationListener) {
        mRealNsdManager.registerService(serviceInfo, protocolDnsSd, registrationListener);
    }

    public void unregisterService(NsdManager.RegistrationListener registrationListener) {
        mRealNsdManager.unregisterService(registrationListener);
    }
}
