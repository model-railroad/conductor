package com.alflabs.conductor.v2.script

class SequenceManager implements IRouteManager {
    private final SequenceInfo mSequenceInfo

    SequenceManager(SequenceInfo sequenceInfo) {
        mSequenceInfo = sequenceInfo
    }

    SequenceInfo getSequenceInfo() {
        return mSequenceInfo
    }

    @Override
    List<IRule> evaluateRules() {
        // TBD evaluate onActive, nodes onEnter, etc.
        return Collections.emptyList()
    }
}
