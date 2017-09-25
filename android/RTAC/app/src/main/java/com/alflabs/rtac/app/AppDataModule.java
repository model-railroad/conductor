package com.alflabs.rtac.app;

import android.net.wifi.WifiManager;
import com.alflabs.annotations.NonNull;
import com.alflabs.rtac.nsd.DiscoveryListener;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rtac.service.KVClientListener;
import com.alflabs.utils.ILogger;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
@SuppressWarnings("WeakerAccess")
public class AppDataModule {

    @NonNull
    @Provides
    @Singleton
    public DataClientMixin providesDataClientMixin(
            ILogger mLogger,
            AppPrefsValues mAppPrefsValues,
            DiscoveryListener mNsdListener,
            KVClientListener mKVClientListener,
            WifiManager mWifiManager) {
        return new DataClientMixin(
                mLogger,
                mAppPrefsValues,
                mNsdListener,
                mKVClientListener,
                mWifiManager);
    }
}
