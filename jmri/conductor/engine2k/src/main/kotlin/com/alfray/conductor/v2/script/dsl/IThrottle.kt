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

package com.alfray.conductor.v2.script.dsl

data class DccSpeed(val speed: Int) {
    val stopped: Boolean
        get() = speed == 0
    val forward: Boolean
        get() = speed > 0
    val reverse: Boolean
        get() = speed < 0
    fun reverse() : DccSpeed =
        DccSpeed(-1 * speed)
}

val Int.speed: DccSpeed
    get() = DccSpeed(this)

/** DSL script interface for a JMRI Throttle. */
interface IThrottle : IVarName {
    val dccAddress: Int
    /** The last speed set for this engine. */
    val speed: DccSpeed
    val light: Boolean
    val sound: Boolean
    val stopped: Boolean
        get() = speed.stopped
    val forward: Boolean
        get() = speed.forward
    val reverse: Boolean
        get() = speed.reverse
    val activationCount: Int

    /**
     * Provides a script-defined name for this throttle that differs from the JMRI system name.
     * Can only be set once. Ignored if already set to the same value.
     */
    infix fun named(name: String) : IThrottle

    /** Increments [activationCount]. */
    fun incActivationCount()

    /** Sets the speed to forward on a DCC 28 speed scale. */
    fun forward(speed: DccSpeed)
    /** Sets the speed to reverse on a DCC 28 speed scale. */
    fun reverse(speed: DccSpeed)
    /** Sets the speed to 0. */
    fun stop()
    /** Sets the engine's light. Uses F2 for 500 ms. */
    fun horn()
    /** Sets the engine's light. Uses [IThrottleBuilder.onLight] or F0 by default. */
    fun light(on: Boolean)
    /** Sets the engine's sound. Uses [IThrottleBuilder.onSound] or !F8 by default. */
    fun sound(on: Boolean)
    /** Sets the engine's bell. Uses [IThrottleBuilder.onBell] or F1 by default. */
    fun bell(on: Boolean)
    /** Sets the repeat speed interval. Does nothing if <= 0. */
    fun repeat(repeat: Delay)

    val f: IFBits
    val f0: Boolean
        get() = f[0]
    val f1: Boolean
        get() = f[1]
    val f2: Boolean
        get() = f[2]
    val f3: Boolean
        get() = f[3]
    val f4: Boolean
        get() = f[4]
    val f5: Boolean
        get() = f[5]
    val f6: Boolean
        get() = f[6]
    val f7: Boolean
        get() = f[7]
    val f8: Boolean
        get() = f[8]
    val f9: Boolean
        get() = f[9]
    val f10: Boolean
        get() = f[10]
    val f11: Boolean
        get() = f[11]
    val f12: Boolean
        get() = f[12]
    val f13: Boolean
        get() = f[13]
    val f14: Boolean
        get() = f[14]
    val f15: Boolean
        get() = f[15]
    val f16: Boolean
        get() = f[16]
    val f17: Boolean
        get() = f[17]
    val f18: Boolean
        get() = f[18]
    val f19: Boolean
        get() = f[19]
    val f20: Boolean
        get() = f[20]
    val f21: Boolean
        get() = f[21]
    val f22: Boolean
        get() = f[22]
    val f23: Boolean
        get() = f[23]
    val f24: Boolean
        get() = f[24]
    val f25: Boolean
        get() = f[25]
    val f26: Boolean
        get() = f[26]
    val f27: Boolean
        get() = f[27]
    val f28: Boolean
        get() = f[28]

    fun f(index: Int, on: Boolean) : IFBits
    fun f0(on: Boolean) : IFBits = f(0, on)
    fun f1(on: Boolean) : IFBits = f(1, on)
    fun f2(on: Boolean) : IFBits = f(2, on)
    fun f3(on: Boolean) : IFBits = f(3, on)
    fun f4(on: Boolean) : IFBits = f(4, on)
    fun f5(on: Boolean) : IFBits = f(5, on)
    fun f6(on: Boolean) : IFBits = f(6, on)
    fun f7(on: Boolean) : IFBits = f(7, on)
    fun f8(on: Boolean) : IFBits = f(8, on)
    fun f9(on: Boolean) : IFBits = f(9, on)
    fun f10(on: Boolean) : IFBits = f(10, on)
    fun f11(on: Boolean) : IFBits = f(11, on)
    fun f12(on: Boolean) : IFBits = f(12, on)
    fun f13(on: Boolean) : IFBits = f(13, on)
    fun f14(on: Boolean) : IFBits = f(14, on)
    fun f15(on: Boolean) : IFBits = f(15, on)
    fun f16(on: Boolean) : IFBits = f(16, on)
    fun f17(on: Boolean) : IFBits = f(17, on)
    fun f18(on: Boolean) : IFBits = f(18, on)
    fun f19(on: Boolean) : IFBits = f(19, on)
    fun f20(on: Boolean) : IFBits = f(20, on)
    fun f21(on: Boolean) : IFBits = f(21, on)
    fun f22(on: Boolean) : IFBits = f(22, on)
    fun f23(on: Boolean) : IFBits = f(23, on)
    fun f24(on: Boolean) : IFBits = f(24, on)
    fun f25(on: Boolean) : IFBits = f(25, on)
    fun f26(on: Boolean) : IFBits = f(26, on)
    fun f27(on: Boolean) : IFBits = f(27, on)
    fun f28(on: Boolean) : IFBits = f(28, on)
}

interface IFBits {
    operator fun get(bit: Int) : Boolean
    operator fun set(bit: Int, on: Boolean) : IFBits
}
