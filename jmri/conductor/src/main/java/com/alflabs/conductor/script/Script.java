package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.util.FrequencyMeasurer;
import com.alflabs.conductor.util.Logger;
import com.alflabs.conductor.util.NowProvider;
import com.alflabs.conductor.util.RateLimiter;

import java.util.ArrayList;
import java.util.LinkedList;
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
 * The script follows the setup/handle format of a JMRI Jython script. A script must be first
 * {@link #setup(IJmriProvider)} to link to the underlying JMRI throttles and sensors, then
 * {@link #handle()} is called repeatedly to evaluate all conditions and execute all fired events.
 * <p/>
 * Implementation wise, the "execution engine" is so simple that it is integrated here instead
 * of being factored out.
 */
public class Script extends NowProvider {

    private final Logger mLogger;
    private final TreeMap<String, Throttle> mThrottles = new TreeMap<>();
    private final TreeMap<String, Var> mVars = new TreeMap<>();
    private final TreeMap<String, Sensor> mSensors = new TreeMap<>();
    private final TreeMap<String, Turnout> mTurnouts = new TreeMap<>();
    private final TreeMap<String, Timer> mTimers = new TreeMap<>();
    private final List<Event> mEvents = new ArrayList<>();
    private final List<Event> mActivatedEvents = new LinkedList<>();
    private final CondCache mCondCache = new CondCache();
    private final FrequencyMeasurer mHandleFrequency = new FrequencyMeasurer(this);
    private final RateLimiter mHandleRateLimiter = new RateLimiter(30.0f, this);
    private Runnable mHandleListener;

    public Script(Logger logger) {
        mLogger = logger;
    }

    public Logger getLogger() {
        return mLogger;
    }

    public void addThrottle(String name, Throttle throttle) {
        mThrottles.put(name.toLowerCase(Locale.US), throttle);
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

    public Throttle getThrottle(String name) {
        return mThrottles.get(name.toLowerCase(Locale.US));
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

    public boolean isExistingName(String name) {
        name = name.toLowerCase(Locale.US);
        return mThrottles.containsKey(name)
                || mVars.containsKey(name)
                || getConditional(name) != null;
    }

    /**
     * Initializes throttle and sensors before executing the script.
     *
     * @param provider A non-null JMRI provider.
     * @return An error if there's no DCC variable for the throttle DCC address.
     */
    public boolean setup(IJmriProvider provider) {
        for (Throttle throttle : mThrottles.values()) {
            throttle.setup(provider);
        }

        for (Turnout turnout : mTurnouts.values()) {
            turnout.setup(provider);
        }

        for (Sensor sensor : mSensors.values()) {
            sensor.setup(provider);
        }

        return true;
    }

    /**
     * Handles one execution of events.
     * <p/>
     * This first checks ALL the events, and then applies activated actions.
     * Because some actions influence conditions (e.g. throttle stop/forward), all conditions
     * are evaluated first. Actions are only executed after all conditions have been checked.
     * Conditions are checked only once per iteration to make sure that timers and sensors
     * give a uniform view during the evaluation.
     * <p/>
     * Each event is only activated once when the condition becomes true (e.g. on a raising
     * edge in electronics terms). Next time the event is evaluated, it is not executed again
     * unless the condition was first evaluated to false.
     */
    public void handle() {
        mHandleFrequency.ping();

        mCondCache.clear();
        mActivatedEvents.clear();
        for (Event event : mEvents) {
            if (event.evalConditions(mCondCache)) {
                if (!event.isExecuted()) {
                    mActivatedEvents.add(event);
                }
            } else {
                event.resetExecuted();
            }
        }
        for (Event event : mActivatedEvents) {
            event.execute();
        }

        if (mHandleListener != null) {
            try {
                mHandleListener.run();
            } catch (Throwable ignore) {}
        }

        mHandleRateLimiter.limit();
    }

    public float getHandleFrequency() {
        return mHandleFrequency.getFrequency();
    }

    public void setHandleListener(Runnable handleListener) {
        mHandleListener = handleListener;
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
