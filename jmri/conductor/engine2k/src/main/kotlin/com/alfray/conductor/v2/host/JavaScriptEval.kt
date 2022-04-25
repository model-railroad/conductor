package com.alfray.conductor.v2.host

import com.alfray.conductor.v2.script.dsl.IConductor
import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import kotlin.script.experimental.api.SourceCode

// References:
// - https://github.com/JetBrains/kotlin/blob/master/libraries/scripting/jvm-host/src/kotlin/script/experimental/jvmhost/jsr223/KotlinJsr223ScriptEngineImpl.kt
// - https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/repl/KotlinJsr223JvmScriptEngineBase.kt
// - https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/repl/ReplState.kt

class JavaScriptEval {
    fun eval(source: SourceCode) : IConductor {
        val manager = ScriptEngineManager()
        val engine = manager.getEngineByExtension("kts")

        val ret = engine.eval(source.text)

        println("Eval: $ret")
        println("Engine Binding: " + engine.getBindings(ScriptContext.ENGINE_SCOPE))
        println("Global Binding: " + engine.getBindings(ScriptContext.GLOBAL_SCOPE))
        println("Context: " + engine.context)
        println("Context Engine Binding: " + engine.context.getBindings(ScriptContext.ENGINE_SCOPE))
        println("Context Global Binding: " + engine.context.getBindings(ScriptContext.GLOBAL_SCOPE))

        dumpVars("engine", engine.getBindings(ScriptContext.ENGINE_SCOPE))
        dumpVars("global", engine.getBindings(ScriptContext.GLOBAL_SCOPE))

        return ret as IConductor
    }

    private fun dumpVars(prefix: String, bindings: Bindings?) {
        for (entry in bindings!!.entries) {
            println(prefix + " [" + entry.key + "] => " + entry.value);
        }

        // Variables can be seen in:
        // bindings["kotlin.script.state"] as AggregatedReplStageState
        //  .state2 as JvmReplEvaluatorState
        //      .history as ReplStageHistoryWithReplace
        //          [0].item.second as ScriptingHost
        //              field B320...
        //              field res0 = CScriptBuilder (return value from script)

//        val state = bindings["kotlin.script.state"]
//        if (state is AggregatedReplStageState) {
//
//        }
    }
}
