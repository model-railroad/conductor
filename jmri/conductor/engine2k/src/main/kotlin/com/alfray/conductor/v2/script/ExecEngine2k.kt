package com.alfray.conductor.v2.script

import com.alflabs.conductor.util.FrequencyMeasurer
import com.alflabs.conductor.util.RateLimiter
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.impl.IExecEngine
import com.alfray.conductor.v2.script.impl.Rule
import javax.inject.Inject

const val TAG = "ExecEngine2k"

@Script2kScope
class ExecEngine2k @Inject constructor(
    val conductor: ConductorImpl,
    private val clock: IClock,
    private val logger: ILogger,
) : IExecEngine {
    private val mHandleFrequency = FrequencyMeasurer(clock)
    private val mHandleRateLimiter = RateLimiter(30.0f, clock)

    override fun onExecStart() {
    }

    override fun onExecHandle() {
        mHandleFrequency.startWork()

        evalScript()

        mHandleFrequency.endWork()
        mHandleRateLimiter.limit()
    }

    private fun evalScript() {
        // First collect all rules with an active condition.
        val activeRules = conductor.rules.filter {
            var result = false
            try {
                result = (it as Rule).evaluateCondition()
            } catch (t: Throwable) {
                logger.d(TAG, "Eval Condition Failed", t)
            }
            return@filter result
        }

        // TBD also add rules from any currently active route, in order.

        // Second execute all actions in the order they are defined.
        activeRules.forEach {
            try {
                (it as Rule).evaluateAction()
            } catch (t: Throwable) {
                logger.d(TAG, "Eval Action Failed", t)
            }
        }
    }

    fun getActualFrequency(): Float {
        return mHandleFrequency.actualFrequency
    }

    fun getMaxFrequency(): Float {
        return mHandleFrequency.maxFrequency
    }

}
