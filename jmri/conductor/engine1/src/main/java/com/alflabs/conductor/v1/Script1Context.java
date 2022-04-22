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
import com.alflabs.conductor.v1.dagger.IScript1Component;
import com.alflabs.conductor.v1.parser.Reporter;
import com.alflabs.conductor.v1.script.ExecEngine1;
import com.alflabs.conductor.v1.script.Script1;
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
public class Script1Context {
    private final IScript1Component.Factory mScript1ComponentFactory;
    private final StringBuilder mError = new StringBuilder();
    private IScript1Component mScript1Component;
    private File mScript1File;

    @Inject
    public Script1Context(IScript1Component.Factory script1ComponentFactory) {
        mScript1ComponentFactory = script1ComponentFactory;
    }

    public void reset() {
        mScript1Component = null;
        mError.setLength(0);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Script1Context setScript1Component(@Null IScript1Component script1Comp) {
        mScript1Component = script1Comp;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Script1Context setScript1File(@Null File script1File) {
        mScript1File = script1File;
        return this;
    }

    @NonNull
    public Optional<File> getScript1File() {
        return Optional.ofNullable(mScript1File);
    }

    @NonNull
    public IScript1Component.Factory getScript1ComponentFactory() {
        return mScript1ComponentFactory;
    }

    /** Returns the current ScriptComponent if set. */
    @NonNull
    public Optional<IScript1Component> getScript1Component() {
        return Optional.ofNullable(mScript1Component);
    }

    /** Convenience method to return the ScriptComponent.ExecEngine1. Can be null. */
    @NonNull
    public Optional<ExecEngine1> getExecEngine1() {
        if (mScript1Component == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mScript1Component.getExecEngine1());
    }

    /** Convenience method to return the current parsed Script1. Can be null. */
    @NonNull
    public Optional<Script1> getScript1() {
        if (mScript1Component == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mScript1Component.getScript1());
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
