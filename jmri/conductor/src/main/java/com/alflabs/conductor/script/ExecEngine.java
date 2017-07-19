package com.alflabs.conductor.script;

import com.alflabs.conductor.util.FrequencyMeasurer;
import com.alflabs.conductor.util.Now;
import com.alflabs.conductor.util.RateLimiter;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

@ScriptScope
public class ExecEngine implements IExecStart {
    private final Script mScript;
    private final List<Event> mActivatedEvents = new LinkedList<>();
    private final CondCache mCondCache = new CondCache();
    private final FrequencyMeasurer mHandleFrequency;
    private final RateLimiter mHandleRateLimiter;
    private Runnable mHandleListener;

    @Inject
    public ExecEngine(Now now, Script script) {
        mScript = script;
        mHandleFrequency = new FrequencyMeasurer(now);
        mHandleRateLimiter = new RateLimiter(30.0f, now);
    }


    /**
     * Initializes throttle and sensors before executing the script.
     */
    @Override
    public void onExecStart() {
        for (Throttle throttle : mScript.getThrottles()) {
            throttle.onExecStart();
        }

        for (Turnout turnout : mScript.getTurnouts()) {
            turnout.onExecStart();
        }

        for (Sensor sensor : mScript.getSensors()) {
            sensor.onExecStart();
        }
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
        for (Event event : mScript.getEvents()) {
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
            } catch (Throwable ignore) {
            }
        }

        mHandleRateLimiter.limit();
    }

    public float getHandleFrequency() {
        return mHandleFrequency.getFrequency();
    }

    public void setHandleListener(Runnable handleListener) {
        mHandleListener = handleListener;
    }
}
