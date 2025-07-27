package com.alfray.dazzserv

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class DazzServTest {
    @Before
    fun setUp() {
        println("DazzServTest.setUp")
    }

    @After
    fun tearDown() {
        println("DazzServTest.tearDown")
    }

    @Test
    fun testOnStart() {
        println("DazzServTest.onStart")
        assertThat(true).isFalse()
    }
}
