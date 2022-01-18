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
import com.alflabs.conductor.v1.dagger.IScriptComponent;
import com.alflabs.conductor.v1.parser.ScriptParser2;
import com.alflabs.conductor.v1.script.ExecEngine;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * A utility helper to load a script and start executing it.
 */
@Singleton
public class ScriptLoader {

    private final ILogger mLogger;

    @Inject
    public ScriptLoader(ILogger logger) {
        mLogger = logger;
    }

    public void execByPath(@NonNull ScriptContext scriptContext) throws Exception {
        // Sanitize the path
        File file = scriptContext
                .getScriptFile()
                .orElseThrow(() -> new IllegalArgumentException("Script File Not Defined"));
        String path = file.getPath();
        if (!path.endsWith(".txt")) {
            path += ".txt";
            file = new File(path);
        }

        IScriptComponent scriptComponent = scriptContext
                .getScriptComponentFactory()
                .createComponent(scriptContext.createErrorReporter(mLogger));
        scriptContext.setScriptComponent(scriptComponent);

        try {
            ScriptParser2 parser = scriptComponent.createScriptParser2();
            parser.parse(file);
            ExecEngine engine = scriptComponent.createScriptExecEngine();
            engine.onExecStart();
        } catch (Throwable t) {
            // Throwable t2 = StackTraceUtils.sanitize(t); -- for Groovy scripting only
            StackTraceElement[] stackTrace = t.getStackTrace();
            String msg = t.getMessage();
            if (stackTrace != null && stackTrace.length > 0) {
                msg = stackTrace[0].toString() + " :\n" + msg;
            }
            throw new Exception(msg, t);
        }
    }
}
