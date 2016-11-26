package com.alflabs.conductor.script;

import java.util.HashMap;
import java.util.Map;

class CondCache {
    private final Map<IConditional, Boolean> mCache = new HashMap<>();

    void clear() {
        mCache.clear();
    }

    void put(IConditional conditional, boolean status) {
        mCache.put(conditional, status);
    }

    Boolean get(IConditional conditional) {
        return mCache.get(conditional);
    }
}
