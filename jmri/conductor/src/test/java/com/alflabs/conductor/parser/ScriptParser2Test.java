/*
 * Project: Conductor
 * Copyright (C) 2017 alf.labs gmail com,
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

package com.alflabs.conductor.parser;

import com.alflabs.conductor.ConductorModule;
import com.alflabs.conductor.DaggerIConductorComponent;
import com.alflabs.conductor.IConductorComponent;
import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriSensor;
import com.alflabs.conductor.IJmriThrottle;
import com.alflabs.conductor.IJmriTurnout;
import com.alflabs.conductor.script.Enum_;
import com.alflabs.conductor.script.ExecEngine;
import com.alflabs.conductor.script.IScriptComponent;
import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.script.ScriptModule;
import com.alflabs.conductor.script.Timer;
import com.alflabs.conductor.script.Var;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Constants;
import com.alflabs.manifest.MapInfo;
import com.alflabs.manifest.RouteInfo;
import com.alflabs.rx.IStream;
import com.alflabs.rx.Schedulers;
import com.alflabs.rx.Streams;
import com.alflabs.utils.FakeClock;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

    private final IStream<String> mChangedStream = Streams.<String>stream().on(Schedulers.sync());

    @Mock IJmriProvider mJmriProvider;
    @Mock IJmriThrottle mJmriThrottle;
    @Mock IKeyValue mKeyValue;
    @Mock FileOps mFileOps;

    private FakeClock mClock;
    private TestReporter mReporter;
    private IScriptComponent mScriptComponent;
    private IScriptComponent mFakeNowScriptComponent;

    @Before
    public void setUp() throws Exception {
        when(mKeyValue.getChangedStream()).thenReturn(mChangedStream);
        when(mJmriProvider.getThrotlle(42)).thenReturn(mJmriThrottle);

        // Enable the ExecEngine by default.
        when(mKeyValue.getValue(Constants.EStopKey)).thenReturn(Constants.EStopState.NORMAL.toString());

        mReporter = new TestReporter();

        File file = File.createTempFile("conductor_tests", "tmp");
        file.deleteOnExit();

        IConductorComponent realNowComponent = DaggerIConductorComponent.builder()
                .conductorModule(new ConductorModule(mJmriProvider) {
                    @Override
                    public FileOps provideFileOps() {
                        return mFileOps;
                    }
                })
                .scriptFile(file)
                .build();

        mScriptComponent = realNowComponent.newScriptComponent(
                new ScriptModule(mReporter, mKeyValue));

        mClock = new FakeClock(1000);

        IConductorComponent fakeNowComponent = DaggerIConductorComponent.builder()
                .conductorModule(new ConductorModule(mJmriProvider) {
                    @Override
                    public IClock provideClock() {
                        return mClock;
                    }

                    @Override
                    public FileOps provideFileOps() {
                        return mFileOps;
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
                "Int id = 1\n" +
                "Int _  = 2\n" +
                "Int My-Var = 3\n" +
                "int id2=4\n" +
                "int __id3__=5\n";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("id").getAsInt()).isEqualTo(1);
        assertThat(script.getVar("_").getAsInt()).isEqualTo(2);
        assertThat(script.getVar("my-var").getAsInt()).isEqualTo(3);
        assertThat(script.getVar("id2").getAsInt()).isEqualTo(4);
        assertThat(script.getVar("__id3__").getAsInt()).isEqualTo(5);
    }

    @Test
    public void testDefineInt() throws Exception {
        String source = "  Int VALUE    = 5201 # d&rgw ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("value")).isNotNull();
        Var var = script.getVar("Value");
        assertThat(var.getAsInt()).isEqualTo(5201);
        assertThat(var.isExported()).isFalse();
        assertThat(var.isImported()).isFalse();
    }

    @Test
    public void testDefineExportInt() throws Exception {
        String source = " Export Int VALUE    = 5201 # d&rgw ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("value")).isNotNull();
        Var var = script.getVar("Value");
        assertThat(var.getAsInt()).isEqualTo(5201);
        assertThat(var.isExported()).isTrue();
        assertThat(var.isImported()).isFalse();
    }

    @Test
    public void testDefineImportExportInt() throws Exception {
        String source = " Import Export Int VALUE    = 5201 # d&rgw ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("value")).isNotNull();
        Var var = script.getVar("Value");
        assertThat(var.getAsInt()).isEqualTo(5201);
        assertThat(var.isExported()).isTrue();
        assertThat(var.isImported()).isTrue();
    }

    @Test
    public void testDefineInt_missingId() throws Exception {
        String source = "  Int = 5201 ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: missing ID at '='.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineInt_alreadyDefined() throws Exception {
        String source = "" +
                "  INT VALUE    = 5201 \n " +
                "int value = 42";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo(
                "Error at line 2: Name 'value' is already defined.\n" +
                "  Line 2: 'int value = 42'");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineIncInt() throws Exception {
        String source = "" +
                " Sensor A = NS1 \n" +
                " Sensor B = NS2 \n" +
                "  Int VALUE  = 5201 # d&rgw \n" +
                " A -> value += 1 \n" +
                " A -> value -= 12 \n" +
                " B -> value += value ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("value")).isNotNull();
        Var var = script.getVar("Value");
        assertThat(var.getAsInt()).isEqualTo(5201);
    }

    @Test
    public void testDefineIncInt_missingId() throws Exception {
        String source = "" +
                " Sensor A = NS1 \n" +
                " Sensor B = NS2 \n" +
                "  Int VALUE  = 5201 # d&rgw \n" +
                " A -> value += 42.43 \n" +
                " A -> value += -12 \n" +  // Note negative values aren't parsed yet
                " B -> value -= invalid-id ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo(
                "Error at line 4: extraneous input '.43' expecting {<EOF>, EOL, SB_COMMENT, ';'}.\n" +
                "Error at line 5: token recognition error at: '-1'.\n" +
                "Error at line 4: Unexpected symbol: '.43'.\n" +
                "  Line 4: 'A -> value += 42.43'\n" +
                "Error at line 6: Expected NUM or ID argument for 'value' but found 'invalid-id'.\n" +
                "  Line 6: 'B -> value -= invalid-id'");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineString() throws Exception {
        String source = "  String VALUE    = \"5201 # d&rgw\" ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("value")).isNotNull();
        Var var = script.getVar("Value");
        assertThat(var.get()).isEqualTo("5201 # d&rgw");
        assertThat(var.isExported()).isFalse();
        assertThat(var.isImported()).isFalse();
    }

    @Test
    public void testDefineExportString() throws Exception {
        String source = " Export String VALUE    = \"5201 # d&rgw\" ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("value")).isNotNull();
        Var var = script.getVar("Value");
        assertThat(var.get()).isEqualTo("5201 # d&rgw");
        assertThat(var.isExported()).isTrue();
        assertThat(var.isImported()).isFalse();
    }

    @Test
    public void testDefineImportExportString() throws Exception {
        String source = " Import Export String VALUE    = \"5201 # d&rgw\" ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("value")).isNotNull();
        Var var = script.getVar("Value");
        assertThat(var.get()).isEqualTo("5201 # d&rgw");
        assertThat(var.isExported()).isTrue();
        assertThat(var.isImported()).isTrue();
    }

    @Test
    public void testDefineString_MultiLine() throws Exception {
        String source = "  String SingleLineString = \"This is a free string. It is not used in the script.\"\n" +
                "    String MultiLineString = '''This string can\n" +
                "    be split on as many lines as needed. It cannot\n" +
                "    however contain any \"single-quotes\" (for grammar\n" +
                "    simplification).''' ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("SingleLineString")).isNotNull();
        assertThat(script.getVar("SingleLineString").get()).contains("free string");
        assertThat(script.getVar("SingleLineString").get()).doesNotContain("\"");
        assertThat(script.getVar("MultiLineString")).isNotNull();
        assertThat(script.getVar("MultiLineString").get()).contains("many lines");
        assertThat(script.getVar("MultiLineString").get()).doesNotContain("'''");
    }

    @Test
    public void testDefineString_missingId() throws Exception {
        String source = "  String = \"Address 5201\" ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: missing ID at '='.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineString_alreadyDefined() throws Exception {
        String source = "" +
                "  String VALUE    = \"Address 5201\" \n " +
                "String value = \"Address 42\"";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo(
                "Error at line 2: Name 'value' is already defined.\n" +
                        "  Line 2: 'String value = \"Address 42\"'");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineSensor() throws Exception {
        String source = "  Sensor Alias   = NS784 ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getSensor("alias")).isNotNull();
    }

    @Test
    public void testDefineSensor_alreadyDefined() throws Exception {
        String source = "" +
                "Sensor Alias   = NS784 \n " +
                "sensor alias   = B42";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo(
                "Error at line 2: Name 'alias' is already defined.\n" +
                "  Line 2: 'sensor alias   = B42'");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineSensor_invalidValue() throws Exception {
        String source = "sensor alias   = 42";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: mismatched input '42' expecting ID.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineTurnout() throws Exception {
        String source = "  Turnout TT   = NS784 ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getTurnout("tt")).isNotNull();
    }

    @Test
    public void testDefineThrottle() throws Exception {
        String source = "  Throttle TH   = 5201 ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getThrottle("th")).isNotNull();
    }

    @Test
    public void testDefineThrottle_invalidDccAddress() throws Exception {
        String source = "  Throttle TH   = Block42 ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: mismatched input 'Block42' expecting NUM.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineMultiThrottle() throws Exception {
        String source = "  Throttle TH   = 5201 5202 5203 5204 ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getThrottle("th")).isNotNull();
    }

    @Test
    public void testDefineMultiThrottle_invalidDccAddress() throws Exception {
        String source = "  Throttle TH   = 5201 5202 Block42 5203 5204 ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("Error at line 1: extraneous input 'Block42' expecting {<EOF>, EOL, SB_COMMENT, NUM}.");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineTimer() throws Exception {
        String source = "  Timer Timer-1 = 5 ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getTimer("TIMER-1")).isNotNull();
        Timer timer = script.getTimer("timer-1");
        assertThat(timer.getDurationSec()).isEqualTo(5);
    }

    @Test
    public void testDefineEnum() throws Exception {
        String source = "  Enum EN   = Init Idle Fwd Rev ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        Enum_ enum_ = script.getEnum("en");
        assertThat(enum_).isNotNull();
        assertThat(enum_.getValues().toArray()).isEqualTo(
                new String[] { "init", "idle", "fwd", "rev" });
        assertThat(enum_.isExported()).isFalse();
        assertThat(enum_.isImported()).isFalse();
    }

    @Test
    public void testDefineExportedEnum() throws Exception {
        String source = " Export Enum EN   = Init Idle Fwd Rev ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        Enum_ enum_ = script.getEnum("en");
        assertThat(enum_).isNotNull();
        assertThat(enum_.getValues().toArray()).isEqualTo(
                new String[] { "init", "idle", "fwd", "rev" });
        assertThat(enum_.isExported()).isTrue();
        assertThat(enum_.isImported()).isFalse();
    }

    @Test
    public void testDefineImportExportEnum() throws Exception {
        String source = " Import Export Enum EN   = Init Idle Fwd Rev ";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        Enum_ enum_ = script.getEnum("en");
        assertThat(enum_).isNotNull();
        assertThat(enum_.getValues().toArray()).isEqualTo(
                new String[] { "init", "idle", "fwd", "rev" });
        assertThat(enum_.isExported()).isTrue();
        assertThat(enum_.isImported()).isTrue();
    }

    @Test
    public void testDefineEnum_alreadyDefined() throws Exception {
        String source = "" +
                "Enum EN   = Init Idle Fwd Rev \n" +
                "Enum EN   = Init Idle";
        Script script = mScriptComponent.createScriptParser2().parse(source);

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

        when(mFileOps.isFile(new File("path/to/map1.svg"))).thenReturn(true);
        when(mFileOps.toString(new File("path/to/map1.svg"), Charsets.UTF_8)).thenReturn("<svg1/>");

        when(mFileOps.isFile(new File("path\\to\\map2.svg"))).thenReturn(true);
        when(mFileOps.toString(new File("path\\to\\map2.svg"), Charsets.UTF_8)).thenReturn("<svg2/>");

        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        TreeMap<String, MapInfo> maps = script.getMaps();
        assertThat(maps).hasFirstEntry("map-1", new MapInfo("Map-1", "<svg1/>"));
        assertThat(maps).hasLastEntry ("map-2", new MapInfo("Map-2", "<svg2/>"));
        assertThat(maps.get("map-1").toString()).isEqualTo("MapInfo{name='Map-1', svg='<svg1/>'}");
        assertThat(maps.get("map-2").toString()).isEqualTo("MapInfo{name='Map-2', svg='<svg2/>'}");
        assertThat(maps).hasSize(2);
    }

    @Test
    public void testDefineMap_alreadyDefined() throws Exception {
        String source = "" +
                "  Map Map-1 = \"path/to/map1.svg\" \n" +
                "  Map Map-1 = \"path\\to\\map2.svg\" ";

        when(mFileOps.isFile(new File("path/to/map1.svg"))).thenReturn(true);
        when(mFileOps.toString(new File("path/to/map1.svg"), Charsets.UTF_8)).thenReturn("<svg1/>");

        when(mFileOps.isFile(new File("path\\to\\map2.svg"))).thenReturn(true);
        when(mFileOps.toString(new File("path\\to\\map2.svg"), Charsets.UTF_8)).thenReturn("<svg2/>");

        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo(
                "Error at line 2: Name 'Map-1' is already defined.\n" +
                "  Line 2: 'Map Map-1 = \"path\\to\\map2.svg\"'");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineRoute_AndExecEngineStart() throws Exception {
        String source = "" +
                "Throttle PA-100  = 100\n" +
                "Throttle BLT-200 = 200\n" +
                "Enum PA-Status   = INIT IDLE FWD\n" +
                "Int  BL-Status   = 300\n" +
                "Int  PA-Counter  = 1\n" +
                "Int  BL-Counter  = 2\n" +
                "Sensor PA-Toggle = NS420\n" +
                "Sensor BL-Toggle = NS430\n" +
                "Route Passenger  = Throttle: PA-100, Status: PA-Status, Counter: PA-Counter, Toggle: PA-Toggle\n" +
                "Route Branchline=toggle:bl-toggle,status:bl-status,counter:bl-counter,throttle:blt-200\n";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        TreeMap<String, RouteInfo> routes = script.getRoutes();
        assertThat(routes).hasFirstEntry("branchline",
                new RouteInfo("Branchline", "S/bl-toggle", "V/bl-status", "V/bl-counter", "D/200"));
        assertThat(routes).hasLastEntry("passenger"  ,
                new RouteInfo("Passenger" , "S/pa-toggle", "V/pa-status", "V/pa-counter", "D/100"));
        assertThat(routes).hasSize(2);

        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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

        ArrayList<String> sortedKeys = new ArrayList<>(keyCapture.getAllValues());
        ArrayList<String> sortedValues = new ArrayList<>(valueCapture.getAllValues());
        Collections.sort(sortedKeys);
        Collections.sort(sortedValues);

        assertThat(sortedKeys.toArray()).isEqualTo(new String[]
                { "D/100", "D/200",
                        "M/maps", "R/routes",
                        "S/bl-toggle", "S/pa-toggle",
                        "V/$estop-state$",
                        "V/bl-counter", "V/bl-status",
                        "V/pa-counter", "V/pa-status" });

        assertThat(sortedValues.toArray()).isEqualTo(new String[]
                { "0", "0", "1", "2", "300", "NORMAL", "OFF", "OFF", "init",
                        "{\"mapInfos\":[]}",
                        "{\"routeInfos\":[" +
                                "{\"name\":\"Branchline\",\"toggleKey\":\"S/bl-toggle\",\"statusKey\":\"V/bl-status\",\"counterKey\":\"V/bl-counter\",\"throttleKey\":\"D/200\"}," +
                                "{\"name\":\"Passenger\",\"toggleKey\":\"S/pa-toggle\",\"statusKey\":\"V/pa-status\",\"counterKey\":\"V/pa-counter\",\"throttleKey\":\"D/100\"}]}" });
    }

    @Test
    public void testDefineRoute_alreadyDefined() throws Exception {
        String source = "" +
                "Throttle PA-100  = 100\n" +
                "Throttle BLT-200 = 200\n" +
                "Enum PA-Status   = INIT IDLE FWD\n" +
                "Int  BL-Status   = 300\n" +
                "Int  PA-Counter  = 1\n" +
                "Int  BL-Counter  = 2\n" +
                "Sensor PA-Toggle = NS420\n" +
                "Sensor BL-Toggle = NS430\n" +
                "Route Passenger  = Throttle: PA-100, Status: PA-Status, Counter: PA-Counter, Toggle: PA-Toggle\n" +
                "Route Passenger  = toggle:bl-toggle,status:bl-status,counter:bl-counter,throttle:blt-200\n";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo(
                "Error at line 10: Name 'Passenger' is already defined.\n" +
                "  Line 10: 'Route Passenger  = toggle:bl-toggle,status:bl-status,counter:bl-counter,throttle:blt-200'");
        assertThat(script).isNotNull();
    }

    @Test
    public void testDefineRoute_MissingThrottle() throws Exception {
        String source = "" +
                "Enum PA-Status   = INIT IDLE FWD\n" +
                "Sensor PA-Toggle = NS420\n" +
                "Route Passenger  = Throttle: PA-100, Status: PA-Status, Counter: PA-Counter, Toggle: PA-Toggle\n";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 3: Route 'Passenger': Id 'PA-100' for argument 'throttle' is not defined.\n" +
                "  Line 3: 'Route Passenger  = Throttle: PA-100, Status: PA-Status, Counter: PA-Counter, Toggle: PA-Toggle'");
        assertThat(script).isNotNull();

        TreeMap<String, RouteInfo> routes = script.getRoutes();
        assertThat(routes).isNotNull();
        assertThat(routes).isEmpty();
    }

    @Test
    public void testDefineRoute_MissingStatus() throws Exception {
        String source = "" +
                "Throttle PA-100  = 100\n" +
                "Int  PA-Counter  = 1\n" +
                "Sensor PA-Toggle = NS420\n" +
                "Route Passenger  = Throttle: PA-100, Status: PA-Status, Counter: PA-Counter, Toggle: PA-Toggle\n";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 4: Route 'Passenger': Id 'PA-Status' for argument 'status' is not defined.\n" +
                "  Line 4: 'Route Passenger  = Throttle: PA-100, Status: PA-Status, Counter: PA-Counter, Toggle: PA-Toggle'");
        assertThat(script).isNotNull();

        TreeMap<String, RouteInfo> routes = script.getRoutes();
        assertThat(routes).isNotNull();
        assertThat(routes).isEmpty();
    }

    @Test
    public void testDefineRoute_MissingToggle() throws Exception {
        String source = "" +
                "Throttle PA-100  = 100\n" +
                "Int  PA-Counter  = 1\n" +
                "Enum PA-Status   = INIT IDLE FWD\n" +
                "Route Passenger  = Throttle: PA-100, Status: PA-Status, Counter: PA-Counter, Toggle: PA-Toggle\n";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 4: Route 'Passenger': Id 'PA-Toggle' for argument 'toggle' is not defined.\n" +
                "  Line 4: 'Route Passenger  = Throttle: PA-100, Status: PA-Status, Counter: PA-Counter, Toggle: PA-Toggle'");
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
                "Int  PA-Counter  = 1\n" +
                "Sensor PA-Toggle = NS420\n" +
                "Sensor MyToggle  = NS421\n" +
                "Route Passenger  = Toggle: MyToggle, Throttle: PA-100, Status: PA-Status, Counter: PA-Counter, Toggle: PA-Toggle\n";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("" +
                "Error at line 6: Route 'Passenger': Argument 'toggle' is already defined.\n" +
                "  Line 6: 'Route Passenger  = Toggle: MyToggle, Throttle: PA-100, Status: PA-Status, Counter: PA-Counter, Toggle: PA-Toggle'");
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
                "Int My-Var=1\n" +
                "my-var->my-var=2\n";
        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        assertThat(script.getVar("my-var").getAsInt()).isEqualTo(1);
    }

    @Test
    public void testEnumDefineCondAction() throws Exception {
        String source = "" +
                "Enum State = Init Idle Fwd Rev\n" +
                "State == INIT -> State = Idle\n";
        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();

        assertThat(script.getEnum("State").get()).isEqualTo("init");

        engine.onExecHandle();

        assertThat(script.getEnum("State").get()).isEqualTo("idle");
    }

    @Test
    public void testActionStr() throws Exception {
        String source = "" +
                "Enum State = Init Set\n" +
                "String Value = \"a\" \n" +
                "State == Init -> value = \"bc\" \n" +
                "State == Set  -> value = \"d{e}\"";
        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();

        assertThat(script.getEnum("State").get()).isEqualTo("init");
        assertThat(script.getVar ("Value").get()).isEqualTo("a");

        engine.onExecHandle();

        assertThat(script.getEnum("State").get()).isEqualTo("init");
        assertThat(script.getVar ("Value").get()).isEqualTo("bc");
    }

    @Test
    public void testActionInt() throws Exception {
        String source = "" +
                "Enum State = Init Set\n" +
                "Int Value = 0 \n" +
                "State == Init -> value = 1 \n" +
                "State == Set  -> value = 2";
        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();

        assertThat(script.getEnum("State").get()).isEqualTo("init");
        assertThat(script.getVar ("Value").getAsInt()).isEqualTo(0);

        engine.onExecHandle();

        assertThat(script.getEnum("State").get()).isEqualTo("init");
        assertThat(script.getVar ("Value").getAsInt()).isEqualTo(1);
    }

    @Test
    public void testActionIncDecInt() throws Exception {
        String source = "" +
                "Enum State = Init Set\n" +
                "Int Value = 1 \n" +
                "State == Init -> value += 1 \n" +
                "State == Set  -> value -= 5";
        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();

        assertThat(script.getEnum("State").get()).isEqualTo("init");
        assertThat(script.getVar ("Value").getAsInt()).isEqualTo(1);

        engine.onExecHandle();

        assertThat(script.getEnum("State").get()).isEqualTo("init");
        assertThat(script.getVar ("Value").getAsInt()).isEqualTo(2);

        script.getEnum("State").accept("set");
        engine.onExecHandle();

        assertThat(script.getEnum("State").get()).isEqualTo("set");
        assertThat(script.getVar ("Value").getAsInt()).isEqualTo(-3);
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
        Script script = mScriptComponent.createScriptParser2().parse(source);

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
        Script script = mScriptComponent.createScriptParser2().parse(source);

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
                "Error at line 9: Expected NUM or ID or \"STR\" argument for 't1' but found 'B42'.\n" +
                "  Line 9: 't1 forward -> t1 forward = B42'\n" +
                "Error at line 10: Expected NUM or ID or \"STR\" argument for 't1' but found 'block'.\n" +
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
                "int speed = 5 \n " +
                "t1 stopped -> t1 forward = speed \n" +
                "t1 forward -> t1 stop";

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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
                "int speed = 5 \n " +
                "t1 stopped -> t1 forward = speed ; t1 stop \n" +
                "t1 forward -> t1 stop ; t1 forward = speed ";

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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
                "int speed = 5 \n " +
                "t1 stopped -> t1 forward = speed ; t1 stop \n" +
                "t1 forward -> t1 stop ; t1 forward = speed ";

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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
    public void testThrottleRepeat() throws Exception {
        String source = "" +
                "throttle T1 = 42 \n " +
                "t1 stopped -> t1 Repeat = 2 \n" +
                "t1 forward -> t1 repeat = 0  ";

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);
        assertThat(script.getThrottle("t1").getRepeatSpeedSeconds()).isEqualTo(0);

        // Execute t1 stopped case
        engine.onExecHandle();
        assertThat(script.getThrottle("t1").getRepeatSpeedSeconds()).isEqualTo(2);

        // Execute t1 forward case
        script.getThrottle("t1").setSpeed(5);
        engine.onExecHandle();
        assertThat(script.getThrottle("t1").getRepeatSpeedSeconds()).isEqualTo(0);
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
        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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
                "int myVar=5\n " +
                "t1 stopped ->myVar=0\n" +
                "t1 forward ->myVar=1 ";

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

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

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriTurnout turnout1 = mock(IJmriTurnout.class);
        IJmriTurnout turnout2 = mock(IJmriTurnout.class);
        when(mJmriProvider.getTurnout("NT42")).thenReturn(turnout1);
        when(mJmriProvider.getTurnout("NT43")).thenReturn(turnout2);
        when(turnout1.isNormal()).thenReturn(IJmriTurnout.NORMAL);
        when(turnout2.isNormal()).thenReturn(IJmriTurnout.NORMAL);

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);
        verify(mJmriProvider).getTurnout("NT42");
        verify(mJmriProvider).getTurnout("NT43");

        engine.onExecHandle();
        verify(turnout1).setTurnout(IJmriTurnout.NORMAL);
        verify(turnout2).setTurnout(IJmriTurnout.REVERSE);
        when(turnout2.isNormal()).thenReturn(IJmriTurnout.REVERSE);
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

        Script script = mFakeNowScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mFakeNowScriptComponent.createScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();

        // throttle is stopped, starts t1
        assertThat(script.getTimer("t1").isActive()).isFalse();
        engine.onExecHandle();
        assertThat(script.getTimer("t1").isActive()).isFalse();

        // t1 is active 5 seconds later
        mClock.add(5*1000 - 1);
        engine.onExecHandle();
        assertThat(script.getTimer("t1").isActive()).isFalse();

        // Note: timer is still active because the "t1 ->" does not reset it with end yet.
        // A timer remains active till it is either restarted or ended.
        mClock.add(1);
        engine.onExecHandle();
        verify(mJmriThrottle).horn();
        verify(mJmriThrottle, never()).setSpeed(anyInt());
        assertThat(script.getTimer("t1").isActive()).isTrue();

        // t2 is active 2 seconds later. Both t1 gets reset as soon as t2 becomes active.
        mClock.add(2*1000);
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

        Script script = mFakeNowScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mFakeNowScriptComponent.createScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();

        // throttle is stopped, starts t2, t5, t9
        engine.onExecHandle();
        assertThat(script.getTimer("t2").isActive()).isFalse();
        assertThat(script.getTimer("t5").isActive()).isFalse();
        assertThat(script.getTimer("t9").isActive()).isFalse();

        // t2 is active 5 seconds later
        mClock.add(2*1000);
        engine.onExecHandle();
        assertThat(script.getTimer("t2").isActive()).isTrue();
        verify(mJmriThrottle).horn();

        // t5 is active 3 seconds later
        mClock.add(3*1000);
        engine.onExecHandle();
        assertThat(script.getTimer("t5").isActive()).isTrue();
        verify(mJmriThrottle).setSound(true);

        // t9 is active 4 seconds later
        mClock.add(4*1000);
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

        Script script = mFakeNowScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mFakeNowScriptComponent.createScriptExecEngine();

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
        mClock.add(2*1000);
        engine.onExecHandle();
        assertThat(script.getTimer("t2").isActive()).isFalse();
        verify(throttle).horn();

        // t5 is not executed 3 seconds later as it was reset
        mClock.add(3*1000);
        engine.onExecHandle();
        assertThat(script.getTimer("t5").isActive()).isFalse();
        verify(throttle, never()).setSound(true);

        // t9 is not executed 4 seconds later as it was reset
        mClock.add(4*1000);
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

        Script script = mFakeNowScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mFakeNowScriptComponent.createScriptExecEngine();

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
        mClock.add(2*1000 - 1);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(anyBoolean());

        mClock.add(1);
        engine.onExecHandle();
        verify(mJmriThrottle).setLight(false);
        verify(mJmriThrottle, never()).setSound(anyBoolean());


        // Line 3 : !b2 is active 4 seconds after the start.
        // However b1 is still negative so the line doesn't yet trigger.
        mClock.add(2 * 1000);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(anyBoolean());

        // Trigger b1. Note this is now "!b2 + 5" and thus does not trigger (because !b2 + 4
        // has auto-reset as soon as it became active).
        when(sensor1.isActive()).thenReturn(true);
        mClock.add(1 * 1000);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(false);

        // Line 2: b1 + 3 is now becoming active.
        mClock.add(3 * 1000);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle).setLight(true);
        verify(mJmriThrottle, never()).setSound(anyBoolean());

        // Line 4: b1 + 5 becomes active. But b2 is still negative so the line doesn't trigger.
        mClock.add(2 * 1000);
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
        mClock.add(6 * 1000);
        assertThat(script.getTimer("$b1$5$").isActive()).isFalse();
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(true);

        // Line 5: b2+7 becomes active.
        // But since b1+5 has been reset as soon as it became active.
        mClock.add(1 * 1000);
        reset(mJmriThrottle);
        engine.onExecHandle();
        verify(mJmriThrottle, never()).setLight(anyBoolean());
        verify(mJmriThrottle, never()).setSound(anyBoolean());
    }
}
