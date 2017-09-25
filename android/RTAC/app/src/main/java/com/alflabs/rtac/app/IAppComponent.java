package com.alflabs.rtac.app;

import com.alflabs.rtac.activity.IMainActivityComponent;
import com.alflabs.rtac.service.RtacService;
import com.alflabs.rtac.service.ServiceModule;
import com.alflabs.rtac.service.WifiLockMixin;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = { AppContextModule.class, AppDataModule.class, ServiceModule.class} )
@SuppressWarnings("WeakerAccess")
public interface IAppComponent
    extends IMainActivityComponent.Factory {

    // Note: this line should not be needed (create() is inherited from the Component.Factory))
    // but there's a bug in Jack/ECJ, tracked at https://code.google.com/p/android/issues/detail?id=223549
    // This is only needed in AS2 when the new (now already obsoleted) Jack compiler is used for Java 8 support.
    //--IMainActivityComponent create(ActivityContextModule activityContextModule);

    AppPrefsValues getAppPrefsValues();
    WifiLockMixin getWifiLockMixin();

    void inject(MainApp mainApp);
    void inject(BootReceiver bootReceiver);
    void inject(RtacService rtacService);
}
