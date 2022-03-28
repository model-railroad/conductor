package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull
import com.alflabs.annotations.Null
import com.alflabs.conductor.v2.script.impl.ActiveRoute
import com.alflabs.conductor.v2.script.impl.ActiveRouteInfo
import com.alflabs.conductor.v2.script.impl.BaseVar
import com.alflabs.conductor.v2.script.impl.Block
import com.alflabs.conductor.v2.script.impl.IRouteManager
import com.alflabs.conductor.v2.script.impl.IRule
import com.alflabs.conductor.v2.script.impl.IdleManager
import com.alflabs.conductor.v2.script.impl.MapInfo
import com.alflabs.conductor.v2.script.impl.Route
import com.alflabs.conductor.v2.script.impl.Rule
import com.alflabs.conductor.v2.script.impl.RuleAfter
import com.alflabs.conductor.v2.script.impl.Sensor
import com.alflabs.conductor.v2.script.impl.SequenceInfo
import com.alflabs.conductor.v2.script.impl.SequenceManager
import com.alflabs.conductor.v2.script.impl.SequenceNode
import com.alflabs.conductor.v2.script.impl.SequenceNodeEvents
import com.alflabs.conductor.v2.script.impl.Throttle
import com.alflabs.conductor.v2.script.impl.Timer
import com.alflabs.conductor.v2.script.impl.Turnout

class RootScript extends Script {

    private Map<String, ISensor> mSensors = new TreeMap<>()
    private Map<String, IBlock> mBlocks = new TreeMap<>()
    private Map<String, Turnout> mTurnouts = new TreeMap<>()
    private Map<String, Timer> mTimers = new TreeMap<>()
    private List<Timer> mAnonymousTimers = new ArrayList<>();
    private Map<String, Throttle> mThrottles = new TreeMap<>()
    private Map<String, MapInfo> mMaps = new TreeMap<>()
    private Map<String, Route> mRoutes = new TreeMap<>()
    private Map<String, ActiveRoute> mActiveRoutes = new TreeMap<>()
    private List<IRule> mRules = new ArrayList<>()

    @Override
    Object run() {
        return null
    }

    /** Called after the first script runScript to collect all global variables' names. */
    void resolvePendingVars(@NonNull Binding scriptBinding) {
        for (entry in scriptBinding.getVariables().entrySet()) {
            def k = entry.key
            def v = entry.value
            if (v instanceof BaseVar) {
                ((BaseVar) v).setVarName((String) k)
                // DEBUG // println "Var[$k] => $v"
                if (v instanceof ISensor) {
                    mSensors.put((String) k, (ISensor) v)
                } else if (v instanceof IBlock) {
                    mBlocks.put((String) k, (IBlock) v)
                } else if (v instanceof Turnout) {
                    mTurnouts.put((String) k, (Turnout) v)
                } else if (v instanceof Timer) {
                    mTimers.put((String) k, (Timer) v)
                    mAnonymousTimers.remove(v)
                } else if (v instanceof Throttle) {
                    mThrottles.put((String) k, (Throttle) v)
                } else if (v instanceof Route) {
                    mRoutes.put((String) k, (Route) v)
                } else if (v instanceof ActiveRoute) {
                    mActiveRoutes.put((String) k, (ActiveRoute) v)
                }
            }
        }

        for (Timer t in mAnonymousTimers) {
            if (mTimers.containsKey(t.varName)) {
                // Make name unique
                t.varName = String.format("%s_%08x", t.varName, t.hashCode())
            }
            mTimers.put(t.varName, t)
        }
        mAnonymousTimers.clear()
    }

    /** Executes all Rules. */
    void executeRules() {
        List<IRule> activeRules = new ArrayList<>()

        // First collect all rules with an active condititon.
        for (IRule rule : mRules) {
            if (rule.evaluateCondition()) {
                activeRules.add(rule)
            }
        }

        // TBD parse all active routes, and queue all onEnter / onActivate rules.
        for (Map.Entry<String, ActiveRoute> activeRoute : mActiveRoutes.entrySet()) {
            List<IRule> rules = activeRoute.value.evaluateRules()
            if (!rules.isEmpty()) {
                activeRules.addAll(rules)
            }
        }

        // Second execute all actions in the order they are defined.
        for (IRule rule : activeRules) {
            rule.evaluateAction(this)
        }
    }

    @NonNull
    List<IRule> rules() {
        return mRules.asUnmodifiable()
    }

