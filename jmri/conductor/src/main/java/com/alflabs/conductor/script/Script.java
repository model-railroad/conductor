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

package com.alflabs.conductor.script;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.conductor.util.ILocalTimeNowProvider;
import com.alflabs.conductor.util.Logger;
import com.alflabs.manifest.Constants;
import com.alflabs.manifest.MapInfo;
import com.alflabs.manifest.Prefix;
import com.alflabs.manifest.RouteInfo;
import com.alflabs.utils.IClock;
import com.google.common.collect.ImmutableList;
import com.google.googlejavaformat.Indent;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.time.Clock;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/**
 * A script with all its state as well as its "execution engine".
 * <p/>
 * A script is composed of typed variables (throttles, timers, sensors and integer
 * variables) and a series of events.
 * Each event is a combination of boolean conditions (acting as AND, all must be active)
 * and a sequence of actions. Events fire on a "raising edge" means only when their condition
 * switches from false to true. Once fire, the event will not be executed again till the condition
 * first becomes false.
 * <p/>
 * The script follows the setup/handle format of a JMRI Jython script.
 * There's an {@link ExecEngine} associated with this script.
 * A exec engine must be first setup by calling the adequately named
 * {@link ExecEngine#onExecStart()} method to link to the underlying JMRI throttles and sensors,
 * then {@link ExecEngine#onExecHandle()} is called repeatedly to evaluate all conditions and execute
 * all fired events. The execution engine's instance keeps all the dynamic state due to the
 * evaluation whereas the script is "static" and does not change once parsed.
 */
@ScriptScope
public class Script {

    private final Logger mLogger;
    private final ILocalTimeNowProvider mLocalTimeNow;
    private final EStopHandler mEStopHandler;
    private final EnumFactory mEnumFactory;
    private final VarFactory mVarFactory;
    private final TreeMap<String, Throttle> mThrottles = new TreeMap<>();
    private final TreeMap<String, Enum_> mEnums = new TreeMap<>();
    private final TreeMap<String, Var> mVars = new TreeMap<>();
    private final TreeMap<String, Sensor> mSensors = new TreeMap<>();
    private final TreeMap<String, Turnout> mTurnouts = new TreeMap<>();
    private final TreeMap<String, Timer> mTimers = new TreeMap<>();
    private final TreeMap<String, MapInfo> mMaps = new TreeMap<>();
    private final TreeMap<String, RouteInfo> mRoutes = new TreeMap<>();
    private final List<Event> mEvents = new ArrayList<>();

    @Inject
    public Script(
            Logger logger,
            ILocalTimeNowProvider localTimeNow,
            EStopHandler eStopHandler,
            EnumFactory enumFactory,
            VarFactory varFactory) {
        mLogger = logger;
        mLocalTimeNow = localTimeNow;
        mEStopHandler = eStopHandler;
        mEnumFactory = enumFactory;
        mVarFactory = varFactory;

        createBuiltinVariables();
    }

    /** Create built-in variables shared with RTAC so that the script can use them. */
    private void createBuiltinVariables() {
        String rtacTextName = Constants.RtacPsaText.substring(Prefix.Var.length() );
        Var rtacText = mVarFactory.create("Loading...", rtacTextName.toLowerCase(Locale.US));
        rtacText.setExported(true);
        addVar(rtacTextName, rtacText);

        String rtacMotionName = Constants.RtacMotion.substring(Prefix.Var.length() );
        Enum_ rtacMotion = mEnumFactory.create(ImmutableList.of(Constants.Disabled, Constants.Off, Constants.On), rtacMotionName);
        rtacMotion.setImported(true);
        addEnum(rtacMotionName, rtacMotion);

        String hhmmTimeName = Constants.ConductorTime.substring(Prefix.Var.length() );
        Var hhmmTime = mVarFactory.create(() -> {
            // Note: This is the system time in the "default" timezone which is... well it depends.
            // Many linux installs default to UTC, so that needs to be verified on deployment site.
            LocalTime now = mLocalTimeNow.getNow();
            int h = now.getHour();
            int m = now.getMinute();
            return h * 100 + m;
        }, hhmmTimeName.toLowerCase(Locale.US));
        hhmmTime.setExported(true);
        addVar(hhmmTimeName, hhmmTime);
    }

    public Logger getLogger() {
        return mLogger;
    }

    @NonNull
    Collection<Throttle> getThrottles() {
        return mThrottles.values();
    }

    @NonNull
    Collection<Sensor> getSensors() {
        return mSensors.values();
    }

    @NonNull
    Collection<Turnout> getTurnouts() {
        return mTurnouts.values();
    }

    @NonNull
    Collection<Event> getEvents() {
        return mEvents;
    }

