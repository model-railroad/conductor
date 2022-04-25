package com.alfray.conductor.v2.script.dsl

import com.alfray.conductor.v2.script.TAction

interface IRule {
    infix fun then(action: TAction)
}

val RuleActionEmpty : TAction = {}
