@file:Suppress("FunctionName")

package com.alfray.conductor.v2.script

interface IAfter {
    infix fun then(action: TAction) : IThenAfter
}

interface IThenAfter {
    infix fun and_after(timer: ITimer) : IAfter
}
