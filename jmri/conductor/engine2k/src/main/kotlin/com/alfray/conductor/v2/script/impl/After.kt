package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.IAfter
import com.alfray.conductor.v2.script.dsl.IThenAfter
import com.alfray.conductor.v2.script.TAction

internal class After(val delay: Delay) : IAfter {
    private lateinit var action: TAction
    private var thenAfter: IAfter? = null

    override fun then(action: TAction) : IThenAfter {
        this.action = action
        return object : IThenAfter {
            override fun and_after(delay: Delay): IAfter {
                val after = After(delay)
                thenAfter = after
                return after
            }
        }
    }
}

