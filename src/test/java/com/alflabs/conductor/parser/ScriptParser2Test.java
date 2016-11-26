package com.alflabs.conductor.parser;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriSensor;
import com.alflabs.conductor.IJmriThrottle;
import com.alflabs.conductor.IJmriTurnout;
import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.script.Timer;
import com.alflabs.conductor.script.Var;
import com.alflabs.conductor.util.NowProvider;
import com.alflabs.conductor.util.NowProviderTest;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;

/**
 * Tests for both {@link ScriptParser2} *and* {@link Script} execution engine.
 */
public class ScriptParser2Test {
    private TestReporter mReporter;

    @Before
    public void setUp() throws Exception {
        mReporter = new TestReporter();
    }

    @Test
    public void testValidId() throws Exception {
        String source = "" +
                "Var id = 1\n" +
                "Var _  = 2\n" +
                "Var My-Var = 3\n" +
                "var id2=4\n" +
                "var __id3__=5\n";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("id").getAsInt()).isEqualTo(1);
        assertThat(script.getVar("_").getAsInt()).isEqualTo(2);
        assertThat(script.getVar("my-var").getAsInt()).isEqualTo(3);
        assertThat(script.getVar("id2").getAsInt()).isEqualTo(4);
        assertThat(script.getVar("__id3__").getAsInt()).isEqualTo(5);
    }

    @Test
    public void testDefineVar() throws Exception {
        String source = "  Var VALUE    = 5201 # d&rgw ";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("value")).isNotNull();
        Var var = script.getVar("Value");
        assertThat(var.getAsInt()).isEqualTo(5201);
    }

    @Test
    public void testDefineVar_missingId() throws Exception {
        String source = "  Var = 5201 ";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: missing ID at '='.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineVar_alreadyDefined() throws Exception {
        String source = "" +
                "  Var VALUE    = 5201 \n " +
                "var value = 42";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("Error at line 2: Name 'value' is already defined.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineSensor() throws Exception {
        String source = "  Sensor Alias   = NS784 ";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getSensor("alias")).isNotNull();
    }

    @Test
    public void testDefineSensor_alreadyDefined() throws Exception {
        String source = "" +
                "Sensor Alias   = NS784 \n " +
                "sensor alias   = B42";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("Error at line 2: Name 'alias' is already defined.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineSensor_invalidValue() throws Exception {
        String source = "sensor alias   = 42";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: mismatched input '42' expecting ID.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineTurnout() throws Exception {
        String source = "  Turnout TT   = NS784 ";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getTurnout("tt")).isNotNull();
    }

    @Test
    public void testDefineThrottle() throws Exception {
        String source = "  Throttle TH   = 5201 ";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getThrottle("th")).isNotNull();
    }

    @Test
    public void testDefineThrottle_invalidDccAddress() throws Exception {
        String source = "  Throttle TH   = Block42 ";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: mismatched input 'Block42' expecting NUM.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineTimer() throws Exception {
        String source = "  Timer Timer-1 = 5 ";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getTimer("TIMER-1")).isNotNull();
        Timer timer = script.getTimer("timer-1");
        assertThat(timer.getDurationSec()).isEqualTo(5);
    }

    @Test
    public void testActionSpacing() throws Exception {
        // Note that IDs can use "-" and "-" can also be part of "->".
        // "->" needs a space before otherwise the "-" is parsed as part of the previous ID.
        String source = "" +
                "Var My-Var=1\n" +
                "my-var ->my-var=2\n";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("my-var").getAsInt()).isEqualTo(1);
    }

    @Test
    public void testThrottleConditionErrors() throws Exception {
        String source = "" +
                "throttle T1    = 42 \n" +
                "sensor block   = B42 \n" +
                "t1 stop        -> t1 stop \n" +    // condition is "stopped", action is "stop"
                "t1             -> t1 stop \n" +    // throttle is not a condition without a keyword
                "t1 !forward    -> t1 stop \n" +
                "!forward       -> t1 stop\n" +
                "block stopped  -> t1 stop";
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 3: no viable alternative at input 'stop'.\n" +
                "Error at line 5: no viable alternative at input '!'.\n" +
                "Error at line 6: missing ID at 'forward'.\n" +
                "Error at line 3: Unexpected symbol: 'stop'.\n" +
                "Error at line 3: Unknown event condition 't1'.\n" +
                "Error at line 4: Unknown event condition 't1'.\n" +
                "Error at line 5: Unexpected symbol: '!'.\n" +
                "Error at line 5: Unknown event condition 't1'.\n" +
                "Error at line 6: Unexpected symbol: '<missing ID>'.\n" +
                "Error at line 6: Expected throttle ID for 'forward' but found '<missing ID>'.\n" +
                "Error at line 7: Expected throttle ID for 'stopped' but found 'block'.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testThrottleActionErrors() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n" +
                "sensor block  = B42 \n" +
                "t1 stopped -> t1 stopped \n" +     // condition is "stopped", action is "stop"
                "t1 forward -> !t1 stopped \n" +    // no negation on actions
                "t1 forward -> t1 normal\n" +       // turnout op instead of throttle op
                "block      -> t1 stop = 5\n" +     // valid but ignored
                "block      -> t1 start\n" +        // timer op instead of throttle op
                "t1 forward -> t1 forward = -12\n" +
                "t1 forward -> t1 forward = B42\n" +
                "t1 forward -> t1 forward = block" ;
        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 3: no viable alternative at input 'stopped'.\n" +
                "Error at line 4: extraneous input '!' expecting ID.\n" +
                "Error at line 4: no viable alternative at input 'stopped'.\n" +
                "Error at line 8: token recognition error at: '-1'.\n" +
                "Error at line 3: Unexpected symbol: 'stopped'.\n" +
                "Error at line 3: Expected var ID but found 't1'.\n" +
                "Error at line 4: Unexpected symbol: '!'.\n" +
                "Error at line 4: Expected var ID but found 't1'.\n" +
                "Error at line 5: Expected turnout ID for 'normal' but found 't1'.\n" +
                "Error at line 7: Expected timer ID for 'start' but found 't1'.\n" +
                "Error at line 9: Expected NUM or ID argument for 't1' but found 'B42'.\n" +
                "Error at line 10: Expected NUM or ID argument for 't1' but found 'block'.");
        assertThat(script).isNotNull();
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

        Script script = new ScriptParser2().parse(source, mReporter);

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

        Script script = new ScriptParser2().parse(source, mReporter);

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
    public void testThrottleFn() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "t1 stopped -> t1 f1 = 1 ; t1 f0     ; t1 f28 = 42 \n" +
                "t1 forward -> t1 f1     ; t1 f0 = 0 ; t1 f28 = 0  ";

        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);

        script.setup(provider);
        verify(provider).getThrotlle(42);

        // Execute t1 stopped case
        script.handle();
        verify(throttle).triggerFunction(1, true);
        verify(throttle).triggerFunction(0, false);
        verify(throttle).triggerFunction(28, true);

        // Execute t1 forward case
        script.getThrottle("t1").setSpeed(5);
        reset(throttle);
        script.handle();
        verify(throttle).triggerFunction(1, false);
        verify(throttle).triggerFunction(0, false);
        verify(throttle).triggerFunction(28, false);
    }

    @Test
    public void testActionVar() throws Exception {

        String source = "" +
                "throttle T1=42\n " +
                "var myVar=5\n " +
                "t1 stopped ->myVar=0\n" +
                "t1 forward ->myVar=1 ";

        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);

        script.setup(provider);
        verify(provider).getThrotlle(42);
        assertThat(script.getVar("myvar").getAsInt()).isEqualTo(5);

        // Execute with throttle defaulting to speed 0 (stopped).
        script.handle();
        assertThat(script.getVar("myvar").getAsInt()).isEqualTo(0);

        // Execute with throttle at speed 5
        script.getThrottle("t1").setSpeed(5);
        script.handle();
        assertThat(script.getVar("myvar").getAsInt()).isEqualTo(1);
    }

    @Test
    public void testActionSound() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "t1 stopped -> t1 Sound=0 \n" +
                "t1 forward -> t1 Sound=1";

        Script script = new ScriptParser2().parse(source, mReporter);

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

        Script script = new ScriptParser2().parse(source, mReporter);

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
                "t1 stopped -> T1 Horn \n" +
                "t1 forward -> T1 Horn=1";

        Script script = new ScriptParser2().parse(source, mReporter);

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
                "!b1         -> T1 Light=0 \n" +
                " B1         -> t1 Light=1 \n" +
                " b1 & !b777 -> t1 Sound=0 \n" +
                " B1 &  B777 -> T1 Sound=1 \n" ;

        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        IJmriSensor sensor1 = mock(IJmriSensor.class);
        IJmriSensor sensor2 = mock(IJmriSensor.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);
        when(provider.getSensor("NS42")).thenReturn(sensor1);
        when(provider.getSensor("NS7805")).thenReturn(sensor2);

        script.setup(provider);
        verify(provider).getThrotlle(42);
        verify(provider).getSensor("NS42");
        verify(provider).getSensor("NS7805");

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
    public void testActionTurnout() throws Exception {
        // Note: syntax if parser one for turnouts was "-> t1 = normal"
        // whereas new syntax is                       "-> t1 normal"
        String source = "" +
                "throttle th = 42 \n " +
                "turnout T1  = NT42 \n" +
                "turnout t2  = NT43 \n" +
                "th stopped -> T1 normal ; t2 reverse\n" +
                "th forward -> t1 reverse ; t2 normal \n" +
                " T1        -> th sound = 0 \n" +
                "!t2        -> th sound = 1 \n" ;

        Script script = new ScriptParser2().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        IJmriTurnout turnout1 = mock(IJmriTurnout.class);
        IJmriTurnout turnout2 = mock(IJmriTurnout.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);
        when(provider.getTurnout("NT42")).thenReturn(turnout1);
        when(provider.getTurnout("NT43")).thenReturn(turnout2);

        script.setup(provider);
        verify(provider).getThrotlle(42);
        verify(provider).getTurnout("NT42");
        verify(provider).getTurnout("NT43");

        script.handle();
        verify(turnout1).setTurnout(IJmriTurnout.NORMAL);
        verify(turnout2).setTurnout(IJmriTurnout.REVERSE);
        verify(throttle).setSound(false);
        // Note: line "!t2->..." is not invoked at first because t2 was true in this handle call.
        // The "t2 = reverse" action will only be noticed at the next handle call.
        verify(throttle, never()).setSound(true);

        script.handle();
        verify(throttle).setSound(true);

        reset(turnout1);
        reset(turnout2);
        script.getThrottle("th").setSpeed(5);
        script.handle();
        verify(turnout1).setTurnout(IJmriTurnout.REVERSE);
        verify(turnout2).setTurnout(IJmriTurnout.NORMAL);
    }

    @Test
    public void testTimer() throws Exception {
        String source = "" +
                "throttle th = 42 \n " +
                "timer T1  = 5 # seconds \n" +
                "timer t2  = 2 \n" +
                "th stopped -> T1 start\n" +
                "T1         -> th horn ; t2 start \n" +
                "t2         -> t1 end ; th forward = 1 \n" ; // "Timer end" is optional

        NowProviderTest.TestableNowProvider now =
                new NowProviderTest.TestableNowProvider(1000);

        Script script = new TestableScriptParser2(now).parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);
        script.setup(provider);

        // throttle is stopped, starts t1
        assertThat(script.getTimer("t1").isActive()).isFalse();
        script.handle();
        assertThat(script.getTimer("t1").isActive()).isFalse();

        // t1 is active 5 seconds later
        now.add(5*1000 - 1);
        script.handle();
        assertThat(script.getTimer("t1").isActive()).isFalse();

        // Note: timer is still active because the "t1 ->" does not reset it with end yet.
        // A timer remain active till it is either restarted or ended.
        now.add(1);
        script.handle();
        verify(throttle).horn();
        verify(throttle, never()).setSpeed(anyInt());
        assertThat(script.getTimer("t1").isActive()).isTrue();

        // t2 is active 2 seconds later. Both t1 gets reset as soon as t2 becomes active.
        now.add(2*1000);
        script.handle();
        verify(throttle).setSpeed(anyInt());
        assertThat(script.getTimer("t1").isActive()).isFalse();
        assertThat(script.getTimer("t2").isActive()).isTrue();
    }

    @Test
    public void testScript1() throws Exception {
        String source = getFileSource("script1.txt");
        assertThat(source).isNotNull();
        Script script = new ScriptParser2().parse(source, mReporter);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    @Test
    public void testScript2() throws Exception {
        String source = getFileSource("script2.txt");
        assertThat(source).isNotNull();
        Script script = new ScriptParser2().parse(source, mReporter);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    private String getFileSource(String fileName) throws IOException {
        String path = new File("v2", fileName).getPath();
        return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
    }

    private static class TestableScriptParser2 extends ScriptParser2 {
        private final NowProvider mNowProvider;

        public TestableScriptParser2(NowProvider nowProvider) {
            mNowProvider = nowProvider;
        }

        @Override
        Timer createTimer(int durationSec, NowProvider nowProvider) {
            return super.createTimer(durationSec, mNowProvider);
        }
    }
}
