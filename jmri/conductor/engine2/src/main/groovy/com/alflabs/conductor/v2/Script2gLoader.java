package com.alflabs.conductor.v2;

import com.alflabs.annotations.NonNull;
import com.alflabs.conductor.v2.script.RootScript;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.StackTraceUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Script2gLoader {
    private Optional<Binding> mBinding = Optional.empty();
    private Optional<RootScript> mScript = Optional.empty();

    private String mScriptPrefix = "package v2.script\n" +
                "import com.alflabs.conductor.v2.script.RootScript\n" +
                "import groovy.transform.BaseScript\n" +
                "@BaseScript RootScript baseScript\n";

    public Optional<Binding> getBinding() {
        return mBinding;
    }

    public Optional<RootScript> getScript() {
        return mScript;
    }

    @NonNull
    public void loadScriptFromFile(String scriptName) throws Exception {
        String scriptText = readScriptText(scriptName);
        loadScriptFromText(scriptText);
    }

    void loadScriptFromText(String scriptText) throws Exception {
        loadScriptFromText("local", scriptText);
    }

    public void loadScriptFromText(String scriptName, String scriptText) throws Exception {
        // Important order: we need to load the script, and then _execute_ it in order
        // for all variables to be created in the bindings. Only after can we find their
        // names and resolve them. Local variables (defined with 'def' or a type) are not
        // visible in the binding, and we cannot resolve these.
        mScript = Optional.of(loadScript(scriptName, scriptText));
        runScript();
        mScript.get().resolvePendingVars(mBinding.get());
    }

    @SuppressWarnings("UnstableApiUsage")
    private String readScriptText(String scriptName) throws IOException {
        if (!scriptName.endsWith(".groovy")) {
            scriptName += ".groovy";
        }
        String path = "v2/script/" + scriptName;
        URL url = Resources.getResource(path);
        System.out.println(url.toString());
        String scriptText = Resources.toString(url, Charsets.UTF_8);
        if (scriptText.isEmpty()) {
            throw new IllegalStateException("Source is empty: " + path);
        }

        scriptText = scriptText.replaceAll("-->", "then");
        return scriptText;
    }

    private RootScript loadScript(String scriptName, String scriptText) {
        mBinding = Optional.of(new Binding());
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(RootScript.class.getName());

        GroovyShell shell = new GroovyShell(
                this.getClass().getClassLoader(),
                mBinding.get(),
                config);
        groovy.lang.Script script = shell.parse(scriptText, scriptName);
        if (!(script instanceof RootScript)) {
            throw new IllegalStateException("Source is not RootScript class: " + scriptName);
        }
        return (RootScript) script;
    }

    private void runScript() throws Exception {
        try {
            // This runs the script and actually creates the variables.
            mScript.get().run();
        } catch (Throwable t) {
            Throwable t2 = StackTraceUtils.sanitize(t);
            StackTraceElement[] stackTrace = t2.getStackTrace();
            String msg = t2.getMessage();
            if (stackTrace != null && stackTrace.length > 0) {
                msg = stackTrace[0].toString() + " :\n" + msg;
            }
            throw new Exception(msg, t2);
        }
    }
}

