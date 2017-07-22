package com.alflabs.conductor.script;

import com.alflabs.conductor.parser.Reporter;
import com.alflabs.kv.IKeyValue;
import dagger.Module;
import dagger.Provides;

@Module
public class ScriptModule {

    private final Reporter mReporter;
    private final IKeyValue mKeyValue;

    public ScriptModule(Reporter reporter, IKeyValue keyValue) {
        mReporter = reporter;
        mKeyValue = keyValue;
    }

    @Provides
    public Reporter provideReporter() {
        return mReporter;
    }

    @Provides
    public IKeyValue provideKeyValue() {
        return mKeyValue;
    }
}
