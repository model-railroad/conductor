package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.IActive
import com.alfray.conductor.v2.script.IRule
import com.alfray.conductor.v2.script.TAction
import com.alfray.conductor.v2.script.TCondition

private const val VERBOSE = false

class Rule(private val condition: TCondition) : IRule {
    private lateinit var action: TAction

    override fun then(action: TAction) {
        this.action = action
    }

    fun evaluateCondition() : Boolean {
        val result = condition.invoke()
        if (VERBOSE) println("Rule eval condition: $result")
        val cond : Boolean =
            when (result) {
                is Boolean -> result
                is IActive -> result.active
                else -> throw IllegalArgumentException("Invalid Condition Return type")
            }
        if (VERBOSE) println("Rule eval condition: $result -> $cond")
        return cond
    }

    fun evaluateAction() {
        if (VERBOSE) println("Rule eval action: $action")
        action.invoke()
    }
}
