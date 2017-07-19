package com.alflabs.conductor;

import com.alflabs.conductor.script.IScriptComponent;
import com.alflabs.conductor.script.ScriptModule;
import com.alflabs.conductor.util.Now;
import com.alflabs.kv.KeyValueServer;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

@Singleton
@Component(modules = {ConductorModule.class})
public interface IConductorComponent {

    Now getNow();
    KeyValueServer getKeyValueServer();
    IJmriProvider getJmriProvider();
    @Named("script") File getScriptFile();

    void inject(EntryPoint entryPoint);

    IScriptComponent newScriptComponent(ScriptModule scriptModule);

    @Component.Builder
    interface Builder {
        IConductorComponent build();
        Builder conductorModule(ConductorModule conductorModule);
        @BindsInstance Builder scriptFile(@Named("script") File scriptFile);
    }
}
