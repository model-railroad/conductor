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

import com.alflabs.conductor.util.FrequencyMeasurer;
import com.alflabs.conductor.util.RateLimiter;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Constants;
import com.alflabs.manifest.MapInfo;
import com.alflabs.manifest.MapInfos;
import com.alflabs.manifest.RouteInfo;
import com.alflabs.manifest.RouteInfos;
import com.alflabs.utils.IClock;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.inject.Inject;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@ScriptScope
public class ExecEngine implements IExecEngine {
    private final Script mScript;
    private final List<Event> mActivatedEvents = new LinkedList<>();
    private final CondCache mCondCache = new CondCache();
    private final FrequencyMeasurer mHandleFrequency;
    private final RateLimiter mHandleRateLimiter;
    private final IKeyValue mKeyValue;
    private Runnable mHandleListener;
    private Constants.EStopState mLastEStopState;

    @Inject
    public ExecEngine(IClock clock, Script script, IKeyValue keyValue) {
        mScript = script;
        mHandleFrequency = new FrequencyMeasurer(clock);
        mHandleRateLimiter = new RateLimiter(30.0f, clock);
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

        reset();
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

        propagateExecHandle();

        final Constants.EStopState eStopState = getEStopState();
        switch (eStopState) {
        case NORMAL:
            evalScript();
            break;
        case ACTIVE:
            if (mLastEStopState == Constants.EStopState.NORMAL) {
                // First time going from NORMAL to ACTIVE E-Stop.
                eStopAllThrottles();
            }
            break;
        case RESET:
            reset();
            break;
        }
        mLastEStopState = eStopState;

        Runnable listener = mHandleListener;
        if (listener != null) {
            try {
                listener.run();
            } catch (Throwable e) {
                mScript.getLogger().log("[Conductor] Handle Listener: " + e);
            }
        }

        mHandleRateLimiter.limit();
    }

    /**
     * Returns true if The EStop-State is defined and Normal.
     * <p/>
     * For a more predictible behavior, the absence of the EStop-State is treated as
     * a active case. This is one of these "should not happen" scenarios.
     */
    private Constants.EStopState getEStopState() {
        final String value = mKeyValue.getValue(Constants.EStopKey);
        if (value == null) return Constants.EStopState.ACTIVE;
        try {
            return Constants.EStopState.valueOf(value);
        } catch (IllegalArgumentException ignore) {}
        return Constants.EStopState.ACTIVE;
    }

    private void propagateExecHandle() {
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
    }

    private void evalScript() {
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
    }

    private void reset() {
        // for (Throttle throttle : mScript.getThrottles()) : not resettable
        // for (Turnout turnout : mScript.getTurnouts()) : not resettable
        // for (Sensor sensor : mScript.getSensors()) : : not resettable

        mScript.getResetTimersFunction().accept(0);

        for (Var var : mScript.getVars()) {
            var.reset();
        }

        for (Enum_ enum_ : mScript.getEnums()) {
            enum_.reset();
        }

        mCondCache.clear();
        mActivatedEvents.clear();
        for (Event event : mScript.getEvents()) {
            event.resetExecuted();
        }

        mLastEStopState = Constants.EStopState.NORMAL;
        mKeyValue.putValue(Constants.EStopKey, mLastEStopState.toString(), true);
    }

    private void eStopAllThrottles() {
        for (Throttle throttle : mScript.getThrottles()) {
            throttle.eStop();
        }
    }

    public float getHandleFrequency() {
        return mHandleFrequency.getFrequency();
    }

    public void setHandleListener(Runnable handleListener) {
        mHandleListener = handleListener;
    }
}