    @NonNull
    ISensor sensor(@NonNull String systemName) {
        return mSensors.computeIfAbsent(systemName) {
            name -> new Sensor(name)
        }
    }

    @NonNull
    Map<String, ISensor> sensors() {
        return mSensors.asUnmodifiable()
    }

    @NonNull
    IBlock block(@NonNull String systemName) {
        return mBlocks.computeIfAbsent(systemName) {
            name -> new Block(name)
        }
    }

    @NonNull
    Map<String, IBlock> blocks() {
        return mBlocks.asUnmodifiable()
    }

    @NonNull
    Turnout turnout(String systemName) {
        return mTurnouts.computeIfAbsent(systemName) {
            name -> new Turnout(name)
        }
    }

    @NonNull
    Map<String, Turnout> turnouts() {
        return mTurnouts.asUnmodifiable()
    }

    @NonNull
    Timer timer(int delay) {
        def t = new Timer(delay)
        t.setVarName("@timer@${delay}")
        mAnonymousTimers.add(t)
        return t
    }

    @NonNull
    private Timer chainTimers(@Null Timer previousTimer, @NonNull Timer supplementalTimer) {
        Timer t = supplementalTimer
        if (previousTimer != null) {
            t = new Timer(previousTimer, supplementalTimer.delay)
            t.setVarName(previousTimer.varName + "_" + supplementalTimer.varName)
        }
        mAnonymousTimers.add(t)
        return t
    }

    @NonNull
    Map<String, Timer> timers() {
        return mTimers.asUnmodifiable()
    }

    @NonNull
    Throttle throttle(int dccAddress) {
        return new Throttle(dccAddress)
    }

    @NonNull
    Map<String, Throttle> throttles() {
        return mThrottles.asUnmodifiable()
    }

    @NonNull
    MapInfo map(@NonNull @DelegatesTo(MapInfo) Closure cl) {
        def map = new MapInfo()
        def code = cl.rehydrate(map /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
        mMaps.put(map.name, map)
        return map
    }

    @NonNull
    Map<String, MapInfo> maps() {
        return mMaps.asUnmodifiable()
    }

    @NonNull
    Rule on(@NonNull @DelegatesTo(RootScript) Closure<Boolean> condition) {
        //println "condition.delegate = ${condition.delegate}"      // => is RootScript
        //println "condition.owner = ${condition.owner}"            // => is RootScript
        //println "condition.this = ${condition.thisObject}"        // => is RootScript
        def rule = new Rule(condition)
        mRules.add(rule)
        return rule
    }

    @NonNull
    RuleAfter after(@NonNull Timer afterTimer) {
        def rule = new RuleAfter(afterTimer, __and_after_to_then(afterTimer, mRules))
        mRules.add(rule)
        return rule
    }

    private RuleAfter.AndAfterContinuation __and_after_to_then(
            @NonNull Timer previousTimer,
            @NonNull List<IRule> rules) {
        return new RuleAfter.AndAfterContinuation() {
            @Override
            RuleAfter and_after(Timer newTimer) {
                def chainedTimer = chainTimers(previousTimer, newTimer)
                def rule = new RuleAfter(chainedTimer, __and_after_to_then(chainedTimer, rules))
                rules.add(rule)
                return rule
            }
        }
    }

    @NonNull
    Route route(@NonNull IRouteManager manager) {
        return new Route(manager)
    }

    @NonNull
    Map<String, Route> routes() {
        return mRoutes.asUnmodifiable()
    }

    // as a function: manager = idle()
    @NonNull
    IRouteManager idle() {
        return new IdleManager()
    }

    // as a property getter: manager = idle
    //IRouteManager getIdle() {
    //    return idle()
    //}

    @NonNull
    IRouteManager sequence(@NonNull @DelegatesTo(SequenceInfo) Closure cl) {
        def info = new SequenceInfo()
        def code = cl.rehydrate(info /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
        return new SequenceManager(info)
    }

    @NonNull
    SequenceNode node(@NonNull Block block,
                      @NonNull @DelegatesTo(SequenceNodeEvents) Closure action) {
        return new SequenceNode(block, action)
    }

    @NonNull
    ActiveRoute activeRoute(@NonNull @DelegatesTo(ActiveRouteInfo) Closure cl) {
        def info = new ActiveRouteInfo()
        def code = cl.rehydrate(info /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
        return new ActiveRoute(info)
    }

    @NonNull
    Map<String, ActiveRoute> activeRoutes() {
        return mActiveRoutes.asUnmodifiable()
    }

}
