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
    private val handleFrequency = FrequencyMeasurer(clock)
    private val handleRateLimiter = RateLimiter(30.0f, clock)
    private val activatedRules = mutableListOf<Rule>()
    private val ruleCondCache = BooleanCache<Rule>()
    private val ruleExecCache = BooleanCache<Rule>()

    override fun onExecStart() {
    }

    override fun onExecHandle() {
        handleFrequency.startWork()

        evalScript()

        handleFrequency.endWork()
        handleRateLimiter.limit()
    }

    private fun evalScript() {
        ruleCondCache.clear()
        activatedRules.clear()

        // First collect all rules with an active condition that have not been
        // executed yet
        for (r in conductor.rules) {
            val rule = r as Rule
            val active = ruleCondCache.getOrEval(rule) {
                var result = false
                try {
                    result = rule.evaluateCondition()
                } catch (t: Throwable) {
                    logger.d(TAG, "Eval Condition Failed", t)
                }
                result
            }

            // Rules only get executed once when activated and until
            // the condition is cleared and activated again.
            if (active) {
                if (!ruleExecCache.get(rule)) {
                    activatedRules.add(rule)
                }
            } else {
                ruleExecCache.remove(rule)
            }
        }

        // TBD also add rules from any currently active route, in order.

        // Second execute all actions in the order they are queued.
        for (rule in activatedRules) {
            try {
                rule.evaluateAction()
            } catch (t: Throwable) {
                logger.d(TAG, "Eval Action Failed", t)
            }
        }
    }

    fun getActualFrequency(): Float {
        return handleFrequency.actualFrequency
    }

    fun getMaxFrequency(): Float {
        return handleFrequency.maxFrequency
    }

}
