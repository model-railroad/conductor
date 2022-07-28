package com.alfray.conductor.v2.script.dsl

import com.alflabs.kv.IKeyValue
import com.alfray.conductor.v2.script.ScriptTest2kBase
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class ExportedVarsTest: ScriptTest2kBase() {

    @Inject lateinit var keyValue: IKeyValue
    @Inject lateinit var exportedVars: ExportedVars

    @Before
    fun setUp() {
        createComponent()
        scriptComponent.inject(this)
    }

    @Test
    fun testExported() {
        exportedVars.Conductor_Time = 1234
        exportedVars.JSON_URL = "json://url"
        exportedVars.GA_URL = "ga://url"
        exportedVars.GA_Tracking_Id = "12345"
        exportedVars.RTAC_PSA_Text = "Automation Running"
        exportedVars.RTAC_Motion = true

        exportedVars.export()

        val kv = keyValue.keys
            .sorted()
            .map { "$it=" + keyValue.getValue(it) }
            .toList()
        assertThat(kv).containsExactly(
            "Conductor-Time=1234",
            "GA-Tracking-Id=12345",
            "GA-URL=ga://url",
            "JSON-URL=json://url",
            "RTAC-Motion=true",
            "RTAC-PSA-Text=Automation Running",
        ).inOrder()
    }
}
