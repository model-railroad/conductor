package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.script.impl.IExecEngine

internal class ExecEngine(val conductor: ConductorImpl) : IExecEngine {

    override fun onExecStart() {
    }

    override fun onExecHandle() {
        // First collect all rules with an active condition.
        val activeRules = conductor.rules.filter { it.evaluateCondition() }

        // TBD also add rules from any currently active route, in order.

        // Second execute all actions in the order they are defined.
        activeRules.forEach { it.evaluateAction() }
    }
}
