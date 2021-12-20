/*
 * Project: RTAC
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
