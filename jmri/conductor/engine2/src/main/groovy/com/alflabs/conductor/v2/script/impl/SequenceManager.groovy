package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull

class SequenceManager implements IRouteManager {
    private final SequenceInfo mSequenceInfo

    SequenceManager(@NonNull SequenceInfo sequenceInfo) {
        mSequenceInfo = sequenceInfo
    }

    @NonNull
    SequenceInfo getSequenceInfo() {
        return mSequenceInfo
    }

    @Override
    @NonNull
    List<IRule> evaluateRules() {
        // TBD evaluate onActive, nodes onEnter, etc.
        return Collections.emptyList()
    }
}
