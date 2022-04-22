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
import com.alflabs.conductor.v1.dagger.IScript1Component;
import com.alflabs.conductor.v1.parser.Script1Parser2;
import com.alflabs.conductor.v1.script.ExecEngine1;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * A utility helper to load a script and start executing it.
 */
@Singleton
public class Script1Loader {

    private final ILogger mLogger;

    @Inject
    public Script1Loader(ILogger logger) {
        mLogger = logger;
    }

    public void execByPath(@NonNull Script1Context scriptContext) throws Exception {
        // Sanitize the path
        File file = scriptContext
                .getScript1File()
                .orElseThrow(() -> new IllegalArgumentException("Script1 File Not Defined"));
        String path = file.getPath();
        if (!path.endsWith(".txt")) {
            path += ".txt";
            file = new File(path);
        }

        IScript1Component scriptComponent = scriptContext
                .getScript1ComponentFactory()
                .createComponent(scriptContext.createErrorReporter(mLogger));
        scriptContext.setScript1Component(scriptComponent);

        try {
            Script1Parser2 parser = scriptComponent.getScript1Parser2();
            parser.parse(file);
            ExecEngine1 engine = scriptComponent.getExecEngine1();
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