    @NonNull
    public Collection<Enum_> getEnums() {
        return mEnums.values();
    }

    @NonNull
    public Collection<Var> getVars() {
        return mVars.values();
    }

    public void addThrottle(String name, Throttle throttle) {
        mThrottles.put(name.toLowerCase(Locale.US), throttle);
    }

    public void addEnum(String name, Enum_ enum_) {
        mEnums.put(name.toLowerCase(Locale.US), enum_);
    }

    public void addVar(String name, Var var) {
        mVars.put(name.toLowerCase(Locale.US), var);
    }

    public void addSensor(String name, Sensor sensor) {
        mSensors.put(name.toLowerCase(Locale.US), sensor);
    }

    public void addTurnout(String name, Turnout turnout) {
        mTurnouts.put(name.toLowerCase(Locale.US), turnout);
    }

    public void addTimer(String name, Timer timer) {
        mTimers.put(name.toLowerCase(Locale.US), timer);
    }

    public void addEvent(Event event) {
        mEvents.add(event);
    }

    public void addMap(String mapName, MapInfo mapInfo) {
        mMaps.put(mapName.toLowerCase(Locale.US), mapInfo);
    }

    public void addRoute(String routeName, RouteInfo routeInfo) {
        mRoutes.put(routeName.toLowerCase(Locale.US), routeInfo);
    }

    @Null
    public Throttle getThrottle(String name) {
        return mThrottles.get(name.toLowerCase(Locale.US));
    }

    @Null
    public Enum_ getEnum(String name) {
        return mEnums.get(name.toLowerCase(Locale.US));
    }

    @Null
    public Var getVar(String name) {
        return mVars.get(name.toLowerCase(Locale.US));
    }

    @Null
    public Sensor getSensor(String name) {
        return mSensors.get(name.toLowerCase(Locale.US));
    }

    @Null
    public Turnout getTurnout(String name) {
        return mTurnouts.get(name.toLowerCase(Locale.US));
    }

    @Null
    public Timer getTimer(String name) {
        return mTimers.get(name.toLowerCase(Locale.US));
    }

    @Null
    public IConditional getConditional(String name) {
        name = name.toLowerCase(Locale.US);
        if (mVars.containsKey(name)) {
            return mVars.get(name);

        } else if (mSensors.containsKey(name)) {
            return mSensors.get(name);

        } else if (mTurnouts.containsKey(name)) {
            return mTurnouts.get(name);

        } else if (mTimers.containsKey(name)) {
            return mTimers.get(name);
        }

        return null;
    }

    @NonNull
    public List<String> getThrottleNames() {
        return new ArrayList<>(mThrottles.keySet());
    }

    @NonNull
    public List<String> getTurnoutNames() {
        return new ArrayList<>(mTurnouts.keySet());
    }

    @NonNull
    public List<String> getTimerNames() {
        return new ArrayList<>(mTimers.keySet());
    }

    @NonNull
    public List<String> getSensorNames() {
        return new ArrayList<>(mSensors.keySet());
    }

    @NonNull
    public List<String> getVarNames() {
        return new ArrayList<>(mVars.keySet());
    }

    @NonNull
    public List<String> getEnumNames() {
        return new ArrayList<>(mEnums.keySet());
    }

    @Null
    public TreeMap<String, MapInfo> getMaps() {
        return mMaps;
    }

    @Null
    public TreeMap<String, RouteInfo> getRoutes() {
        return mRoutes;
    }

    /** Returns the KV Server key name for the script ID name or null if not defined. */
    @Null
    public String getKVKeyNameForId(@NonNull String name) {
        name = name.toLowerCase(Locale.US);
        if (mSensors.containsKey(name)) {
            return Prefix.Sensor + name;
        }
        if (mTurnouts.containsKey(name)) {
            return Prefix.Turnout + name;
        }
        if (mThrottles.containsKey(name)) {
            return Prefix.DccThrottle + name;
        }
        if (mVars.containsKey(name) || mEnums.containsKey(name)) {
            return Prefix.Var + name;
        }
        if (mMaps.containsKey(name)) {
            return Prefix.Map + name;
        }
        if (mRoutes.containsKey(name)) {
            return Prefix.Route + name;
        }
        return null;
    }

    public boolean isExistingName(String name) {
        name = name.toLowerCase(Locale.US);
        return mThrottles.containsKey(name)
                || mVars.containsKey(name)
                || mEnums.containsKey(name)
                || mMaps.containsKey(name)
                || mRoutes.containsKey(name)
                || getConditional(name) != null;
    }

    public IAction getResetTimersAction() {
        return () -> {
            for (Timer timer : mTimers.values()) {
                timer.reset();
            }
        };
    }

    public IAction getEstopAction() {
        return mEStopHandler::activateEStop;
    }
}
