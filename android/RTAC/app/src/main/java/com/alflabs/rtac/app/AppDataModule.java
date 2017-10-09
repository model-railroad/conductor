package com.alflabs.rtac.app;

import android.net.wifi.WifiManager;
import com.alflabs.annotations.NonNull;
import com.alflabs.rtac.nsd.DiscoveryListener;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rtac.service.KVClientStatsListener;
import com.alflabs.utils.AndroidClock;
import com.alflabs.utils.IClock;
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
            IClock clock,
            ILogger logger,
            AppPrefsValues appPrefsValues,
            DiscoveryListener nsdListener,
            KVClientStatsListener kvClientListener,
            WifiManager wifiManager) {
        return new DataClientMixin(
                clock,
                logger,
                appPrefsValues,
                nsdListener,
                kvClientListener,
                wifiManager);
    }

    @NonNull
    @Provides
    @Singleton
    public IClock providesClock() {
        return new AndroidClock();
    }
}
