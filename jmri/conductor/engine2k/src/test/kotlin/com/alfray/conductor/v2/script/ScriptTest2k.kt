package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.host.ConductorScriptHost
import com.google.common.io.Resources
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import kotlin.script.experimental.host.UrlScriptSource

@Suppress("UnstableApiUsage")
class ScriptTest2k {

    @Before
    fun setUp() {
        println("Test before")
    }

    @Test
    fun loadScriptAndEval() {
        val scriptPath = "v2/script/sample_v2.conductor.kts"
        val scriptUrl = Resources.getResource(scriptPath)!!
        val source = UrlScriptSource(scriptUrl)

        val conductorImpl = ConductorImpl()
        val host = ConductorScriptHost()
        val result = host.eval(source, conductorImpl)
        assertThat(result).isNotNull()
    }
}
