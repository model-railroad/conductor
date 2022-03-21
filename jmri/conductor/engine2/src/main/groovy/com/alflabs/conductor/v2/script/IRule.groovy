package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull
import groovy.transform.PackageScope

interface IRule {
    boolean evaluateCondition();
    void evaluateAction(@NonNull RootScript rootScript);
}
