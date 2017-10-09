package com.alflabs.rtac.fragment;

import com.alflabs.dagger.ActivityScope;
import dagger.Subcomponent;


@ActivityScope
@Subcomponent
public interface IFragmentComponent {

    interface Factory {
        IFragmentComponent create();
    }

    void inject(RoutesFragment routesFragment);
    void inject(EStopFragment eStopFragment);
    void inject(DebugFragment debugFragment);
}
