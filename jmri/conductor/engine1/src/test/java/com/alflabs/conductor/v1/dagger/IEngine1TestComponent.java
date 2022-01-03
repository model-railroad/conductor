package com.alflabs.conductor.v1.dagger;

import com.alflabs.conductor.dagger.CommonTestModule;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.v1.parser.ScriptParser2Test;
import com.alflabs.conductor.v1.parser.ScriptParserFullTest;
import com.alflabs.conductor.v1.simulator.SimulatorTest;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

@Singleton
@Component(modules = { CommonTestModule.class })
public interface IEngine1TestComponent extends IEngine1Component {

    void inject(SimulatorTest simulatorTest);
    void inject(ScriptParser2Test scriptParser2Test);
    void inject(ScriptParserFullTest scriptParserFullTest);
    void inject(IEngine1TestComponentTest iEngine1TestComponentTest);

    @Component.Factory
    interface Factory {
        IEngine1TestComponent createTestComponent(
                @BindsInstance IJmriProvider jmriProvider,
                @BindsInstance @Named("script") File scriptFile);
    }
}
