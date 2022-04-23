package com.alflabs.conductor.v2.dagger;

import com.alflabs.conductor.dagger.CommonModule;
import com.alflabs.conductor.jmri.IJmriProvider;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = { CommonModule.class })
public interface IEngine2Component {
    // IScript2Component.Factory getScriptComponentFactory();

    @Component.Factory
    interface Factory {
        IEngine2Component createComponent(@BindsInstance IJmriProvider jmriProvider);
    }
}
