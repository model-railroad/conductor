package com.alflabs.rtac.fragment;

import com.alflabs.dagger.ActivityScope;
import dagger.Subcomponent;


@ActivityScope
@Subcomponent
public interface IRoutesFragmentComponent {

    interface Factory {
        IRoutesFragmentComponent create();
    }

    void inject(RoutesFragment routesFragment);
}
