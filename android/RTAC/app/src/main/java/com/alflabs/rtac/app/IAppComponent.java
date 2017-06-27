package com.alflabs.rtac.app;

import com.alflabs.rtac.activity.IMainActivityComponent;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = { AppContextModule.class } )
@SuppressWarnings("WeakerAccess")
public interface IAppComponent
    extends IMainActivityComponent.Factory {

    void inject(MainApp mainApp);

    // Note: this line should not be needed (create() is inherited from the Component.Factory))
    // but there's a bug in Jack/ECJ, tracked at https://code.google.com/p/android/issues/detail?id=223549
    // This is only needed when the new (now already obsoleted) Jack compiler is used for Java 8 support.
    // -- IMainActivityComponent create(ActivityContextModule activityContextModule);
}
