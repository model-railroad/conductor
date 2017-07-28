package com.alflabs.conductor.parser;

import com.alflabs.conductor.ConductorModule;
import com.alflabs.conductor.DaggerIConductorComponent;
import com.alflabs.conductor.IConductorComponent;
import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriSensor;
import com.alflabs.conductor.IJmriThrottle;
import com.alflabs.conductor.IJmriTurnout;
import com.alflabs.conductor.script.ExecEngine;
import com.alflabs.conductor.script.IScriptComponent;
import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.script.ScriptModule;
import com.alflabs.conductor.script.Timer;
import com.alflabs.conductor.script.Var;
import com.alflabs.conductor.util.FakeNow;
import com.alflabs.conductor.util.Now;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.MapInfo;
import com.alflabs.manifest.RouteInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.util.TreeMap;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for both {@link ScriptParser2} *and* {@link Script} execution engine
 * using isolated JUnit tests.
 */
public class ScriptParser2Test {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriProvider mJmriProvider;
    @Mock IJmriThrottle mJmriThrottle;
    @Mock IKeyValue mKeyValue;

    private FakeNow mNow;
    private TestReporter mReporter;
    private IScriptComponent mScriptComponent;
    private IScriptComponent mFakeNowScriptComponent;

    @Before
    public void setUp() throws Exception {

        when(mJmriProvider.getThrotlle(42)).thenReturn(mJmriThrottle);

        mReporter = new TestReporter();

        File file = File.createTempFile("conductor_tests", "tmp");
        file.deleteOnExit();

        IConductorComponent realNowComponent = DaggerIConductorComponent.builder()
                .conductorModule(new ConductorModule(mJmriProvider))
                .scriptFile(file)
                .build();

        mScriptComponent = realNowComponent.newScriptComponent(
                new ScriptModule(mReporter, mKeyValue));

        mNow = new FakeNow(1000);

        IConductorComponent fakeNowComponent = DaggerIConductorComponent.builder()
                .conductorModule(new ConductorModule(mJmriProvider) {
                    @Override
                    public Now provideNowProvider() {
                        return mNow;
                    }
                })
                .scriptFile(file)
                .build();

        mFakeNowScriptComponent = fakeNowComponent.newScriptComponent(
                new ScriptModule(mReporter, mKeyValue));
    }

