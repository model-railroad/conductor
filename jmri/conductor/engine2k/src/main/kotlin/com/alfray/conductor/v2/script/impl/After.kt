package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.IAfter
import com.alfray.conductor.v2.script.IThenAfter
import com.alfray.conductor.v2.script.ITimer
import com.alfray.conductor.v2.script.TAction

class After(val timer: ITimer) : IAfter {
    private lateinit var action: TAction
    private var thenAfter: IAfter? = null

    override fun then(action: TAction) : IThenAfter {
        this.action = action
        return object : IThenAfter {
            override fun and_after(timer: ITimer): IAfter {
                val after = After(timer)
                thenAfter = after
                return after
            }
        }
    }
}

