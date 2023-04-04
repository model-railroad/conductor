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

    fun scheduleAfter(millis: Int, function: () -> Unit) {
        val nowMs = clock.elapsedRealtime()
        val timeMs = nowMs + millis
        logger.get().d(TAG, "[schedule] after $millis --> TS $nowMs [$function]")
        functionsAt.add(FunctionAt(timeMs, function))
        functionsAt.sortBy { timeMs }
    }


    override fun onExecStart() {
        // no-op
    }

    override fun onExecHandle() {
        val nowMs = clock.elapsedRealtime()
        functionsAt.removeAll {
            val delta = nowMs - it.timeMs
            if (delta >= 0) {
                logger.get().d(TAG, "[schedule] invoke +$delta ms --> TS $nowMs [${it.function}]")
                it.function.invoke()
                return@removeAll true
            } else {
                return@removeAll false
            }
        }
    }

    data class FunctionAt(val timeMs: Long, val function: () -> Unit)
}
