package com.alflabs.conductor.v1.dagger;

import com.alflabs.conductor.dagger.ICommonComponent;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

@Singleton
@Component(dependencies = { ICommonComponent.class })
public interface IEngine1Component {

    IScriptComponent.Factory newScriptComponent();

    @Component.Factory
    interface Factory {
        IEngine1Component createComponent(
                ICommonComponent commonComponent,
                @BindsInstance @Named("script") File scriptFile);
    }
}
