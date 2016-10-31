package com.alfray.conductor.parser;

import com.alfray.conductor.IJmriProvider;
import com.alfray.conductor.IJmriThrottle;
import com.alfray.conductor.script.Script;
import com.alfray.conductor.script.Timer;
import com.alfray.conductor.script.Var;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScriptParserTest {
    private TestReporter mReporter;

    @Before
    public void setUp() throws Exception {
        mReporter = new TestReporter();
    }

    @Test
    public void testVar() throws Exception {
        String source = "  Var DCC    = 1201 ";
        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("dcc")).isNotNull();
        Var var = script.getVar("DCC");
        assertThat(var.getValue()).isEqualTo(1201);
    }

    @Test
    public void testSensor() throws Exception {
        String source = "  Sensor Alias   = NS784 ";
        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getSensor("alias")).isNotNull();
    }

    @Test
    public void testTimer() throws Exception {
        String source = "  Timer Timer-1 = 5 ";
        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getTimer("TIMER-1")).isNotNull();
        Timer timer = script.getTimer("timer-1");
        assertThat(timer.getDurationSec()).isEqualTo(5);
    }

    @Test
    public void testStopForward() throws Exception {
        String source = "var dcc = 42 \n var speed = 5 \n stopped -> forward = speed";

        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);

        script.setup(provider);
        verify(provider).getThrotlle(42);

        script.handle();
        verify(throttle).setSpeed(5);
    }

    @Test
    public void testScript1() throws Exception {
        String script1 = Resources.toString(Resources.getResource("script1.txt"), Charsets.UTF_8);
        assertThat(script1).isNotNull();
    }

    public static class TestReporter extends ScriptParser.Reporter {
        private String report = "";

        @Override
        public void report(String line, int lineCount, String error) {
            report += String.format("Error at line %d: %s\n", lineCount, error);
        }

        @Override
        public String toString() {
            return report;
        }
    }
}
