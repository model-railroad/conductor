package com.alfray.conductor.v2.script;

import com.alflabs.annotations.NonNull;
import com.alflabs.conductor.v2.Script2gLoader;
import com.alflabs.conductor.v2.script.IBlock;
import com.alflabs.conductor.v2.script.ISensor;
import com.alflabs.conductor.v2.script.impl.BaseVar;
import com.alflabs.conductor.v2.script.impl.Block;
import com.alflabs.conductor.v2.script.impl.IEvalRule;
import com.alflabs.conductor.v2.script.impl.MapInfo;
import com.alflabs.conductor.v2.script.RootScript;
import com.alflabs.conductor.v2.script.impl.Route;
import com.alflabs.conductor.v2.script.impl.Sensor;
import com.alflabs.conductor.v2.script.impl.SequenceInfo;
import com.alflabs.conductor.v2.script.impl.SequenceManager;
import com.alflabs.conductor.v2.script.impl.SequenceNode;
import com.alflabs.conductor.v2.script.impl.Throttle;
import com.alflabs.conductor.v2.script.impl.Timer;
import com.alflabs.conductor.v2.script.impl.Turnout;
import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.truth.Truth.assertThat;
import static java.util.stream.Collectors.toList;

public class ScriptTest2 {
    private Binding mBinding;
    private RootScript mScript;

    private String mScriptPrefix = "package v2.script\n" +
                "import com.alflabs.conductor.v2.script.RootScript\n" +
                "import groovy.transform.BaseScript\n" +
                "@BaseScript RootScript baseScript\n";


    @Before
    public void setUp() throws Exception {
    }

    @NonNull
    private void loadScriptFromFile(String scriptName) throws Exception {
        Script2gLoader loader = new Script2gLoader();
        loader.loadScriptFromFile(scriptName);
        mScript = loader.getScript().get();
        mBinding = loader.getBinding().get();
    }

    private void loadScriptFromText(String scriptText) throws Exception {
        loadScriptFromText("local", scriptText);
    }

    private void loadScriptFromText(String scriptName, String scriptText) throws Exception {
        Script2gLoader loader = new Script2gLoader();
        loader.loadScriptFromText(scriptName, scriptText);
        mScript = loader.getScript().get();
        mBinding = loader.getBinding().get();
    }

    @Test
    public void testVarBlock() throws Exception {
        loadScriptFromFile("sample_v2");

        assertThat(mBinding.getVariables()).containsKey("B310");
        assertThat(mBinding.getVariables()).containsKey("B311");
        assertThat(mBinding.getVariable("B310")).isInstanceOf(IBlock.class);
        assertThat(mBinding.getVariable("B311")).isInstanceOf(IBlock.class);

        assertThat(mScript.blocks()).containsKey("NS768");
        assertThat(mBinding.getVariable("B310")).isSameInstanceAs(mScript.blocks().get("NS768"));
        assertThat(mBinding.getVariable("B310")).isSameInstanceAs(mScript.block("NS768"));

        assertThat(((Block) mScript.blocks().get("NS768")).getVarName()).isEqualTo("B310");
        assertThat(((Block) mScript.blocks().get("B310")).getVarName()).isEqualTo("B310");
    }

    @Test
    public void testVarSensor() throws Exception {
        loadScriptFromFile("sample_v2");

        assertThat(mBinding.getVariables()).containsKey("Toggle");
        assertThat(mBinding.getVariable("Toggle")).isInstanceOf(Sensor.class);

        assertThat(mScript.sensors()).containsKey("NS829");
        assertThat(mBinding.getVariable("Toggle")).isSameInstanceAs(mScript.sensors().get("NS829"));
        assertThat(mBinding.getVariable("Toggle")).isSameInstanceAs(mScript.sensor("NS829"));

        assertThat(((Sensor) mScript.sensors().get("NS829")).getVarName()).isEqualTo("Toggle");
        assertThat(((Sensor) mScript.sensors().get("Toggle")).getVarName()).isEqualTo("Toggle");
    }

