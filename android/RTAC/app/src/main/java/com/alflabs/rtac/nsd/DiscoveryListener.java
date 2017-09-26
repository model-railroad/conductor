package com.alflabs.rtac.nsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import com.alflabs.dagger.AppQualifier;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.app.AppPrefsValues;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IStream;
import com.alflabs.rx.Publishers;
import com.alflabs.rx.Streams;
import com.alflabs.utils.Utils;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * NSD discovery listener.
 */
@Singleton
public class DiscoveryListener {

    private static final String TAG = DiscoveryListener.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String SERVICE_TYPE = "_withrottle._tcp.";

    private final Context mContext;
    private final IStream<NsdServiceInfo> mServiceResolvedStream = Streams.stream();
    private final IPublisher<NsdServiceInfo> mServiceResolvedPublisher = Publishers.publisher();

    private NsdManagerDelegate mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;

    private AppPrefsValues mAppPrefsValues;

    /**
     * Creates a new DiscoveryListener. <br/>
     * This must be done only on the UI thread because we'll use a Looper later.
     * The object can however be used later on other threads, typically any network thread.
     */
    @Inject
    public DiscoveryListener(
            @AppQualifier Context context,
            AppPrefsValues appPrefsValues) {
        mContext = context;
        mAppPrefsValues = appPrefsValues;
        mServiceResolvedStream.publishWith(mServiceResolvedPublisher);
    }

    public IStream<NsdServiceInfo> getServiceResolvedStream() {
        return mServiceResolvedStream;
    }

    public boolean start() {
        if (DEBUG) Log.d(TAG, "onResume");

        initializeResolveListener();
        initializeDiscoveryListener();
        assert mResolveListener != null;
        assert mDiscoveryListener != null;

        return startDiscovery();
    }

    public void stop() {
        if (DEBUG) Log.d(TAG, "onPause");
        tearDown();
    }

    private void initializeResolveListener() {
        if (mResolveListener != null) {
            return;
        }
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.d(TAG, "Resolve failed: " + errorCode + " " + serviceInfo);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                if (DEBUG) Log.d(TAG, "Resolve Succeeded: " + serviceInfo);
                mServiceResolvedPublisher.publish(serviceInfo);
            }
        };
    }

    private void initializeDiscoveryListener() {
        // Instantiates a new DiscoveryListener
        if (mDiscoveryListener != null) {
            return;
        }

        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                if (DEBUG) Log.d(TAG, "Service discovery started: " + regType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                if (DEBUG) Log.d(TAG, "Service discovery success: " + service);

                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                    return;
                }

                try {
                    mNsdManager.resolveService(service, mResolveListener);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "resolveService non-fatal failure: " + e.toString());
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                if (DEBUG) Log.d(TAG, "service lost: " + service);
                // no-op, we don't care about service lost here
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                if (DEBUG) Log.d(TAG, "Discovery stopped: " + serviceType);
                // no-op, we don't care about stopped here
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG, "Discovery failed: Error code: " + errorCode + " " + serviceType);
                // no-op, we don't care about failure here
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(TAG, "Discovery failed: Error code: " + errorCode + " " + serviceType);
                // no-op, we don't care about failure here
            }
        };
    }

    /**
     *  Start discovery for the service type
     *  Unit-tests can override this to inject test cases instead of the real manager.
     */
    private boolean startDiscovery() {
        if (_isDisabledInPrefs()) {
            Log.w(TAG, "DiscoveryMixin disabled in app prefs.");
            return false;
        }

        if (_abortIfEmulator()) {
            Log.w(TAG, "DiscoveryMixin skipped on emulator.");
            return false;
        }

        mNsdManager = _getNsdManager();
        assert mNsdManager != null;
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        return true;
    }

    /** For unit tests to override. */
    protected boolean _isDisabledInPrefs() {
        return !mAppPrefsValues.getSystem_EnableNsd();
    }

    /** For unit tests to override. */
    protected boolean _abortIfEmulator() {
        // Emulator in API 21 seems to hang as soon as the mDSN service is started.
        return Utils.isEmulator();
    }

    /** For unit tests to override. */
    protected NsdManagerDelegate _getNsdManager() {
        return new NsdManagerDelegate((NsdManager) mContext.getSystemService(Context.NSD_SERVICE));
    }

    private void tearDown() {
        if (mNsdManager != null) {
            if (mDiscoveryListener != null) {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
                mDiscoveryListener = null;
            }
        }
        mNsdManager = null;
    }
}
