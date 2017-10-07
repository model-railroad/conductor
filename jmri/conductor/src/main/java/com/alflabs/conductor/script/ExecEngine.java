package com.alflabs.conductor.script;

import com.alflabs.conductor.util.FrequencyMeasurer;
import com.alflabs.conductor.util.Now;
import com.alflabs.conductor.util.RateLimiter;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Constants;
import com.alflabs.manifest.MapInfo;
import com.alflabs.manifest.MapInfos;
import com.alflabs.manifest.RouteInfo;
import com.alflabs.manifest.RouteInfos;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.inject.Inject;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@ScriptScope
public class ExecEngine implements IExecEngine {
    private final Script mScript;
    private final List<Event> mActivatedEvents = new LinkedList<>();
    private final CondCache mCondCache = new CondCache();
    private final FrequencyMeasurer mHandleFrequency;
    private final RateLimiter mHandleRateLimiter;
    private final IKeyValue mKeyValue;
    private Runnable mHandleListener;

    @Inject
    public ExecEngine(Now now, Script script, IKeyValue keyValue) {
        mScript = script;
        mHandleFrequency = new FrequencyMeasurer(now);
        mHandleRateLimiter = new RateLimiter(30.0f, now);
        mKeyValue = keyValue;
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

        for (Var var : mScript.getVars()) {
            var.onExecStart();
        }

        for (Enum_ enum_ : mScript.getEnums()) {
            enum_.onExecStart();
        }

        exportMaps(mScript.getMaps().values());
        exportRoutes(mScript.getRoutes().values());
    }

    private void exportMaps(Collection<MapInfo> values) {
        MapInfos maps = new MapInfos(values.toArray(new MapInfo[values.size()]));
        try {
            mKeyValue.putValue(Constants.MapsKey, maps.toJsonString(), true);
        } catch (JsonProcessingException e) {
            mScript.getLogger().log("[Conductor] Export KV Maps failed: " + e);
        }
    }

    private void exportRoutes(Collection<RouteInfo> values) {
        RouteInfos routes = new RouteInfos(values.toArray(new RouteInfo[values.size()]));
        try {
            mKeyValue.putValue(Constants.RoutesKey, routes.toJsonString(), true);
        } catch (JsonProcessingException e) {
            mScript.getLogger().log("[Conductor] Export KV Routes failed: " + e);
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
    @Override
    public void onExecHandle() {
        mHandleFrequency.ping();

        for (Throttle throttle : mScript.getThrottles()) {
            throttle.onExecHandle();
        }

        for (Turnout turnout : mScript.getTurnouts()) {
            turnout.onExecHandle();
        }

        for (Sensor sensor : mScript.getSensors()) {
            sensor.onExecHandle();
        }

        for (Var var : mScript.getVars()) {
            var.onExecHandle();
        }

        for (Enum_ enum_ : mScript.getEnums()) {
            enum_.onExecHandle();
        }

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
