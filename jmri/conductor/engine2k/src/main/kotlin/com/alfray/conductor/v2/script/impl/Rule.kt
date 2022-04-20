package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.IActive
import com.alfray.conductor.v2.script.IRule
import com.alfray.conductor.v2.script.TAction
import com.alfray.conductor.v2.script.TCondition

class Rule(private val condition: TCondition) : IRule {
    private lateinit var action: TAction

    override fun then(action: TAction) {
        this.action = action
    }

    fun evaluateCondition() : Boolean {
        val result = condition.invoke()
        println("Rule eval condition: $result")
        val cond : Boolean =
            when (result) {
                is Boolean -> result
                is IActive -> result.active
                else -> throw IllegalArgumentException("Invalid Condition Return type")
            }
        println("Rule eval condition: $result -> $cond")
        return cond
    }

    fun evaluateAction() {
        println("Rule eval action: $action")
        action.invoke()
    }
}