    @Test
    public void testVarTurnout() throws Exception {
        loadScriptFromFile("sample_v2");

        assertThat(mBinding.getVariables()).containsKey("T311");
        assertThat(mBinding.getVariable("T311")).isInstanceOf(Turnout.class);

        assertThat(mScript.turnouts()).containsKey("NT311");
        assertThat(mBinding.getVariable("T311")).isSameInstanceAs(mScript.turnouts().get("NT311"));
        assertThat(mBinding.getVariable("T311")).isSameInstanceAs(mScript.turnout("NT311"));

        assertThat(mScript.turnouts().get("NT311").getVarName()).isEqualTo("T311");
        assertThat(mScript.turnouts().get("T311").getVarName()).isEqualTo("T311");
    }

    @Test
    public void testVarThrottle() throws Exception {
        loadScriptFromFile("sample_v2");

        assertThat(mBinding.getVariables()).containsKey("Train1");
        assertThat(mBinding.getVariable("Train1")).isInstanceOf(Throttle.class);
        assertThat(mBinding.getVariable("Train1")).isSameInstanceAs(mScript.throttle(1001));
        assertThat(mScript.throttles()).containsKey(1001);

        assertThat(((Throttle) mBinding.getVariable("Train1")).getDccAddress()).isEqualTo(1001);
        assertThat(((Throttle) mBinding.getVariable("Train1")).getVarName()).isEqualTo("Train1");
    }

