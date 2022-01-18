package com.alflabs.conductor.v1.dagger;

import com.alflabs.conductor.dagger.CommonModule;
import com.alflabs.conductor.jmri.IJmriProvider;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = { CommonModule.class })
public interface IEngine1Component {
    IScriptComponent.Factory newScriptComponent();

    @Component.Factory
    interface Factory {
        IEngine1Component createComponent(@BindsInstance IJmriProvider jmriProvider);
    }
}
