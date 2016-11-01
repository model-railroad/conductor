package com.alfray.conductor.parser;

import com.alfray.conductor.IJmriProvider;
import com.alfray.conductor.IJmriSensor;
import com.alfray.conductor.IJmriThrottle;
import com.alfray.conductor.script.Script;
import com.alfray.conductor.script.Timer;
import com.alfray.conductor.script.Var;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScriptParserTest {
    private TestReporter mReporter;

    @Before
    public void setUp() throws Exception {
        mReporter = new TestReporter();
    }

    @Test
    public void testDefineVar() throws Exception {
        String source = "  Var VALUE    = 1201 ";
        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("value")).isNotNull();
        Var var = script.getVar("Value");
        assertThat(var.getValue()).isEqualTo(1201);
    }

    @Test
    public void testDefineSensor() throws Exception {
        String source = "  Sensor Alias   = NS784 ";
        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getSensor("alias")).isNotNull();
    }

    @Test
    public void testDefineTimer() throws Exception {
        String source = "  Timer Timer-1 = 5 ";
        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getTimer("TIMER-1")).isNotNull();
        Timer timer = script.getTimer("timer-1");
        assertThat(timer.getDurationSec()).isEqualTo(5);
    }

    @Test
    public void testStopForwardParallel() throws Exception {
        // This starts with a throttle in the stopped state (speed == 0).
        // Note the possible ambiguity: checking for "stopped" sets the throttle to forward
        // then checking for forward sets it to stop. The script is set in such a way that
        // all conditions are checked first, so the throttle will always be in either the
        // forward or stopped condition and only one of the two actions can be executed but
        // never both.
        String source = "" +
                "throttle T1 = 42 \n " +
                "var speed = 5 \n " +
                "t1 stopped -> t1 forward = speed \n" +
                "t1 forward -> t1 stop";

        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);

        script.setup(provider);
        verify(provider).getThrotlle(42);

        // Execute with throttle defaulting to speed 0 (stopped)
        script.handle();
        verify(throttle).setSpeed(5);
        verify(throttle, never()).setSpeed(0);
        assertThat(script.getThrottle("t1").getSpeed()).isEqualTo(5);

        // Execute with throttle at speed 5
        reset(throttle);
        script.handle();
        verify(throttle, never()).setSpeed(5);
        verify(throttle).setSpeed(0);
        assertThat(script.getThrottle("t1").getSpeed()).isEqualTo(0);
    }

    @Test
    public void testStopForwardSequence() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "var speed = 5 \n " +
                "t1 stopped -> t1 forward = speed ; t1 stop \n" +
                "t1 forward -> t1 stop ; t1 forward = speed ";

        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);

        script.setup(provider);
        verify(provider).getThrotlle(42);

        // Execute with throttle defaulting to speed 0 (stopped). Speed is set then reset.
        script.handle();
        verify(throttle).setSpeed(0);
        verify(throttle).setSpeed(5);
        assertThat(script.getThrottle("t1").getSpeed()).isEqualTo(0);

        // Execute with throttle at speed 5
        script.getThrottle("t1").setSpeed(5);
        reset(throttle);
        script.handle();
        verify(throttle).setSpeed(0);
        verify(throttle).setSpeed(5);
        assertThat(script.getThrottle("t1").getSpeed()).isEqualTo(5);
    }

    @Test
    public void testActionVar() throws Exception {

        String source = "" +
                "throttle T1=42\n " +
                "var myVar=5\n " +
                "t1 stopped->myVar=0\n" +
                "t1 forward->myVar=1 ";

        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);

        script.setup(provider);
        verify(provider).getThrotlle(42);
        assertThat(script.getVar("myvar").getValue()).isEqualTo(5);

        // Execute with throttle defaulting to speed 0 (stopped).
        script.handle();
        assertThat(script.getVar("myvar").getValue()).isEqualTo(0);

        // Execute with throttle at speed 5
        script.getThrottle("t1").setSpeed(5);
        script.handle();
        assertThat(script.getVar("myvar").getValue()).isEqualTo(1);
    }

    @Test
    public void testActionSound() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "t1 stopped -> t1 Sound=0 \n" +
                "t1 forward -> t1 Sound=1";

        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);

        script.setup(provider);
        verify(provider).getThrotlle(42);

        // Execute with throttle defaulting to speed 0 (stopped)
        script.handle();
        verify(throttle).setSound(false);

        // Execute with throttle at forward speed
        reset(throttle);
        script.getThrottle("t1").setSpeed(5);
        script.handle();
        verify(throttle).setSound(true);
    }

    @Test
    public void testActionLight() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "t1 stopped -> t1 Light=0 \n" +
                "t1 forward -> t1 Light=1";

        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);

        script.setup(provider);
        verify(provider).getThrotlle(42);

        // Execute with throttle defaulting to speed 0 (stopped)
        script.handle();
        verify(throttle).setLight(false);

        // Execute with throttle at forward speed
        reset(throttle);
        script.getThrottle("t1").setSpeed(5);
        script.handle();
        verify(throttle).setLight(true);
    }

    @Test
    public void testActionHorn() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "t1 stopped -> t1 Horn \n" +
                "t1 forward -> t1 Horn=1";

        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);

        script.setup(provider);
        verify(provider).getThrotlle(42);

        // Execute with throttle defaulting to speed 0 (stopped)
        script.handle();
        verify(throttle).horn();

        // Execute with throttle at forward speed
        reset(throttle);
        script.getThrottle("t1").setSpeed(5);
        script.handle();
        verify(throttle).horn();
    }

    @Test
    public void testActionSensor() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "sensor b1  = NS42 \n" +
                "sensor b777= NS7805 \n" +
                "!b1         -> t1 Light=0 \n" +
                " b1         -> t1 Light=1 \n" +
                " b1 + !b777 -> t1 Sound=0 \n" +
                " b1 +  b777 -> t1 Sound=1 \n" ;

        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        IJmriSensor sensor1 = mock(IJmriSensor.class);
        IJmriSensor sensor2 = mock(IJmriSensor.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);
        when(provider.getSensor("ns42")).thenReturn(sensor1);
        when(provider.getSensor("ns7805")).thenReturn(sensor2);

        script.setup(provider);
        verify(provider).getThrotlle(42);
        verify(provider).getSensor("ns42");
        verify(provider).getSensor("ns7805");

        when(sensor1.isActive()).thenReturn(false);
        when(sensor2.isActive()).thenReturn(false);
        reset(throttle);
        script.handle();
        verify(throttle).setLight(false);
        verify(throttle, never()).setSound(anyBoolean());

        when(sensor1.isActive()).thenReturn(false);
        when(sensor2.isActive()).thenReturn(true);
        reset(throttle);
        script.handle();
        // Note: event !b1 is not executed a second time till the condition gets invalidated
        verify(throttle, never()).setLight(anyBoolean());
        verify(throttle, never()).setSound(anyBoolean());

        when(sensor1.isActive()).thenReturn(true);
        when(sensor2.isActive()).thenReturn(false);
        reset(throttle);
        script.handle();
        verify(throttle).setLight(true);
        verify(throttle).setSound(false);

        when(sensor1.isActive()).thenReturn(true);
        when(sensor2.isActive()).thenReturn(true);
        reset(throttle);
        script.handle();
        // Note: event b1 is not executed a second time till the condition gets invalidated
        verify(throttle, never()).setLight(anyBoolean());
        verify(throttle).setSound(true);
    }

    @Test
    public void testScript1() throws Exception {
        String source = Resources.toString(Resources.getResource("script1.txt"), Charsets.UTF_8);
        assertThat(source).isNotNull();
        Script script = ScriptParser.parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    @Test
    public void testScript2() throws Exception {
        String source = Resources.toString(Resources.getResource("script2.txt"), Charsets.UTF_8);
        assertThat(source).isNotNull();
        Script script = ScriptParser.parse(source, mReporter);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
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
