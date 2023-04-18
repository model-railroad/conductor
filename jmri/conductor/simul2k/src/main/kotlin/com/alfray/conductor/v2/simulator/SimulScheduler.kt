package com.alfray.conductor.v2.simulator

import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimulScheduler @Inject constructor(
    private val logger: Lazy<ILogger>,
    private val clock: IClock,
) : IExecSimul {
    private val TAG = javaClass.simpleName
    private val functionsAt = mutableListOf<FunctionAt>()

    fun scheduleAfter(millis: Int, tag: Any, function: () -> Unit) {
        val nowMs = clock.elapsedRealtime()
        val timeMs = nowMs + millis
        logger.get().d(TAG, "Schedule after +$millis ms = TS $nowMs [$tag]")
        functionsAt.add(FunctionAt(timeMs, tag, function))
        functionsAt.sortBy { timeMs }
    }

    fun forceExec(tag: Any) {
        val nowMs = clock.elapsedRealtime()
        functionsAt.removeAll {
            val delta = nowMs - it.timeMs
            if (it.tag == tag) {
                logger.get().d(TAG, "Exec force +$delta ms = TS $nowMs [${tag}]")
                it.function.invoke()
                return@removeAll true
            } else {
                return@removeAll false
            }
        }
    }

    override fun onExecStart() {
        // no-op
    }

    override fun onExecHandle() {
        val nowMs = clock.elapsedRealtime()
        functionsAt.removeAll {
            val delta = nowMs - it.timeMs
            if (delta >= 0) {
                logger.get().d(TAG, "Exec invoke +$delta ms = TS $nowMs [${it.tag}]")
                it.function.invoke()
                return@removeAll true
            } else {
                return@removeAll false
            }
        }
    }

    data class FunctionAt(val timeMs: Long, val tag: Any, val function: () -> Unit)
}
