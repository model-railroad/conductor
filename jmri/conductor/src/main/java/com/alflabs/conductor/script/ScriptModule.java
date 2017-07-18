package com.alflabs.conductor.script;

import com.alflabs.conductor.parser.Reporter;
import com.alflabs.conductor.parser.ScriptParser2;
import com.alflabs.conductor.util.Now;
import dagger.Module;
import dagger.Provides;

@Module
public class ScriptModule {

    private final Reporter mReporter;

    public ScriptModule(Reporter reporter) {
        mReporter = reporter;
    }

    @Provides
    @ScriptScope
    public ScriptParser2 provideScriptParser(Reporter reporter, Script script, Now now) {
        return new ScriptParser2(reporter, script, now);
    }

    @Provides
    public Reporter provideReporter() {
        return mReporter;
    }
}
