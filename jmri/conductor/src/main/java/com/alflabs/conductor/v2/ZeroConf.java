/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
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

package com.alflabs.conductor.v2;

import com.alflabs.conductor.util.LogException;
import com.alflabs.manifest.Constants;
import com.alflabs.utils.ILogger;

import javax.jmdns.JmDNS;
import javax.jmdns.NetworkTopologyDiscovery;
import javax.jmdns.ServiceInfo;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ZeroConf {
    private static final String TAG = ZeroConf.class.getSimpleName();

    private final List<JmDNS> mJmDnsList = new ArrayList<>();
    private CountDownLatch mJmDNSLatch;
    private ILogger mLogger;

    public void start(ILogger logger, File scriptFile) {
        mLogger = logger;
        new Thread(() -> startZeroconfAdvertising(scriptFile.getName())).start();
    }

    public void stop() {
        if (mJmDNSLatch != null) {
            try {
                mLogger.d(TAG, "Waiting for ZeroConf");
                mJmDNSLatch.await(1, TimeUnit.MINUTES);
            } catch (InterruptedException ignore) {}
        }

        for (JmDNS jmDns : mJmDnsList) {
            try {
                mLogger.d(TAG, "Teardown ZeroConf on " + jmDns.getInetAddress());
            } catch (IOException ignore) {}
            jmDns.unregisterAllServices();
            try {
                jmDns.close();
            } catch (IOException e) {
                mLogger.d(TAG, "Teardown ZeroConf exception: " + e);
            }
        }
    }

    private void startZeroconfAdvertising(String name) {
        mJmDNSLatch = new CountDownLatch(1);
        try {
            mLogger.d(TAG, "Starting ZeroConf");

            Map<String, String> props = new TreeMap<>();
            props.put("origin", "conductor");
            props.put("version", "1");
            props.put("script", name);
            final int weight = 0;
            final int priority = 0;

            boolean gotIpv4 = false;
            final NetworkTopologyDiscovery topology = NetworkTopologyDiscovery.Factory.getInstance();
            for (InetAddress address : topology.getInetAddresses()) {
                if (!gotIpv4 && address instanceof Inet4Address) {
                    gotIpv4 = true;
                }
                if (gotIpv4 && address instanceof Inet6Address) {
                    mLogger.d(TAG, "Skip ZeroConf on " + address + " (already got an IPv4 before).");
                    continue;
                }
                ServiceInfo info = ServiceInfo.create(
                        Constants.KV_SERVER_SERVICE_TYPE,
                        name.replace(".", "-"),
                        Constants.KV_SERVER_PORT,
                        weight,
                        priority,
                        props);

                JmDNS jmDns = JmDNS.create(address);
                jmDns.registerService(info);
                mLogger.d(TAG, "Started ZeroConf on " + jmDns.getInetAddress());
                mJmDnsList.add(jmDns);
            }
        } catch (IOException e) {
            // Ignore. continue.
            mLogger.d(TAG, "ZeroConf not enabled: ");
            LogException.logException(mLogger, TAG, e);
        } finally {
            mJmDNSLatch.countDown();
        }
    }
}
