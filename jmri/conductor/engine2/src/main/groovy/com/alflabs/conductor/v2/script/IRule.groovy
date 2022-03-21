package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

interface IRule {
    boolean evaluateCondition();
    void evaluateAction(@NonNull RootScript rootScript);
}
