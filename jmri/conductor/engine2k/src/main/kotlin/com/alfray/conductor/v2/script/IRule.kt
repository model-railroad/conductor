package com.alfray.conductor.v2.script

interface IRule {
    infix fun then(action: () -> Unit): Unit
}
