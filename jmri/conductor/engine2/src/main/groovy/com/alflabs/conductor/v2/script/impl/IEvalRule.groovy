package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull
import com.alflabs.conductor.v2.script.RootScript
import groovy.transform.PackageScope

interface IEvalRule {
    boolean evaluateCondition();
    void evaluateAction(@NonNull RootScript rootScript);
}
