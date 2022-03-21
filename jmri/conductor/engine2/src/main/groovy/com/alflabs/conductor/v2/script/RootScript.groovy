package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

class RootScript extends Script {

    private Map<String, Sensor> mSensors = new TreeMap<>()
    private Map<String, Block> mBlocks = new TreeMap<>()
    private Map<String, Turnout> mTurnouts = new TreeMap<>()
    private Map<String, Timer> mTimers = new TreeMap<>()
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
    void resolvePendingVars(Binding scriptBinding) {
        for (entry in scriptBinding.getVariables().entrySet()) {
            def k = entry.key
            def v = entry.value
            if (v instanceof BaseVar) {
                ((BaseVar) v).setVarName((String) k)
                // DEBUG // println "Var[$k] => $v"
                if (v instanceof Sensor) {
                    mSensors.put((String) k, (Sensor) v)
                } else if (v instanceof Block) {
                    mBlocks.put((String) k, (Block) v)
                } else if (v instanceof Turnout) {
                    mTurnouts.put((String) k, (Turnout) v)
                } else if (v instanceof Timer) {
                    mTimers.put((String) k, (Timer) v)
                } else if (v instanceof Throttle) {
                    mThrottles.put((String) k, (Throttle) v)
                } else if (v instanceof Route) {
                    mRoutes.put((String) k, (Route) v)
                } else if (v instanceof ActiveRoute) {
                    mActiveRoutes.put((String) k, (ActiveRoute) v)
                }
            }
        }
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
    Sensor sensor(String systemName) {
        return mSensors.computeIfAbsent(systemName) {
            name -> new Sensor(name)
        }
    }

    @NonNull
    Map<String, Sensor> sensors() {
        return mSensors.asUnmodifiable()
    }

    @NonNull
    Block block(String systemName) {
        return mBlocks.computeIfAbsent(systemName) {
            name -> new Block(name)
        }
    }

    @NonNull
    Map<String, Block> blocks() {
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
        return new Timer(delay)
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
    MapInfo map(@DelegatesTo(MapInfo) Closure cl) {
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
    Rule on(@DelegatesTo(RootScript) Closure<Boolean> condition) {
        //println "condition.delegate = ${condition.delegate}"      // => is RootScript
        //println "condition.owner = ${condition.owner}"            // => is RootScript
        //println "condition.this = ${condition.thisObject}"        // => is RootScript
        def rule = new Rule(condition)
        mRules.add(rule)
        return rule
    }

    @NonNull
    Route route(IRouteManager manager) {
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
    IRouteManager sequence(@DelegatesTo(SequenceInfo) Closure cl) {
        def info = new SequenceInfo()
        def code = cl.rehydrate(info /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
        return new SequenceManager(info)
    }

    @NonNull
    SequenceNode node(Block block, @DelegatesTo(SequenceNodeEvents) Closure action) {
        return new SequenceNode(block, action)
    }

    @NonNull
    ActiveRoute activeRoute(@DelegatesTo(ActiveRouteInfo) Closure cl) {
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
