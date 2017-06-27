package com.alflabs.rtac.activity;

import com.alflabs.dagger.ActivityScope;
import dagger.Subcomponent;


@ActivityScope
@Subcomponent(modules = ActivityContextModule.class)
public interface IMainActivityComponent {

    interface Factory {
        IMainActivityComponent create(ActivityContextModule activityContextModule);
    }

    void inject(MainActivity mainActivity);
}
