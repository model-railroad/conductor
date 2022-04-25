package com.alfray.conductor.v2.script

internal class ExecEngine(val conductor: ConductorImpl) : IExecEngine {

    override fun executeRules() {
        // First collect all rules with an active condition.
        val activeRules = conductor.rules.filter { it.evaluateCondition() }

        // TBD also add rules from any currently active route, in order.

        // Second execute all actions in the order they are defined.
        activeRules.forEach { it.evaluateAction() }
    }
}
