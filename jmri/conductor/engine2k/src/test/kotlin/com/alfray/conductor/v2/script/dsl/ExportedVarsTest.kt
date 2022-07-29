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
    fun testExported_empty() {
        exportedVars.export()

        val kv = keyValue.keys
            .sorted()
            .map { "$it=" + keyValue.getValue(it) }
            .toList()
        assertThat(kv).containsExactly(
            // "V/\$ga-id\$=", -- not exported when empty
            "V/conductor-time=0",
            "V/rtac-motion=OFF",
            "V/rtac-psa-text=",
        ).inOrder()
    }

    @Test
    fun testExported() {
        exportedVars.Conductor_Time = 1234
        exportedVars.JSON_URL = "json://url"    // not exported
        exportedVars.GA_URL = "ga://url"        // not exported
        exportedVars.GA_Tracking_Id = "AB-cdEF"
        exportedVars.RTAC_PSA_Text = "Automation Running"
        exportedVars.RTAC_Motion = true

        exportedVars.export()

        val kv = keyValue.keys
            .sorted()
            .map { "$it=" + keyValue.getValue(it) }
            .toList()
        assertThat(kv).containsExactly(
            "V/\$ga-id\$=AB-cdEF",
            "V/conductor-time=1234",
            "V/rtac-motion=ON",
            "V/rtac-psa-text=Automation Running",
        ).inOrder()
    }
}
