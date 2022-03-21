package com.alflabs.conductor.v2.script

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
        for (ActiveRoute activeRoute : mActiveRoutes) {
            List<IRule> rules = activeRoute.evaluateRules()
            if (rules != null && !rules.isEmpty()) {
                activeRules.addAll(rules)
            }
        }

        // Second execute all actions in the order they are defined.
        for (IRule rule : activeRules) {
            rule.evaluateAction(this)
        }
    }

    List<IRule> rules() {
        return mRules.asUnmodifiable()
    }

    Sensor sensor(String systemName) {
        return mSensors.computeIfAbsent(systemName) {
            name -> new Sensor(name)
        }
    }

    Map<String, Sensor> sensors() {
        return mSensors.asUnmodifiable()
    }

    Block block(String systemName) {
        return mBlocks.computeIfAbsent(systemName) {
            name -> new Block(name)
        }
    }

    Map<String, Block> blocks() {
        return mBlocks.asUnmodifiable()
    }

    Turnout turnout(String systemName) {
        return mTurnouts.computeIfAbsent(systemName) {
            name -> new Turnout(name)
        }
    }

    Map<String, Turnout> turnouts() {
        return mTurnouts.asUnmodifiable()
    }

    Timer timer(int delay) {
        return new Timer(delay)
    }

    Map<String, Timer> timers() {
        return mTimers.asUnmodifiable()
    }

    Throttle throttle(int dccAddress) {
        return new Throttle(dccAddress)
    }

    Map<String, Throttle> throttles() {
        return mThrottles.asUnmodifiable()
    }

    MapInfo map(@DelegatesTo(MapInfo) Closure cl) {
        def map = new MapInfo()
        def code = cl.rehydrate(map /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
        mMaps.put(map.name, map)
        return map
    }

    Map<String, MapInfo> maps() {
        return mMaps.asUnmodifiable()
    }

    Rule on(@DelegatesTo(RootScript) Closure<Boolean> condition) {
        //println "condition.delegate = ${condition.delegate}"      // => is RootScript
        //println "condition.owner = ${condition.owner}"            // => is RootScript
        //println "condition.this = ${condition.thisObject}"        // => is RootScript
        def rule = new Rule(condition)
        mRules.add(rule)
        return rule
    }

    Route route(IRouteManager manager) {
        return new Route(manager)
    }

    Map<String, Route> routes() {
        return mRoutes.asUnmodifiable()
    }

    // as a function: manager = idle()
    IRouteManager idle() {
        return new IdleManager()
    }

    // as a property getter: manager = idle
    //IRouteManager getIdle() {
    //    return idle()
    //}

    IRouteManager sequence(@DelegatesTo(SequenceInfo) Closure cl) {
        def info = new SequenceInfo()
        def code = cl.rehydrate(info /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
        return new SequenceManager(info)
    }

    SequenceNode node(Block block, @DelegatesTo(SequenceNodeEvents) Closure action) {
        return new SequenceNode(block, action)
    }

    ActiveRoute activeRoute(@DelegatesTo(ActiveRouteInfo) Closure cl) {
        def info = new ActiveRouteInfo()
        def code = cl.rehydrate(info /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
        return new ActiveRoute(info)
    }

    Map<String, ActiveRoute> activeRoutes() {
        return mActiveRoutes.asUnmodifiable()
    }

}