    @Test
    public void testVarTimer() throws Exception {
        loadScriptFromFile("sample_v2");

        assertThat(mBinding.getVariables()).containsKey("MyTimer1");
        assertThat(mBinding.getVariable("MyTimer1")).isInstanceOf(Timer.class);

        assertThat(((Timer) mBinding.getVariable("MyTimer1")).getDelay()).isEqualTo(5);
        assertThat(((Timer) mBinding.getVariable("MyTimer1")).getVarName()).isEqualTo("MyTimer1");
        assertThat(mScript.timers()).containsKey("MyTimer1");

        assertThat(mScript.timers()).containsKey("@timer@42");
        assertThat(mScript.timers().get("@timer@42").getDelay()).isEqualTo(42);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testVariables() throws Exception {
        loadScriptFromFile("sample_v2");

        // Check that no global variable is null (which typically indicates a getter method
        // lacks a return statement).
        assertThat(mBinding.getVariables().entrySet()
                .stream()
                .filter((Predicate<Map.Entry>) e -> e.getValue() == null)
                .toArray()).isEmpty();

        // Variables and declaration using "def".
        // - In Conductor 2, we are not going to _require_ block/sensor/turnouts objects to be
        //   declared with "def".
        // - Objects declared with "def" are local to the root script and not visible to the exec
        //   engine, nor exported (e.g. to the KV server). They are also not visible to inner local
        //   functions.
        // - Objects declared without "def" are global to the script binding and can be used in any
        //   scope. They are also visible to the exec engine and the KV server.
        assertThat(mBinding.getVariables().keySet()).containsAtLeast(
                "B310", "B311",
                "T311",
                "Toggle",
                "Train1", "Train2",
                "MyTimer1", "MyTimer2",
                "MyStringVar", "MyIntVar", "MyLongVar");
        assertThat(mBinding.getVariables().keySet()).doesNotContain("LocalVar1");
        assertThat(mBinding.getVariables().keySet()).doesNotContain("LocalVar2");
        assertThat(mBinding.getVariables().keySet()).doesNotContain("LocalVar3");

        assertThat(mBinding.getVariable("MyStringVar")).isInstanceOf(String.class);
        assertThat(mBinding.getVariable("MyStringVar")).isEqualTo("This string is exported. Value is 42");
        assertThat(mBinding.getVariable("MyIntVar")).isInstanceOf(Integer.class);
        assertThat(mBinding.getVariable("MyIntVar")).isEqualTo(42 + 42);
        assertThat(mBinding.getVariable("MyLongVar")).isInstanceOf(Long.class);
        assertThat(mBinding.getVariable("MyLongVar")).isEqualTo(44);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMapInfo() throws Exception {
        loadScriptFromFile("sample_v2");

        assertThat(mScript.maps()).containsExactly(
                "Mainline",
                new MapInfo("Mainline", "Map 1.svg"));

        assertThat(mBinding.getVariables().values()
                .stream().map(v -> v.getClass().getSimpleName()).toArray())
                .asList().doesNotContain("MapInfo");
    }

    // Verify that we don't leak implementation details... Currently, we do leak impl details
    // and this test fails and asserts improperly. Reason: even though sensor() returns an
    // ISensor interface, Groovy is highly dynamic and is able at runtime to figure that the
    // underlying implementation does contain the varName property which we don't want to expose.
    @Test(expected = AssertionError.class)
    public void testDontLeakImplementationDetails_BaseVars() throws Exception {
        // This should fail: script should not have access to implementation
        // details such as BaseVar getVarName..
        MissingPropertyException thrown = Assert.assertThrows(MissingPropertyException.class,
                () -> {
                    loadScriptFromText(mScriptPrefix +
                            "def Sensor1 = sensor  \"S01\" \n" +
                            "println \"varName is ${Sensor1.varName}\"\n"
                    );

                    ISensor sensor1 = mScript.sensors().get("S01");
                    assertThat(sensor1).isNotNull();
                    mScript.executeRules();
                });

        assertThat(thrown.getMessage()).contains(
                "No such property: varName for class: com.alflabs.conductor.v2.script.ISensor");
    }

    // Verify that we don't leak implementation details... Currently, we do leak impl details
    // and this test fails and asserts improperly. Reason: even though sensor() returns an
    // ISensor interface, Groovy is highly dynamic and is able at runtime to figure that the
    // underlying implementation does contain the evaluateCondition which we don't want to expose.
    @Test(expected = AssertionError.class)
    public void testDontLeakImplementationDetails_Rules() throws Exception {
        // This should fail: script should not have access to implementation
        // details such as evaluateCondition or evaluateAction.
        Exception thrown = Assert.assertThrows(Exception.class,
                () -> {
                    loadScriptFromText(mScriptPrefix +
                            "def Sensor1  = sensor  \"S01\" \n" +
                            "on {  Sensor1 } then { this.evaluateCondition() } \n" +
                            "on { !Sensor1 } then { this.evaluateAction(null) } \n"
                    );

                    assertThat(mScript.rules().size()).isEqualTo(2);
                    Sensor sensor1 = (Sensor) mScript.sensors().get("S01");
                    assertThat(sensor1).isNotNull();

                    sensor1.setActive(false);
                    mScript.executeRules();

                    sensor1.setActive(true);
                    mScript.executeRules();
                });

        assertThat(thrown.getMessage()).contains(
                "No such property: evaluateCondition");
    }

    @Test
    public void testRuleTurnout() throws Exception {
        loadScriptFromText(mScriptPrefix +
                "Turnout1 = turnout \"NT1\" \n" +
                "def Sensor1  = sensor  \"S01\" \n" +
                "def ResetTurnouts() { Turnout1.reverse() } \n" +
                "on {  Sensor1 } then { Turnout1.normal()  } \n" +
                "on { !Sensor1 } then { ResetTurnouts() } \n"
        );

        assertThat(mScript.rules().size()).isEqualTo(2);

        Turnout turnout1 = mScript.turnouts().get("NT1");
        Sensor sensor1 = (Sensor) mScript.sensors().get("S01");

        assertThat(sensor1.isActive()).isFalse();
        assertThat(turnout1.isNormal()).isTrue();

        sensor1.setActive(true);
        mScript.executeRules();
        assertThat(turnout1.isNormal()).isTrue();

        sensor1.setActive(false);
        mScript.executeRules();
        assertThat(turnout1.isNormal()).isFalse();
    }

    @Test
    public void testRuleThrottle() throws Exception {
        loadScriptFromText(mScriptPrefix +
                "Train1  = throttle 1001 \n" +
                "Train2  = throttle 1002 \n" +
                "Sensor1 = sensor  \"S01\" \n" +
                "Sensor2 = sensor  \"S02\" \n" +
                // Syntax using an action as a function
                "on { !Sensor1 } then { Train1.stop()  } \n" +
                "on {  Sensor1 &&  Sensor2 } then { Train1.forward(5) } \n" +
                "on {  Sensor1 && !Sensor2 } then { Train1.reverse(7) } \n" +
                "on { Train1.forward } then { Train1.light(true); Train1.horn(); Train1.F1(true) } \n" +
                "on { Train1.stopped } then { Train1.light(false); Train1.horn(); Train1.F1(false) } \n" +
                // Syntax using an action setter property (not a function) + getter (for condition)
                // Only "forward" has this, as an example it is possible, and why it's confusing.
                "on { Train1.forward } then { Train2.forward = 42 } \n" +
                "on { Train1.reverse } then { Train2.reverse(43) } \n" +
                // Stop must be a function as it has no value, it cannot be a property.
                "on { Train1.stopped } then { Train2.stop() } \n"
        );

        assertThat(mScript.rules().size()).isEqualTo(8);

        Throttle train1 = mScript.throttleByName("Train1").get();
        Throttle train2 = mScript.throttleByName("Train2").get();
        Sensor sensor1 = (Sensor) mScript.sensors().get("S01");
        Sensor sensor2 = (Sensor) mScript.sensors().get("S02");

        assertThat(train1.getSpeed()).isEqualTo(0);
        assertThat(train2.getSpeed()).isEqualTo(0);

        sensor1.setActive(false);
        mScript.executeRules();
        assertThat(train1.getSpeed()).isEqualTo(0);
        assertThat(train1.isLight()).isEqualTo(false);
        assertThat(train1.isF1()).isEqualTo(false);
        assertThat(train2.getSpeed()).isEqualTo(0);

        // Note: actions are always executed after all conditions are checked. Thus
        // changing the throttle speed does _not_ change conditions in same loop,
        // it only changes conditions in the next loop. This ensures eval consistency.

        sensor1.setActive(true);
        sensor2.setActive(true);
        mScript.executeRules();
        assertThat(train1.getSpeed()).isEqualTo(5);
        assertThat(train2.getSpeed()).isEqualTo(0);
        // train1.forward condition is not active yet until the next execution pass.
        mScript.executeRules();
        assertThat(train1.isLight()).isEqualTo(true);
        assertThat(train1.isF1()).isEqualTo(true);
        assertThat(train2.getSpeed()).isEqualTo(42);

        sensor1.setActive(true);
        sensor2.setActive(false);
        mScript.executeRules();
        assertThat(train1.getSpeed()).isEqualTo(-7);
        mScript.executeRules();
        assertThat(train2.getSpeed()).isEqualTo(-43);

        sensor1.setActive(false);
        mScript.executeRules();
        assertThat(train1.getSpeed()).isEqualTo(0);
        mScript.executeRules();
        assertThat(train2.getSpeed()).isEqualTo(0);
    }

    @Test
    public void testRoute() throws Exception {
        loadScriptFromText(mScriptPrefix +
                "Train1  = throttle 1001 \n" +
                "Block1 = block \"B01\" \n" +
                "Route_Idle = route idle() \n" +
                "Route_Seq = route sequence { \n" +
                "throttle = Train1 \n" +
                "timeout = 42 \n" +
                "node1 = node(Block1) { } \n" +
                "def node2 = node(Block1) { } \n" +
                "nodes = [ [ node1, node2 ], [ node2, node1  ] ] \n" +
                "} \n" +
                "Routes = activeRoute { routes = [ Route_Idle, Route_Seq ] } \n"
        );

        assertThat(mScript.rules().size()).isEqualTo(0);
        Throttle train1 = mScript.throttleByName("Train1").get();
        IBlock block1 = mScript.blocks().get("B01");

        assertThat(mScript.routes()).containsKey("Route_Idle");
        assertThat(mScript.routes()).containsKey("Route_Seq");
        assertThat(mScript.activeRoutes()).containsKey("Routes");
        assertThat(
                Arrays.stream(mScript.activeRoutes().get("Routes").getRoutes())
                        .map(BaseVar::getVarName).collect(toList()))
                .containsExactly("Route_Idle", "Route_Seq");
    }

    @Test
    public void testRouteSequence_OnActivate() throws Exception {
        loadScriptFromFile("sample_v2");
        Throttle train1 = mScript.throttleByName("Train1").get();

        assertThat(mScript.routes()).containsKey("Route1");
        Route seq = mScript.routes().get("Route1");

        assertThat(seq.getManager()).isInstanceOf(SequenceManager.class);
        SequenceManager seqMan = (SequenceManager) seq.getManager();

        assertThat(seqMan.getSequenceInfo()).isNotNull();
        SequenceInfo seqInfo = seqMan.getSequenceInfo();

        assertThat(seqInfo.getThrottle().isPresent()).isTrue();
        assertThat(seqInfo.getThrottle().get()).isSameInstanceAs(train1);
        assertThat(seqInfo.getNodes()).hasSize(2);

        assertThat(seqInfo.getOnActivateRule().isPresent()).isTrue();
        IEvalRule onActivateRule = seqInfo.getOnActivateRule().get();
        assertThat(train1.isLight()).isEqualTo(false);
        onActivateRule.evaluateAction(mScript);
        assertThat(train1.isLight()).isEqualTo(true);
    }

    @Test
    public void testRouteSequence_Nodes() throws Exception {
        loadScriptFromFile("sample_v2");
        Throttle train1 = mScript.throttleByName("Train1").get();

        SequenceInfo seqInfo =
                ((SequenceManager) mScript.routes().get("Route1").getManager()).getSequenceInfo();
        SequenceNode node = seqInfo.getNodes().get(0).get(0);
        assertThat(node).isNotNull();

        assertThat(train1.getSpeed()).isEqualTo(0);
        assertThat(train1.isLight()).isEqualTo(false);

        assertThat(node.getEvents().getOnEnterRule().isPresent()).isTrue();
        assertThat(node.getEvents().getWhileOccupiedRule().isPresent()).isTrue();
        assertThat(node.getEvents().getOnTrailingRule().isPresent()).isTrue();
        assertThat(node.getEvents().getOnEmptyRule().isPresent()).isTrue();

        node.getEvents().getOnEnterRule().get().evaluateAction(mScript);
        assertThat(train1.getSpeed()).isEqualTo(5);
        assertThat(train1.isLight()).isEqualTo(false);

        node.getEvents().getWhileOccupiedRule().get().evaluateAction(mScript);
        assertThat(train1.getSpeed()).isEqualTo(5);
        assertThat(train1.isLight()).isEqualTo(true);

        node.getEvents().getOnTrailingRule().get().evaluateAction(mScript);
        assertThat(train1.getSpeed()).isEqualTo(10);
        assertThat(train1.isLight()).isEqualTo(true);

        node.getEvents().getOnEmptyRule().get().evaluateAction(mScript);
        assertThat(train1.getSpeed()).isEqualTo(10);
        assertThat(train1.isLight()).isEqualTo(false);
    }

    @Test
    public void testRouteSequence_Nodes_NoDirectCommands() {
        Exception thrown = Assert.assertThrows(Exception.class,
                () -> {
                    loadScriptFromText(mScriptPrefix +
                            "Train1  = throttle 1001 \n" +
                            "Block1 = block \"B01\" \n" +
                            "Route_Seq = route sequence { \n" +
                            "throttle = Train1 \n" +
                            "timeout = 42 \n" +
                            "node1 = node(Block1) { Train1.horn() } \n" +
                            "nodes = [ [ node1, node1 ] ] \n" +
                            "} \n");
                });

        assertThat(thrown.getMessage()).contains(
          "No such property: Train1 for class: com.alflabs.conductor.v2.script.impl.SequenceNodeEvents");
    }

    @Test
    public void testAfterThen() throws Exception {
        loadScriptFromText(mScriptPrefix +
                "Train1 = throttle 1001 \n" +
                "after(timer(1)) then { Train1.forward(1) \n" +
                "} and_after(timer(2)) then { Train1.forward(2) \n" +
                "} and_after(timer(3)) then { Train1.forward(3) } \n"
        );

        assertThat(mScript.rules().size()).isEqualTo(3);
        Throttle train1 = mScript.throttleByName("Train1").get();

        assertThat(mScript.timers().keySet()).containsAtLeast(
                "@timer@1",
                "@timer@1_@timer@2",
                "@timer@1_@timer@2_@timer@3");

        mScript.executeRules();
        assertThat(train1.getSpeed()).isEqualTo(0);

        mScript.timers().get("@timer@1").setActive(true);
        mScript.executeRules();
        assertThat(train1.getSpeed()).isEqualTo(1);

        mScript.timers().get("@timer@1").setActive(false);
        mScript.timers().get("@timer@1_@timer@2").setActive(true);
        mScript.executeRules();
        assertThat(train1.getSpeed()).isEqualTo(2);

        mScript.timers().get("@timer@1_@timer@2").setActive(false);
        mScript.timers().get("@timer@1_@timer@2_@timer@3").setActive(true);
        mScript.executeRules();
        assertThat(train1.getSpeed()).isEqualTo(3);
    }
}

