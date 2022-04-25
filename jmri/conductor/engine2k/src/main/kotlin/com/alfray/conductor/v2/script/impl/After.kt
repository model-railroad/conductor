package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.Delay
import com.alfray.conductor.v2.script.IAfter
import com.alfray.conductor.v2.script.IThenAfter
import com.alfray.conductor.v2.script.TAction

class After(val delay: Delay) : IAfter {
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

