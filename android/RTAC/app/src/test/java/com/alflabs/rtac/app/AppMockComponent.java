package com.alflabs.rtac.app;

import android.app.NotificationManager;
import android.net.wifi.WifiManager;
import com.alflabs.annotations.NonNull;
import com.alflabs.rtac.nsd.DiscoveryListener;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rtac.service.KVClientStatsListener;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.MockClock;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppMockComponent extends MainApp {
    private IClock mClock = new MockClock();
    @Mock ILogger mLogger;
    @Mock WifiManager.WifiLock mWifiLock;
    @Mock WifiManager mWifiManager;
    @Mock NotificationManager mNotificationManager;

    private AppContextModule mAppContextModule;

    public AppMockComponent() {
        MockitoAnnotations.initMocks(this);
        when(mWifiManager.createWifiLock(anyInt(), anyString())).thenReturn(mWifiLock);
    }

    public AppContextModule getAppContextModule() {
        return mAppContextModule;
    }

    @NonNull
    @Override
    protected IAppComponent createDaggerAppComponent() {
        IAppComponent component = getAppComponent();
        if (component != null) {
            return component;
        }

        mAppContextModule = new AppContextModule(getApplicationContext()) {
            @NonNull
            @Override
            public ILogger providesLogger() {
                return mLogger;
            }

            @NonNull
            @Override
            public WifiManager providesWifiManager() {
                return mWifiManager;
            }

            @NonNull
            @Override
            public NotificationManager providesNotificationManager() {
                return mNotificationManager;
            }
        };

        DataClientMixin dataClientMixin = mock(DataClientMixin.class);

        AppDataModule appDataModule = new AppDataModule() {
            @NonNull
            @Override
            public DataClientMixin providesDataClientMixin(
                    IClock clock,
                    ILogger logger,
                    AppPrefsValues appPrefsValues,
                    DiscoveryListener nsdListener,
                    KVClientStatsListener kvClientListener,
                    WifiManager wifiManager) {
                return dataClientMixin;
            }

            @NonNull
            @Override
            public IClock providesClock() {
                return mClock;
            }
        };

        return DaggerIAppComponent
                .builder()
                .appContextModule(mAppContextModule)
                .appDataModule(appDataModule)
                .build();
    }
}
