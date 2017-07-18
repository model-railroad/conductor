package com.alflabs.conductor.script;

import com.alflabs.conductor.parser.Reporter;
import dagger.Module;
import dagger.Provides;

@Module
public class ScriptModule {

    private final Reporter mReporter;

    public ScriptModule(Reporter reporter) {
        mReporter = reporter;
    }

    @Provides
    public Reporter provideReporter() {
        return mReporter;
    }
}
