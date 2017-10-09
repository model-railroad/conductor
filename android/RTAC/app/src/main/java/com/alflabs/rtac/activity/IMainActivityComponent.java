package com.alflabs.rtac.activity;

import com.alflabs.dagger.ActivityScope;
import com.alflabs.rtac.fragment.IFragmentComponent;
import dagger.Subcomponent;


@ActivityScope
@Subcomponent(modules = ActivityContextModule.class)
public interface IMainActivityComponent
    extends IFragmentComponent.Factory {

    interface Factory {
        IMainActivityComponent create(ActivityContextModule activityContextModule);
    }

    void inject(MainActivity mainActivity);
}
