package com.alflabs.rtac.fragment;

import com.alflabs.dagger.ActivityScope;
import dagger.Subcomponent;


@ActivityScope
@Subcomponent
public interface IAutomationFragmentComponent {

    interface Factory {
        IAutomationFragmentComponent create();
    }

    void inject(AutomationFragment automationFragment);
}
