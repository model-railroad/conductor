package com.alflabs.conductor.v2.script

interface IRule {
    boolean evaluateCondition();
    void evaluateAction(RootScript rootScript);
}
