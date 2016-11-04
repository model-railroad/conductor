package com.alfray.conductor.parser;

import com.alfray.conductor.IJmriProvider;
import com.alfray.conductor.IJmriSensor;
import com.alfray.conductor.IJmriThrottle;
import com.alfray.conductor.IJmriTurnout;
import com.alfray.conductor.script.Script;
import com.alfray.conductor.script.Timer;
import com.alfray.conductor.script.Var;
import com.alfray.conductor.util.NowProvider;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for both {@link ScriptParser} *and* {@link Script} execution engine.
 */
public class ScriptParserTest {
    private TestReporter mReporter;

    @Before
    public void setUp() throws Exception {
        mReporter = new TestReporter();
    }

    @Test
    public void testDefineVar() throws Exception {
        String source = "  Var VALUE    = 5201 # d&rgw ";
        Script script = new ScriptParser().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("value")).isNotNull();
        Var var = script.getVar("Value");
        assertThat(var.getValue()).isEqualTo(5201);
    }

    @Test
    public void testDefineSensor() throws Exception {
        String source = "  Sensor Alias   = NS784 ";
        Script script = new ScriptParser().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getSensor("alias")).isNotNull();
    }

    @Test
    public void testDefineTurnout() throws Exception {
        String source = "  Turnout TT   = NS784 ";
        Script script = new ScriptParser().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getTurnout("tt")).isNotNull();
    }

    @Test
    public void testDefineTimer() throws Exception {
        String source = "  Timer Timer-1 = 5 ";
        Script script = new ScriptParser().parse(source, mReporter);

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

        Script script = new ScriptParser().parse(source, mReporter);

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

        Script script = new ScriptParser().parse(source, mReporter);

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

        Script script = new ScriptParser().parse(source, mReporter);

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

        Script script = new ScriptParser().parse(source, mReporter);

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

        Script script = new ScriptParser().parse(source, mReporter);

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

        Script script = new ScriptParser().parse(source, mReporter);

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

        Script script = new ScriptParser().parse(source, mReporter);

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
    public void testActionTurnout() throws Exception {
        String source = "" +
                "throttle th = 42 \n " +
                "turnout t1  = NT42 \n" +
                "turnout t2  = NT43 \n" +
                "th stopped -> t1 = normal ; t2 = reverse\n" +
                "th forward -> t1 = reverse ; t2 = normal \n" +
                " t1        -> th sound = 0 \n" +
                "!t2        -> th sound = 1 \n" ;

        Script script = new ScriptParser().parse(source, mReporter);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriProvider provider = mock(IJmriProvider.class);
        IJmriThrottle throttle = mock(IJmriThrottle.class);
        IJmriTurnout turnout1 = mock(IJmriTurnout.class);
        IJmriTurnout turnout2 = mock(IJmriTurnout.class);
        when(provider.getThrotlle(42)).thenReturn(throttle);
        when(provider.getTurnout("nt42")).thenReturn(turnout1);
        when(provider.getTurnout("nt43")).thenReturn(turnout2);

        script.setup(provider);
        verify(provider).getThrotlle(42);
        verify(provider).getTurnout("nt42");
        verify(provider).getTurnout("nt43");

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
                "timer t1  = 5 # seconds \n" +
                "timer t2  = 2 \n" +
                "th stopped -> start = t1\n" +
                "t1         -> th horn ; start = t2 \n" +
                "t2         -> end = t1 ; end = t2 ; th forward = 1 \n" ;

        TestableNowProvider nowProvider = new TestableNowProvider(1000);

        Script script = new TestableScriptParser(nowProvider).parse(source, mReporter);

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
        nowProvider.add(5*1000 - 1);
        script.handle();
        assertThat(script.getTimer("t1").isActive()).isFalse();

        // Note: timer is still active because the "t1 ->" does not reset it with end yet.
        // A timer remain active till it is either restarted or ended.
        nowProvider.add(1);
        script.handle();
        verify(throttle).horn();
        verify(throttle, never()).setSpeed(anyInt());
        assertThat(script.getTimer("t1").isActive()).isTrue();

        // t2 is active 2 seconds later. Both t1 and t2 get reset as soon as t2 becomes active.
        nowProvider.add(2*1000);
        script.handle();
        verify(throttle).setSpeed(anyInt());
        assertThat(script.getTimer("t1").isActive()).isFalse();
        assertThat(script.getTimer("t2").isActive()).isFalse();
    }

    @Test
    public void testScript1() throws Exception {
        String source = Resources.toString(Resources.getResource("script1.txt"), Charsets.UTF_8);
        assertThat(source).isNotNull();
        Script script = new ScriptParser().parse(source, mReporter);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    @Test
    public void testScript2() throws Exception {
        String source = Resources.toString(Resources.getResource("script2.txt"), Charsets.UTF_8);
        assertThat(source).isNotNull();
        Script script = new ScriptParser().parse(source, mReporter);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    private static class TestReporter extends ScriptParser.Reporter {
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

    private static class TestableScriptParser extends ScriptParser {
        private final NowProvider mNowProvider;

        public TestableScriptParser(NowProvider nowProvider) {
            mNowProvider = nowProvider;
        }

        @Override
        Timer createTimer(int durationSec, NowProvider nowProvider) {
            return super.createTimer(durationSec, mNowProvider);
        }
    }

    private static class TestableNowProvider extends NowProvider {
        private long mNow;

        public TestableNowProvider(long now) {
            mNow = now;
        }

        public void add(long now) {
            mNow += now;
        }

        @Override
        public long now() {
            return mNow;
        }
    }
}
