/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alfray.conductor.v2

import com.alfray.conductor.v2.script.dsl.TAction
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class KtReceiverTest {
    private var argFromMethod1: (() -> Unit)? = null
    private var argFromMethodAction: (() -> Unit)? = null
    private var argExecCounter = 0

    @Before
    fun setUp() {
    }

    @Test
    fun testMethod1() {
        assertThat(argFromMethod1).isNull()
        assertThat(argExecCounter).isEqualTo(0)

        method1 {
            println("argument to method1")
            argExecCounter++
        }
        println("after definition, before execution")
        assertThat(argFromMethod1).isNotNull()
        assertThat(argExecCounter).isEqualTo(0)

        argFromMethod1!!.invoke()
        assertThat(argFromMethod1).isNotNull()
        assertThat(argExecCounter).isEqualTo(1)
    }

    fun method1(arg: () -> Unit) {
        argFromMethod1 = arg
    }

    @Test
    fun testMethodAction() {
        assertThat(argFromMethodAction).isNull()
        assertThat(argExecCounter).isEqualTo(0)

        methodAction {
            println("argument to methodAction")
            argExecCounter++
        }
        println("after definition, before execution")
        assertThat(argFromMethodAction).isNotNull()
        assertThat(argExecCounter).isEqualTo(0)

        argFromMethodAction!!.invoke()
        assertThat(argFromMethodAction).isNotNull()
        assertThat(argExecCounter).isEqualTo(1)
    }

    fun methodAction(arg: TAction) {
        argFromMethodAction = arg
    }

    @Test
    fun testFooBuilder() {
        assertThat(argExecCounter).isEqualTo(0)

        val f = foo {
            println("exec foo Builder")

            doSomething {
                println("exec doSomething")
                argExecCounter++
            }
        }
        println("after definition, before execution")
        assertThat(f.someAction).isNotNull()
        assertThat(argExecCounter).isEqualTo(0)

        f.someAction!!.invoke()
        assertThat(argExecCounter).isEqualTo(1)
    }

    interface IFooBuilderInterface {
        fun doSomething(action: TAction)
    }

    class FooBuilder: IFooBuilderInterface {
        var someAction: TAction? = null

        override fun doSomething(action: TAction) {
            someAction = action
        }

        fun create(): Foo = Foo(this)
    }

    class Foo(fooBuilder: FooBuilder) {
        val someAction = fooBuilder.someAction
    }

    fun foo(fooSpecification: IFooBuilderInterface.() -> Unit): Foo {
        val fb = FooBuilder()
        fb.fooSpecification()
        return fb.create()
    }

}

