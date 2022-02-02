package com.alfray.conductor.v2.script;

import com.alflabs.conductor.v2.script.Block;
import com.alflabs.conductor.v2.script.MapInfo;
import com.alflabs.conductor.v2.script.RootScript;
import com.alflabs.conductor.v2.script.Sensor;
import com.alflabs.conductor.v2.script.Turnout;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static com.google.common.truth.Truth.assertThat;

public class ScriptTest {

    private Binding mBinding;
    private RootScript mScript;

    @Before
    public void setUp() throws Exception {
        String scriptName = "sample_v2";
        String scriptText = readScriptText(scriptName);

        mScript = loadScript(scriptName, scriptText);
        runScript();
    }

    @SuppressWarnings("UnstableApiUsage")
    private String readScriptText(String scriptName) throws IOException {
        String path = "v2/script/" + scriptName + ".groovy";
        URL url = Resources.getResource(path);
        System.out.println(url.toString());
        String scriptText = Resources.toString(url, Charsets.UTF_8);
        assertThat(scriptText).isNotEmpty();

        scriptText = scriptText.replaceAll("-->", "then");
        return scriptText;
    }

    private RootScript loadScript(String scriptName, String scriptText) {
        mBinding = new Binding();
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(RootScript.class.getName());

        GroovyShell shell = new GroovyShell(
                this.getClass().getClassLoader(),
                mBinding,
                config);
        groovy.lang.Script script = shell.parse(scriptText, scriptName);
        assertThat(script).isInstanceOf(RootScript.class);
        return (RootScript) script;
    }

    private void runScript() throws Exception {
        try {
            // This runs the script and actually creates the variables.
            mScript.run();
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

    @Test
    public void testBlock() {
        assertThat(mBinding.getVariables()).containsKey("B310");
        assertThat(mBinding.getVariables()).containsKey("B311");
        assertThat(mBinding.getVariable("B310")).isInstanceOf(Block.class);
        assertThat(mBinding.getVariable("B311")).isInstanceOf(Block.class);

        assertThat(mScript.blocks()).containsKey("NS768");
        assertThat(mBinding.getVariable("B310"))
                .isSameAs(mScript.blocks().get("NS768"));
    }

    @Test
    public void testSensor() {
        assertThat(mBinding.getVariables()).containsKey("Toggle");
        assertThat(mBinding.getVariable("Toggle")).isInstanceOf(Sensor.class);

        assertThat(mScript.sensors()).containsKey("NS829");
        assertThat(mBinding.getVariable("Toggle"))
                .isSameAs(mScript.sensors().get("NS829"));
    }

    @Test
    public void testTurnout() {
        assertThat(mBinding.getVariables()).containsKey("T311");
        assertThat(mBinding.getVariable("T311")).isInstanceOf(Turnout.class);

        assertThat(mScript.turnouts()).containsKey("NT311");
        assertThat(mBinding.getVariable("T311"))
                .isSameAs(mScript.turnouts().get("NT311"));
    }

    @Test
    public void testStringVariables() {
        assertThat(mBinding.getVariables()).containsKey("JSON_URL");
        assertThat(mBinding.getVariables()).containsKey("GA_Tracking_Id");
        assertThat(mBinding.getVariables()).containsKey("GA_URL");
        assertThat(mBinding.getVariable("JSON_URL")).isInstanceOf(String.class);
        assertThat(mBinding.getVariable("GA_Tracking_Id")).isInstanceOf(String.class);
        assertThat(mBinding.getVariable("GA_URL")).isInstanceOf(String.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMapInfo() {
        assertThat(mScript.maps()).containsExactly(
                "Mainline",
                new MapInfo("Mainline", "Map 1.svg"));

        assertThat(mBinding.getVariables().values()
                .stream().map(v -> v.getClass().getSimpleName()).toArray())
                .asList().doesNotContain("MapInfo");
    }
}
