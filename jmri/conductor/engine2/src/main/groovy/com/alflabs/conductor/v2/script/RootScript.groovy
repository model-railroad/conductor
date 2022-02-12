package com.alflabs.conductor.v2.script

class RootScript extends Script {

    private Map<String, Sensor> mSensors = new TreeMap<>()
    private Map<String, Block> mBlocks = new TreeMap<>()
    private Map<String, Turnout> mTurnouts = new TreeMap<>()
    private Map<String, MapInfo> mMaps = new TreeMap<>()
    private List<Rule> mRules = new ArrayList<>()

    @Override
    Object run() {
        return null
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

    MapInfo map(
            @DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = MapInfo)
            Closure cl) {
        def map = new MapInfo()
        def code = cl.rehydrate(map /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code.call()
        mMaps.put(map.name, map)
        return map
    }

    Map<String, MapInfo> maps() {
        return mMaps.asUnmodifiable()
    }

    Rule on(
            @DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = RootScript)
            Closure<Boolean> condition) {
        def rule = new Rule(condition)
        mRules.add(rule)
        return rule
    }
}
