package com.alflabs.rtac.app;

import android.app.NotificationManager;
import android.net.wifi.WifiManager;
import com.alflabs.annotations.NonNull;
import com.alflabs.utils.ILogger;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AppMockComponent extends MainApp {
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

        return DaggerIAppComponent
                .builder()
                .appContextModule(mAppContextModule)
                .build();
    }
}
