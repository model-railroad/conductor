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

interface IThrottle {
    val dccAddress: Int
    val speed: DccSpeed
    val light: Boolean
    val sound: Boolean
    val stopped: Boolean
        get() = speed.stopped
    val forward: Boolean
        get() = speed.forward
    val reverse: Boolean
        get() = speed.reverse

    fun forward(speed: DccSpeed)
    fun reverse(speed: DccSpeed)
    fun stop()
    fun horn()
    fun light(on: Boolean)
    fun sound(on: Boolean)
    fun repeat(repeat: Delay)

    val f: FBits
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

    fun f(index: Int, on: Boolean) : FBits = f.set(index, on)
    fun f0(on: Boolean) : FBits = f(0, on)
    fun f1(on: Boolean) : FBits = f(1, on)
    fun f2(on: Boolean) : FBits = f(2, on)
    fun f3(on: Boolean) : FBits = f(3, on)
    fun f4(on: Boolean) : FBits = f(4, on)
    fun f5(on: Boolean) : FBits = f(5, on)
    fun f6(on: Boolean) : FBits = f(6, on)
    fun f7(on: Boolean) : FBits = f(7, on)
    fun f8(on: Boolean) : FBits = f(8, on)
    fun f9(on: Boolean) : FBits = f(9, on)
    fun f10(on: Boolean) : FBits = f(10, on)
}

data class FBits(var f: Int = 0) {
    operator fun get(bit: Int) : Boolean = (f and (1 shl bit)) != 0
    operator fun set(bit: Int, on: Boolean) : FBits {
        f = (f and (1 shl bit).inv()) or ((if (on) 1 else 0) shl bit)
        return this
    }
}
