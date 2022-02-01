package com.alflabs.conductor.v2.script

class RootScript extends Script {

    @Override
    Object run() {
        return null
    }

    Sensor sensor(String systemName) {
        return new Sensor(systemName)
    }

    Block block(String systemName) {
        return new Block(systemName)
    }

    Turnout turnout(String systemName) {
        return new Turnout(systemName)
    }

    MapInfo map(
            @DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = MapInfo)
            Closure cl) {
        def map = new MapInfo()
        def code = cl.rehydrate(map /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code.call()
        return map
    }
}
