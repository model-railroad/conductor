package com.alflabs.conductor.script;

import com.alflabs.conductor.util.Logger;

import javax.inject.Inject;
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
    private final TreeMap<String, Throttle> mThrottles = new TreeMap<>();
    private final TreeMap<String, Enum_> mEnums = new TreeMap<>();
    private final TreeMap<String, Var> mVars = new TreeMap<>();
    private final TreeMap<String, Sensor> mSensors = new TreeMap<>();
    private final TreeMap<String, Turnout> mTurnouts = new TreeMap<>();
    private final TreeMap<String, Timer> mTimers = new TreeMap<>();
    private final TreeMap<String, String> mMaps = new TreeMap<>();
    private final List<Event> mEvents = new ArrayList<>();

    @Inject
    public Script(Logger logger) {
        mLogger = logger;
    }

    public Logger getLogger() {
        return mLogger;
    }

    Collection<Throttle> getThrottles() {
        return mThrottles.values();
    }

    Collection<Sensor> getSensors() {
        return mSensors.values();
    }

    Collection<Turnout> getTurnouts() {
        return mTurnouts.values();
    }

    Collection<Event> getEvents() {
        return mEvents;
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

    public void addMap(String mapName, String mapFilename) {
        mMaps.put(mapName.toLowerCase(Locale.US), mapFilename);
    }

    public Throttle getThrottle(String name) {
        return mThrottles.get(name.toLowerCase(Locale.US));
    }

    public Enum_ getEnum(String name) {
        return mEnums.get(name.toLowerCase(Locale.US));
    }

    public Var getVar(String name) {
        return mVars.get(name.toLowerCase(Locale.US));
    }

    public Sensor getSensor(String name) {
        return mSensors.get(name.toLowerCase(Locale.US));
    }

    public Turnout getTurnout(String name) {
        return mTurnouts.get(name.toLowerCase(Locale.US));
    }

    public Timer getTimer(String name) {
        return mTimers.get(name.toLowerCase(Locale.US));
    }

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

    public List<String> getThrottleNames() {
        return new ArrayList<>(mThrottles.keySet());
    }

    public List<String> getTurnoutNames() {
        return new ArrayList<>(mTurnouts.keySet());
    }
    
    public List<String> getTimerNames() {
        return new ArrayList<>(mTimers.keySet());
    }

    public List<String> getSensorNames() {
        return new ArrayList<>(mSensors.keySet());
    }

    public List<String> getVarNames() {
        return new ArrayList<>(mVars.keySet());
    }

    public TreeMap<String, String> getMaps() {
        return mMaps;
    }

    public boolean isExistingName(String name) {
        name = name.toLowerCase(Locale.US);
        return mThrottles.containsKey(name)
                || mVars.containsKey(name)
                || mEnums.containsKey(name)
                || mMaps.containsKey(name)
                || getConditional(name) != null;
    }

    private IIntFunction mResetTimersFunction = ignored -> {
        for (Timer timer : mTimers.values()) {
            timer.reset();
        }
    };

    public IIntFunction getResetTimersFunction() {
        return mResetTimersFunction;
    }
}
