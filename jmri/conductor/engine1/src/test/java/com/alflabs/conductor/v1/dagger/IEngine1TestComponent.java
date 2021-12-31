package com.alflabs.conductor.v1.dagger;

import com.alflabs.conductor.dagger.ICommonTestComponent;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

@Singleton
@Component(dependencies = { ICommonTestComponent.class })
public interface IEngine1TestComponent {

    IScriptComponent.Factory newScriptComponent();

    @Component.Factory
    interface Factory {
        IEngine1TestComponent createComponent(
                ICommonTestComponent commonComponent,
                @BindsInstance @Named("script") File scriptFile);
    }
}
