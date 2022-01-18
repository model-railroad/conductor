/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.conductor.v1;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.conductor.v1.dagger.IScriptComponent;
import com.alflabs.conductor.v1.parser.Reporter;
import com.alflabs.conductor.v1.script.ExecEngine;
import com.alflabs.conductor.v1.script.Script;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Optional;

/**
 * A global singleton context for the currently running script.
 * It holds the current script filename, the script-scoped component, and the loading error.
 */
@Singleton
public class ScriptContext {
    private final IScriptComponent.Factory mScriptComponentFactory;
    private final StringBuilder mError = new StringBuilder();
    private IScriptComponent mScriptComponent;
    private File mScriptFile;

    @Inject
    public ScriptContext(IScriptComponent.Factory scriptComponentFactory) {
        mScriptComponentFactory = scriptComponentFactory;
    }

    public void reset() {
        mScriptComponent = null;
        mError.setLength(0);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ScriptContext setScriptComponent(@Null IScriptComponent scriptComp) {
        mScriptComponent = scriptComp;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ScriptContext setScriptFile(@Null File scriptFile) {
        mScriptFile = scriptFile;
        return this;
    }

    @NonNull
    public Optional<File> getScriptFile() {
        return Optional.ofNullable(mScriptFile);
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
        if (mScriptComponent == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mScriptComponent.createScriptExecEngine());
    }

    /** Convenience method to return the current parsed Script. Can be null. */
    @NonNull
    public Optional<Script> getScript() {
        if (mScriptComponent == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mScriptComponent.getScript());
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
