package com.alflabs.rtac.app;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = { AppContextModule.class } )
@SuppressWarnings("WeakerAccess")
public interface IAppComponent {
//--    extends IMainActivityComponent.Factory {

    void inject(MainApp mainApp);

    // Note: this line should not be needed (create() is inherited from the Component.Factory))
    // but there's a bug in Jack/ECJ, tracked at https://code.google.com/p/android/issues/detail?id=223549
//--    IMainActivityComponent create(ActivityContextModule activityContextModule);

}
