package com.alflabs.conductor.v1;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.conductor.v1.dagger.IScriptComponent;
import com.alflabs.conductor.v1.parser.Reporter;
import com.alflabs.conductor.v1.script.ExecEngine;
import com.alflabs.conductor.v1.script.Script;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;
import java.util.Optional;

public class ScriptContext {
    private final IScriptComponent.Factory mScriptComponentFactory;
    private final StringBuilder mError = new StringBuilder();
    private IScriptComponent mScriptComponent;
    private String mScriptPath;

    @Inject Script mScript;
    @Inject ExecEngine mExecEngine;

    public ScriptContext(@NonNull IScriptComponent.Factory scriptComponentFactory) {
        mScriptComponentFactory = scriptComponentFactory;
    }

    public void reset() {
        mScriptComponent = null;
        mExecEngine = null;
        mScript = null;
        mError.setLength(0);
    }

    public void set(IScriptComponent scriptComp, String scriptPath) {
        mScriptComponent = scriptComp;
        mScriptPath = scriptPath;
        mScriptComponent.inject(this);
    }

    @NonNull
    public IScriptComponent.Factory getScriptComponentFactory() {
        return mScriptComponentFactory;
    }

    /** Returns the current ScriptComponent if set. */
    @NonNull
    public Optional<IScriptComponent> getScriptComponent() {
        return Optional.ofNullable(mScriptComponent);
    }

    /** Convenience method to return the ScriptComponent.ExecEngine. Can be null. */
    @NonNull
    public Optional<ExecEngine> getExecEngine() {
        return Optional.ofNullable(mExecEngine);
    }

    /** Convenience method to return the current parsed Script. Can be null. */
    @NonNull
    public Optional<Script> getScript() {
        return Optional.ofNullable(mScript);
    }

    /** Returns the errors accumulated by the error reporter, if any. */
    @NonNull
    public String getError() {
        return mError.toString();
    }

    /**
     * Creates an error Reporter that logs using the provided ILogger and captures
     * major errors in the local mError StringBuilder.
     */
    @NonNull
    public Reporter createErrorReporter(@NonNull ILogger logger) {
        return new Reporter(logger) {
            private boolean mIsReport;

            @Override
            public void report(String line, int lineCount, String error) {
                mIsReport = true;
                super.report(line, lineCount, error);
                mIsReport = false;
            }

            @Override
            public void log(String msg) {
                super.log(msg);
                if (mIsReport) {
                    mError.append(msg);
                    if (msg.length() > 0 && !msg.endsWith("\n")) {
                        mError.append('\n');
                    }
                }
            }
        };
    }
}
