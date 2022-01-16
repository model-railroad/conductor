package com.alflabs.conductor.v1;

import com.alflabs.annotations.NonNull;
import com.alflabs.conductor.v1.dagger.IScriptComponent;
import com.alflabs.conductor.v1.parser.ScriptParser2;
import com.alflabs.conductor.v1.script.ExecEngine;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

@Singleton
public class ScriptLoader {

    private final ILogger mLogger;

    @Inject
    public ScriptLoader(
            ILogger logger) {
        mLogger = logger;
    }

    public void execByPath(
            @NonNull ScriptContext scriptContext,
            @NonNull File filePath) throws Exception {
        // Sanitize the path
        String path = filePath.getPath();
        if (!path.endsWith(".txt")) {
            path += ".txt";
            filePath = new File(path);
        }

        IScriptComponent scriptComponent = scriptContext
                .getScriptComponentFactory()
                .createComponent(scriptContext.createErrorReporter(mLogger));
        scriptContext.set(scriptComponent, filePath);

        try {
            ScriptParser2 parser = scriptComponent.createScriptParser2();
            parser.parse(filePath);
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