    @Test
    public void testValidId() throws Exception {
        String source = "" +
                "Var id = 1\n" +
                "Var _  = 2\n" +
                "Var My-Var = 3\n" +
                "var id2=4\n" +
                "var __id3__=5\n";
        Script script = mScriptComponent.getScriptParser2().parse(source);

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
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("value")).isNotNull();
        Var var = script.getVar("Value");
        assertThat(var.getAsInt()).isEqualTo(5201);
    }

    @Test
    public void testDefineVar_missingId() throws Exception {
        String source = "  Var = 5201 ";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: missing ID at '='.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineVar_alreadyDefined() throws Exception {
        String source = "" +
                "  Var VALUE    = 5201 \n " +
                "var value = 42";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo(
                "Error at line 2: Name 'value' is already defined.\n" +
                "  Line 2: 'var value = 42'");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineSensor() throws Exception {
        String source = "  Sensor Alias   = NS784 ";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getSensor("alias")).isNotNull();
    }

    @Test
    public void testDefineSensor_alreadyDefined() throws Exception {
        String source = "" +
                "Sensor Alias   = NS784 \n " +
                "sensor alias   = B42";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo(
                "Error at line 2: Name 'alias' is already defined.\n" +
                "  Line 2: 'sensor alias   = B42'");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineSensor_invalidValue() throws Exception {
        String source = "sensor alias   = 42";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: mismatched input '42' expecting ID.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineTurnout() throws Exception {
        String source = "  Turnout TT   = NS784 ";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getTurnout("tt")).isNotNull();
    }

    @Test
    public void testDefineThrottle() throws Exception {
        String source = "  Throttle TH   = 5201 ";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getThrottle("th")).isNotNull();
    }

    @Test
    public void testDefineThrottle_invalidDccAddress() throws Exception {
        String source = "  Throttle TH   = Block42 ";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: mismatched input 'Block42' expecting NUM.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineMultiThrottle() throws Exception {
        String source = "  Throttle TH   = 5201 5202 5203 5204 ";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getThrottle("th")).isNotNull();
    }

    @Test
    public void testDefineMultiThrottle_invalidDccAddress() throws Exception {
        String source = "  Throttle TH   = 5201 5202 Block42 5203 5204 ";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: extraneous input 'Block42' expecting {<EOF>, EOL, SB_COMMENT, NUM}.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineTimer() throws Exception {
        String source = "  Timer Timer-1 = 5 ";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getTimer("TIMER-1")).isNotNull();
        Timer timer = script.getTimer("timer-1");
        assertThat(timer.getDurationSec()).isEqualTo(5);
    }

    @Test
    public void testDefineEnum() throws Exception {
        String source = "  Enum EN   = Init Idle Fwd Rev ";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getEnum("en")).isNotNull();
        assertThat(script.getEnum("en").getValues().toArray()).isEqualTo(
                new String[] { "init", "idle", "fwd", "rev" });
    }

    @Test
    public void testDefineEnum_alreadyDefined() throws Exception {
        String source = "" +
                "Enum EN   = Init Idle Fwd Rev \n" +
                "Enum EN   = Init Idle";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo(
                "Error at line 2: Name 'EN' is already defined.\n" +
                        "  Line 2: 'Enum EN   = Init Idle'");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineMap() throws Exception {
        String source = "" +
                "  Map Map-1 = \"path/to/map1.svg\" \n" +
                "  Map Map-2 = \"path\\to\\map2.svg\" ";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        TreeMap<String, MapInfo> maps = script.getMaps();
        assertThat(maps).hasFirstEntry("map-1", new MapInfo("Map-1", "path/to/map1.svg"));
        assertThat(maps).hasLastEntry ("map-2", new MapInfo("Map-2", "path\\to\\map2.svg"));
        assertThat(maps.get("map-1").toString()).isEqualTo("{\"name\":\"Map-1\",\"svg\":\"path/to/map1.svg\"}");
        assertThat(maps.get("map-2").toString()).isEqualTo("{\"name\":\"Map-2\",\"svg\":\"path\\\\to\\\\map2.svg\"}");
        assertThat(maps).hasSize(2);
    }

    @Test
    public void testDefineMap_alreadyDefined() throws Exception {
        String source = "" +
                "  Map Map-1 = \"path/to/map1.svg\" \n" +
                "  Map Map-1 = \"path\\to\\map2.svg\" ";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo(
                "Error at line 2: Name 'Map-1' is already defined.\n" +
                        "  Line 2: 'Map Map-1 = \"path\\to\\map2.svg\"'");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineRoute() throws Exception {
        String source = "" +
                "Throttle PA-100  = 100\n" +
                "Throttle BLT-200 = 200\n" +
                "Enum PA-Status   = INIT IDLE FWD\n" +
                "Var  BL-Status   = 300\n" +
                "Sensor PA-Toggle = NS420\n" +
                "Sensor BL-Toggle = NS430\n" +
                "Route Passenger  = Throttle: PA-100, Status: PA-Status, Toggle: PA-Toggle\n" +
                "Route Branchline=toggle:bl-toggle,status:bl-status,throttle:blt-200\n";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        TreeMap<String, RouteInfo> routes = script.getRoutes();
        assertThat(routes).hasFirstEntry("branchline", new RouteInfo("Branchline", "S:bl-toggle", "V:bl-status", "D:200"));
        assertThat(routes).hasLastEntry("passenger"  , new RouteInfo("Passenger" , "S:pa-toggle", "V:pa-status", "D:100"));
        assertThat(routes).hasSize(2);

        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        IJmriThrottle jmriThrottle100 = mock(IJmriThrottle.class);
        IJmriThrottle jmriThrottle200 = mock(IJmriThrottle.class);
        when(jmriThrottle100.getDccAddress()).thenReturn(100);
        when(jmriThrottle200.getDccAddress()).thenReturn(200);
        when(mJmriProvider.getThrotlle(100)).thenReturn(jmriThrottle100);
        when(mJmriProvider.getThrotlle(200)).thenReturn(jmriThrottle200);

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(100);
        verify(mJmriProvider).getThrotlle(200);

        ArgumentCaptor<String> keyCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class);
        verify(mKeyValue, atLeastOnce()).putValue(keyCapture.capture(), valueCapture.capture(), eq(true));
        assertThat(keyCapture.getAllValues().toArray()).isEqualTo(new String[]
                { "D:200", "D:100", "S:bl-toggle", "S:pa-toggle", "V:bl-status", "V:pa-status" });
        assertThat(valueCapture.getAllValues().toArray()).isEqualTo(new String[]
                { "0", "0", "OFF", "OFF", "300", "init" });
    }

    @Test
    public void testDefineRoute_MissingThrottle() throws Exception {
        String source = "" +
                "Enum PA-Status   = INIT IDLE FWD\n" +
                "Sensor PA-Toggle = NS420\n" +
                "Route Passenger  = Throttle: PA-100, Status: PA-Status, Toggle: PA-Toggle\n";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 3: Route 'Passenger': Id 'PA-100' for argument 'throttle' is not defined.\n" +
                "  Line 3: 'Route Passenger  = Throttle: PA-100, Status: PA-Status, Toggle: PA-Toggle'");
        assertThat(script).isNotNull();

        TreeMap<String, RouteInfo> routes = script.getRoutes();
        assertThat(routes).isNotNull();
        assertThat(routes).isEmpty();
    }

    @Test
    public void testDefineRoute_MissingStatus() throws Exception {
        String source = "" +
                "Throttle PA-100  = 100\n" +
                "Sensor PA-Toggle = NS420\n" +
                "Route Passenger  = Throttle: PA-100, Status: PA-Status, Toggle: PA-Toggle\n";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 3: Route 'Passenger': Id 'PA-Status' for argument 'status' is not defined.\n" +
                "  Line 3: 'Route Passenger  = Throttle: PA-100, Status: PA-Status, Toggle: PA-Toggle'");
        assertThat(script).isNotNull();

        TreeMap<String, RouteInfo> routes = script.getRoutes();
        assertThat(routes).isNotNull();
        assertThat(routes).isEmpty();
    }

    @Test
    public void testDefineRoute_MissingToggle() throws Exception {
        String source = "" +
                "Throttle PA-100  = 100\n" +
                "Enum PA-Status   = INIT IDLE FWD\n" +
                "Route Passenger  = Throttle: PA-100, Status: PA-Status, Toggle: PA-Toggle\n";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 3: Route 'Passenger': Id 'PA-Toggle' for argument 'toggle' is not defined.\n" +
                "  Line 3: 'Route Passenger  = Throttle: PA-100, Status: PA-Status, Toggle: PA-Toggle'");
        assertThat(script).isNotNull();

        TreeMap<String, RouteInfo> routes = script.getRoutes();
        assertThat(routes).isNotNull();
        assertThat(routes).isEmpty();
    }

    @Test
    public void testDefineRoute_DupToggle() throws Exception {
        String source = "" +
                "Throttle PA-100  = 100\n" +
                "Enum PA-Status   = INIT IDLE FWD\n" +
                "Sensor PA-Toggle = NS420\n" +
                "Sensor MyToggle  = NS421\n" +
                "Route Passenger  = Toggle: MyToggle, Throttle: PA-100, Status: PA-Status, Toggle: PA-Toggle\n";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 5: Route 'Passenger': Argument 'toggle' is already defined.\n" +
                "  Line 5: 'Route Passenger  = Toggle: MyToggle, Throttle: PA-100, Status: PA-Status, Toggle: PA-Toggle'");
        assertThat(script).isNotNull();

        TreeMap<String, RouteInfo> routes = script.getRoutes();
        assertThat(routes).isNotNull();
        assertThat(routes).isEmpty();
    }

    @Test
    public void testActionSpacing() throws Exception {
        // Note that IDs can use "-" and "-" can also be part of "->".
        // "->" needs a space before otherwise the "-" is parsed as part of the previous ID.
        String source = "" +
                "Var My-Var=1\n" +
                "my-var->my-var=2\n";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("my-var").getAsInt()).isEqualTo(1);
    }

    @Test
    public void testEnumDefineCondAction() throws Exception {
        String source = "" +
                "Enum State = Init Idle Fwd Rev\n" +
                "State == INIT -> State = Idle\n";
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getEnum("State").get()).isEqualTo("init");
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
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 3: extraneous input 'stop' expecting {'->', '&'}.\n" +
                "Error at line 5: mismatched input '!' expecting {'->', '&'}.\n" +
                "Error at line 6: missing ID at 'forward'.\n" +
                "Error at line 3: Unknown event condition 't1'.\n" +
                "  Line 3: 't1 stop        -> t1 stop'\n" +
                "Error at line 4: Unknown event condition 't1'.\n" +
                "  Line 4: 't1             -> t1 stop'\n" +
                "Error at line 5: Unknown event condition 't1'.\n" +
                "  Line 5: 't1 !forward    -> t1 stop'\n" +
                "Error at line 6: Unexpected symbol: '<missing ID>'.\n" +
                "  Line 6: '!forward       -> t1 stop'\n" +
                "Error at line 6: Expected throttle ID for 'forward' but found '<missing ID>'.\n" +
                "  Line 6: '!forward       -> t1 stop'\n" +
                "Error at line 7: Expected throttle ID for 'stopped' but found 'block'.\n" +
                "  Line 7: 'block stopped  -> t1 stop'");
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
        Script script = mScriptComponent.getScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 3: no viable alternative at input 'stopped'.\n" +
                "Error at line 4: no viable alternative at input '!'.\n" +
                "Error at line 8: token recognition error at: '-1'.\n" +
                "Error at line 3: Unexpected symbol: 'stopped'.\n" +
                "  Line 3: 't1 stopped -> t1 stopped'\n" +
                "Error at line 3: Expected var ID but found 't1'.\n" +
                "  Line 3: 't1 stopped -> t1 stopped'\n" +
                "Error at line 4: Unexpected symbol: '!'.\n" +
                "  Line 4: 't1 forward -> !t1 stopped'\n" +
                "Error at line 5: Expected turnout ID for 'normal' but found 't1'.\n" +
                "  Line 5: 't1 forward -> t1 normal'\n" +
                "Error at line 7: Expected timer ID for 'start' but found 't1'.\n" +
                "  Line 7: 'block      -> t1 start'\n" +
                "Error at line 9: Expected NUM or ID argument for 't1' but found 'B42'.\n" +
                "  Line 9: 't1 forward -> t1 forward = B42'\n" +
                "Error at line 10: Expected NUM or ID argument for 't1' but found 'block'.\n" +
                "  Line 10: 't1 forward -> t1 forward = block'");
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

        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);

        // Execute with throttle defaulting to speed 0 (stopped)
        engine.onExecHandle();
        verify(mJmriThrottle).setSpeed(5);
        verify(mJmriThrottle, never()).setSpeed(0);
        assertThat(script.getThrottle("t1").getSpeed()).isEqualTo(5);

        // Execute with throttle at speed 5
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setSpeed(5);
        verify(mJmriThrottle).setSpeed(0);
        assertThat(script.getThrottle("t1").getSpeed()).isEqualTo(0);
    }

    @Test
    public void testStopForwardSequence() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "var speed = 5 \n " +
                "t1 stopped -> t1 forward = speed ; t1 stop \n" +
                "t1 forward -> t1 stop ; t1 forward = speed ";

        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);

        // Execute with throttle defaulting to speed 0 (stopped). Speed is set then reset.
        engine.onExecHandle();
        verify(mJmriThrottle).setSpeed(0);
        verify(mJmriThrottle).setSpeed(5);
        assertThat(script.getThrottle("t1").getSpeed()).isEqualTo(0);

        // Execute with throttle at speed 5
        script.getThrottle("t1").setSpeed(5);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle).setSpeed(0);
        verify(mJmriThrottle).setSpeed(5);
        assertThat(script.getThrottle("t1").getSpeed()).isEqualTo(5);
    }

    @Test
    public void testStopForwardSequence_MultiThrottle() throws Exception {
        String source = "" +
                "throttle T1 = 42 43 44 45 \n " +
                "var speed = 5 \n " +
                "t1 stopped -> t1 forward = speed ; t1 stop \n" +
                "t1 forward -> t1 stop ; t1 forward = speed ";

        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriThrottle throttle2 = mock(IJmriThrottle.class);
        IJmriThrottle throttle3 = mock(IJmriThrottle.class);
        IJmriThrottle throttle4 = mock(IJmriThrottle.class);
        IJmriThrottle throttle5 = mock(IJmriThrottle.class);
        when(mJmriProvider.getThrotlle(42)).thenReturn(throttle2);
        when(mJmriProvider.getThrotlle(43)).thenReturn(throttle3);
        when(mJmriProvider.getThrotlle(44)).thenReturn(throttle4);
        when(mJmriProvider.getThrotlle(45)).thenReturn(throttle5);

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);
        verify(mJmriProvider).getThrotlle(43);
        verify(mJmriProvider).getThrotlle(44);
        verify(mJmriProvider).getThrotlle(45);

        // Execute with throttle defaulting to speed 0 (stopped). Speed is set then reset.
        engine.onExecHandle();

        verify(throttle2).setSpeed(0);
        verify(throttle3).setSpeed(0);
        verify(throttle4).setSpeed(0);
        verify(throttle5).setSpeed(0);

        verify(throttle2).setSpeed(5);
        verify(throttle3).setSpeed(5);
        verify(throttle4).setSpeed(5);
        verify(throttle5).setSpeed(5);
        assertThat(script.getThrottle("t1").getSpeed()).isEqualTo(0);

        // Execute with throttle at speed 5
        script.getThrottle("t1").setSpeed(5);
        reset(throttle2);
        reset(throttle3);
        reset(throttle4);
        reset(throttle5);

        engine.onExecHandle();

        verify(throttle2).setSpeed(0);
        verify(throttle3).setSpeed(0);
        verify(throttle4).setSpeed(0);
        verify(throttle5).setSpeed(0);

        verify(throttle2).setSpeed(5);
        verify(throttle3).setSpeed(5);
        verify(throttle4).setSpeed(5);
        verify(throttle5).setSpeed(5);
        assertThat(script.getThrottle("t1").getSpeed()).isEqualTo(5);
    }

    @Test
    public void testThrottleFn() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "t1 stopped -> t1 f1 = 1 ; t1 f0     ; t1 f28 = 42 \n" +
                "t1 forward -> t1 f1     ; t1 f0 = 0 ; t1 f28 = 0  ";

        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);

        // Execute t1 stopped case
        engine.onExecHandle();
        verify(mJmriThrottle).triggerFunction(1, true);
        verify(mJmriThrottle).triggerFunction(0, false);
        verify(mJmriThrottle).triggerFunction(28, true);

        // Execute t1 forward case
        script.getThrottle("t1").setSpeed(5);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle).triggerFunction(1, false);
        verify(mJmriThrottle).triggerFunction(0, false);
        verify(mJmriThrottle).triggerFunction(28, false);
    }

    @Test
    public void testActionEnum() throws Exception {
        String source = "" +
                "throttle T1=42\n " +
                "Enum State = Init Idle Fwd Rev\n" +
                "Enum Other = Rev\n" +
                "state == init & t1 stopped -> state = idle\n" +
                "state == idle & t1 forward -> state = fwd \n" +
                "state == fwd  & t1 reverse -> state = other ";
        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);
        assertThat(script.getEnum("State").get()).isEqualTo("init");

        // Execute with throttle defaulting to speed 0 (stopped).
        engine.onExecHandle();
        assertThat(script.getEnum("State").get()).isEqualTo("idle");

        // Execute with throttle at speed 5
        script.getThrottle("t1").setSpeed(5);
        engine.onExecHandle();
        assertThat(script.getEnum("State").get()).isEqualTo("fwd");

        // Execute with throttle at speed -5
        script.getThrottle("t1").setSpeed(-5);
        engine.onExecHandle();
        assertThat(script.getEnum("State").get()).isEqualTo("rev");
    }

    @Test
    public void testActionVar() throws Exception {

        String source = "" +
                "throttle T1=42\n " +
                "var myVar=5\n " +
                "t1 stopped ->myVar=0\n" +
                "t1 forward ->myVar=1 ";

        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);
        assertThat(script.getVar("myvar").getAsInt()).isEqualTo(5);

        // Execute with throttle defaulting to speed 0 (stopped).
        engine.onExecHandle();
        assertThat(script.getVar("myvar").getAsInt()).isEqualTo(0);

        // Execute with throttle at speed 5
        script.getThrottle("t1").setSpeed(5);
        engine.onExecHandle();
        assertThat(script.getVar("myvar").getAsInt()).isEqualTo(1);
    }

    @Test
    public void testActionSound() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "t1 stopped -> t1 Sound=0 \n" +
                "t1 forward -> t1 Sound=1";

        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(mJmriProvider.getThrotlle(42)).thenReturn(throttle);

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);

        // Execute with throttle defaulting to speed 0 (stopped)
        engine.onExecHandle();
        verify(throttle).setSound(false);

        // Execute with throttle at forward speed
        reset(throttle);
        script.getThrottle("t1").setSpeed(5);
        engine.onExecHandle();
        verify(throttle).setSound(true);
    }

    @Test
    public void testActionLight() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "t1 stopped -> t1 Light=0 \n" +
                "t1 forward -> t1 Light=1";

        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);

        // Execute with throttle defaulting to speed 0 (stopped)
        engine.onExecHandle();
        verify(mJmriThrottle).setLight(false);

        // Execute with throttle at forward speed
        reset(mJmriThrottle);
        script.getThrottle("t1").setSpeed(5);
        engine.onExecHandle();
        verify(mJmriThrottle).setLight(true);
    }

    @Test
    public void testActionHorn() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "t1 stopped -> T1 Horn \n" +
                "t1 forward -> T1 Horn=1";

        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);

        // Execute with throttle defaulting to speed 0 (stopped)
        engine.onExecHandle();
        verify(mJmriThrottle).horn();

        // Execute with throttle at forward speed
        reset(mJmriThrottle);
        script.getThrottle("t1").setSpeed(5);
        engine.onExecHandle();
        verify(mJmriThrottle).horn();
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

        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriSensor sensor1 = mock(IJmriSensor.class);
        IJmriSensor sensor2 = mock(IJmriSensor.class);
        when(mJmriProvider.getSensor("NS42")).thenReturn(sensor1);
        when(mJmriProvider.getSensor("NS7805")).thenReturn(sensor2);

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);
        verify(mJmriProvider).getSensor("NS42");
        verify(mJmriProvider).getSensor("NS7805");

        when(sensor1.isActive()).thenReturn(false);
        when(sensor2.isActive()).thenReturn(false);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle).setLight(false);
        verify(mJmriThrottle, never()).setSound(anyBoolean());

        when(sensor1.isActive()).thenReturn(false);
        when(sensor2.isActive()).thenReturn(true);
        reset(mJmriThrottle);
        engine.onExecHandle();
        // Note: event !b1 is not executed a second time till the condition gets invalidated
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(anyBoolean());

        when(sensor1.isActive()).thenReturn(true);
        when(sensor2.isActive()).thenReturn(false);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle).setLight(true);
        verify(mJmriThrottle).setSound(false);

        when(sensor1.isActive()).thenReturn(true);
        when(sensor2.isActive()).thenReturn(true);
        reset(mJmriThrottle);
        engine.onExecHandle();
        // Note: event b1 is not executed a second time till the condition gets invalidated
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle).setSound(true);
    }

    @Test
    public void testActionMultiline() throws Exception {
        // Note: syntax if parser one for turnouts was "-> t1 = normal"
        // whereas new syntax is                       "-> t1 normal"
        String source = "" +
                "throttle th = 42 \n " +
                "turnout T1  = NT42 \n" +
                "turnout t2  = NT43 \n" +
                "th stopped -> \n" +
                "   T1 normal ; \n" +
                "   T2 reverse\n" +
                "th forward -> \n" +
                "   t1 reverse ; \n" +
                "   t2 normal \n" ;

        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriTurnout turnout1 = mock(IJmriTurnout.class);
        IJmriTurnout turnout2 = mock(IJmriTurnout.class);
        when(mJmriProvider.getTurnout("NT42")).thenReturn(turnout1);
        when(mJmriProvider.getTurnout("NT43")).thenReturn(turnout2);

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);
        verify(mJmriProvider).getTurnout("NT42");
        verify(mJmriProvider).getTurnout("NT43");

        // Throttle is stopped
        engine.onExecHandle();
        verify(turnout1).setTurnout(IJmriTurnout.NORMAL);
        verify(turnout2).setTurnout(IJmriTurnout.REVERSE);

        engine.onExecHandle();

        reset(turnout1);
        reset(turnout2);
        script.getThrottle("th").setSpeed(5);
        engine.onExecHandle();
        verify(turnout1).setTurnout(IJmriTurnout.REVERSE);
        verify(turnout2).setTurnout(IJmriTurnout.NORMAL);
    }

    @Test
    public void testActionTurnout() throws Exception {
        // Note: syntax if parser one for turnouts was "-> t1 = normal"
        // whereas new syntax is                       "-> t1 normal"
        String source = "" +
                "throttle th = 42 \n " +
                "turnout T1  = NT42 \n" +
                "turnout t2  = NT43 \n" +
                "th stopped -> \n" +
                "   T1 normal ; \n" +
                "   t2 reverse\n" +
                "th forward -> \n" +
                "   t1 reverse ; \n" +
                "   t2 normal \n" +
                " T1        -> th sound = 0 \n" +
                "!t2        -> th sound = 1 \n" ;

        Script script = mScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriTurnout turnout1 = mock(IJmriTurnout.class);
        IJmriTurnout turnout2 = mock(IJmriTurnout.class);
        when(mJmriProvider.getTurnout("NT42")).thenReturn(turnout1);
        when(mJmriProvider.getTurnout("NT43")).thenReturn(turnout2);

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);
        verify(mJmriProvider).getTurnout("NT42");
        verify(mJmriProvider).getTurnout("NT43");

        engine.onExecHandle();
        verify(turnout1).setTurnout(IJmriTurnout.NORMAL);
        verify(turnout2).setTurnout(IJmriTurnout.REVERSE);
        verify(mJmriThrottle).setSound(false);
        // Note: line "!t2->..." is not invoked at first because t2 was true in this onExecHandle call.
        // The "t2 = reverse" action will only be noticed at the next onExecHandle call.
        verify(mJmriThrottle, never()).setSound(true);

        engine.onExecHandle();
        verify(mJmriThrottle).setSound(true);

        reset(turnout1);
        reset(turnout2);
        script.getThrottle("th").setSpeed(5);
        engine.onExecHandle();
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

        Script script = mFakeNowScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mFakeNowScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();

        // throttle is stopped, starts t1
        assertThat(script.getTimer("t1").isActive()).isFalse();
        engine.onExecHandle();
        assertThat(script.getTimer("t1").isActive()).isFalse();

        // t1 is active 5 seconds later
        mNow.add(5*1000 - 1);
        engine.onExecHandle();
        assertThat(script.getTimer("t1").isActive()).isFalse();

        // Note: timer is still active because the "t1 ->" does not reset it with end yet.
        // A timer remains active till it is either restarted or ended.
        mNow.add(1);
        engine.onExecHandle();
        verify(mJmriThrottle).horn();
        verify(mJmriThrottle, never()).setSpeed(anyInt());
        assertThat(script.getTimer("t1").isActive()).isTrue();

        // t2 is active 2 seconds later. Both t1 gets reset as soon as t2 becomes active.
        mNow.add(2*1000);
        engine.onExecHandle();
        verify(mJmriThrottle).setSpeed(anyInt());
        assertThat(script.getTimer("t1").isActive()).isFalse();
        assertThat(script.getTimer("t2").isActive()).isTrue();
    }

    @Test
    public void testTimerNoReset() throws Exception {
        String source = "" +
                "throttle th = 42 \n " +
                "timer T2  = 2 # seconds \n" +
                "timer T5  = 5 \n" +
                "timer T9  = 9 \n" +
                "th stopped -> T2 start ; T5 start ; T9 start ; TH sound = 0 ; TH light = 0 \n" +
                "T2         -> th horn \n" +
                "T5         -> th sound = 1 \n" +
                "T9         -> th light = 1\n" ;

        Script script = mFakeNowScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mFakeNowScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();

        // throttle is stopped, starts t2, t5, t9
        engine.onExecHandle();
        assertThat(script.getTimer("t2").isActive()).isFalse();
        assertThat(script.getTimer("t5").isActive()).isFalse();
        assertThat(script.getTimer("t9").isActive()).isFalse();

        // t2 is active 5 seconds later
        mNow.add(2*1000);
        engine.onExecHandle();
        assertThat(script.getTimer("t2").isActive()).isTrue();
        verify(mJmriThrottle).horn();

        // t5 is active 3 seconds later
        mNow.add(3*1000);
        engine.onExecHandle();
        assertThat(script.getTimer("t5").isActive()).isTrue();
        verify(mJmriThrottle).setSound(true);

        // t9 is active 4 seconds later
        mNow.add(4*1000);
        engine.onExecHandle();
        assertThat(script.getTimer("t9").isActive()).isTrue();
        verify(mJmriThrottle).setLight(true);
    }

    @Test
    public void testTimerWithReset() throws Exception {
        String source = "" +
                "throttle th = 42 \n " +
                "timer T2  = 2 # seconds \n" +
                "timer T5  = 5 \n" +
                "timer T9  = 9 \n" +
                "th stopped -> T2 start ; T5 start ; T9 start ; TH sound = 0 ; TH light = 0 \n" +
                "T2         -> th horn ; Reset Timers \n " +
                "T5         -> th sound = 1 \n" +
                "T9         -> th light = 1\n" ;

        Script script = mFakeNowScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mFakeNowScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriThrottle throttle = mock(IJmriThrottle.class);
        when(mJmriProvider.getThrotlle(42)).thenReturn(throttle);
        engine.onExecStart();

        // throttle is stopped, starts t2, t5, t9
        engine.onExecHandle();
        assertThat(script.getTimer("t2").isActive()).isFalse();
        assertThat(script.getTimer("t5").isActive()).isFalse();
        assertThat(script.getTimer("t9").isActive()).isFalse();

        // t2 is active 5 seconds later and has just been reset
        mNow.add(2*1000);
        engine.onExecHandle();
        assertThat(script.getTimer("t2").isActive()).isFalse();
        verify(throttle).horn();

        // t5 is not executed 3 seconds later as it was reset
        mNow.add(3*1000);
        engine.onExecHandle();
        assertThat(script.getTimer("t5").isActive()).isFalse();
        verify(throttle, never()).setSound(true);

        // t9 is not executed 4 seconds later as it was reset
        mNow.add(4*1000);
        engine.onExecHandle();
        assertThat(script.getTimer("t9").isActive()).isFalse();
        verify(throttle, never()).setLight(true);
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    @Test
    public void testDelayedCondition() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "sensor b1 = NS42 \n" +
                "sensor b2 = NS7805 \n" +
                "!b1 + 2            -> T1 Light=0 \n" +
                " B1 + 3            -> t1 Light=1 \n" +
                " b1     & !b2+4  -> t1 Sound=0 \n" +
                " B1 + 5 &  b2+6  -> T1 Sound=1 ; \n" +
                " B1 + 5 &  b2+7  -> T1 Sound=0 ; \n" ;

        Script script = mFakeNowScriptComponent.getScriptParser2().parse(source);
        ExecEngine engine = mFakeNowScriptComponent.getScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriSensor sensor1 = mock(IJmriSensor.class);
        IJmriSensor sensor2 = mock(IJmriSensor.class);
        when(mJmriProvider.getSensor("NS42")).thenReturn(sensor1);
        when(mJmriProvider.getSensor("NS7805")).thenReturn(sensor2);

        engine.onExecStart();

        verify(mJmriProvider).getThrotlle(42);
        verify(mJmriProvider).getSensor("NS42");
        verify(mJmriProvider).getSensor("NS7805");

        // check virtual timers have been created
        assertThat(script.getTimer("$~b1$2$")).isNotNull();
        assertThat(script.getTimer( "$b1$3$")).isNotNull();
        assertThat(script.getTimer( "$b1$5$")).isNotNull();
        assertThat(script.getTimer("$~b2$4$")).isNotNull();
        assertThat(script.getTimer( "$b2$6$")).isNotNull();
        assertThat(script.getTimer( "$b2$7$")).isNotNull();

        // initial time ; !b1 and !b2 timers start counting from here unless reset.
        when(sensor1.isActive()).thenReturn(false);
        when(sensor2.isActive()).thenReturn(false);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(anyBoolean());

        // Line 1 : !b1 is active 2 seconds later
        mNow.add(2*1000 - 1);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(anyBoolean());

        mNow.add(1);
        engine.onExecHandle();
        verify(mJmriThrottle).setLight(false);
        verify(mJmriThrottle, never()).setSound(anyBoolean());


        // Line 3 : !b2 is active 4 seconds after the start.
        // However b1 is still negative so the line doesn't yet trigger.
        mNow.add(2 * 1000);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(anyBoolean());

        // Trigger b1. Note this is now "!b2 + 5" and thus does not trigger (because !b2 + 4
        // has auto-reset as soon as it became active).
        when(sensor1.isActive()).thenReturn(true);
        mNow.add(1 * 1000);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(false);

        // Line 2: b1 + 3 is now becoming active.
        mNow.add(3 * 1000);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle).setLight(true);
        verify(mJmriThrottle, never()).setSound(anyBoolean());

        // Line 4: b1 + 5 becomes active. But b2 is still negative so the line doesn't trigger.
        mNow.add(2 * 1000);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(anyBoolean());
        // Immediately after it triggers, a delayed timer is reset and ends
        assertThat(script.getTimer("$b1$5$").isActive()).isFalse();

        // Trigger b2
        when(sensor2.isActive()).thenReturn(true);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(anyBoolean());

        // Then skip 6 seconds. b2+6 becomes active. This is now 11 seconds after
        // b1 became active and b1+5 is no longer active since the delayed timer auto-resets.
        // Bottom line: we can't write a rule that uses 2 delayed timers.
        mNow.add(6 * 1000);
        assertThat(script.getTimer("$b1$5$").isActive()).isFalse();
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(true);

        // Line 5: b2+7 becomes active.
        // But since b1+5 has been reset as soon as it became active.
        mNow.add(1 * 1000);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(anyBoolean());
    }
}
