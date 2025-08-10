/* vim: set ai ts=4 sts=4 et sw=4 */
/*
 * Project: Conductor
 * Copyright (C) 2025 alf.labs gmail com,
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

// This test script is a fork of the latest runtime script_v33.03 simplified to
// only contain the Freight route; single route with no recovery. Every else is omitted.

@file:Suppress("FunctionName", "LocalVariableName", "PropertyName", "ClassName")

import com.alfray.conductor.v2.script.dsl.*


// Variables and local declaration.

val On = true
val Off = false

// sensors

val B311         = block ("NS769") named "B311"         // 49:2
val B321         = block ("NS771") named "B321"         // 49:4

val ML_Toggle    = sensor("NS829") named "ML-Toggle"    // 52:14


// turnouts

// n/a, all turnouts removed in this test.


// JSON tracking

exportedVars.jsonUrl = "invalid url for testing"
exportedVars.dazzUrl = "invalid url for testing"


// ---------------------
// Events & RTAC Display
// ---------------------

on { ML_Toggle  } then {
    jsonEvent {
        key1 = "Toggle"
        key2 = "Passenger"
        value = "On"
    }
    dazzEvent {
        key = "toggle/passenger"
        state = true
    }
}
on { !ML_Toggle } then {
    jsonEvent {
        key1 = "Toggle"
        key2 = "Passenger"
        value = "Off"
    }
    dazzEvent {
        key = "toggle/passenger"
        state = false
    }
}


// --------------------
// Events Mainline (ML)
// --------------------


var ML_Requires_Wait_After_Run = false

// FR is Beeline 1067 or 1072
// FR is 1225 for Polar Express
val FR = throttle(1072) {
    // Short mainline route -- Freight.
    name = "FR"
    onBell  { on -> throttle.f1(on) }
    onSound { on -> FR_sound(on) }
}


fun FR_sound(on: Boolean) {
    if (FR_Data.F8_is_Mute) {
        FR.f8(!on)
    } else {
        FR.f8(on)
    }
}

fun FR_marker (on: Boolean) {
    /* no-op on 1067, 1072 */
}

// --- Mainline Routes

val ML_Route = routes {
    name = "Mainline"
    toggle = ML_Toggle
    status = { "status" }
}

var ML_Idle_Route: IRoute
ML_Idle_Route = ML_Route.idle {
    // The idle route is where we check whether a mainline train should start, and which one.
    name = "ML Ready"

    onActivate {
        FR.stop()
        FR.light(On)
        FR.bell(Off)
    }

    onIdle {
        if (ML_Toggle.active && FR.stopped) {
            Freight_Route.activate()
        }
    }
}

val ML_Wait_Route = ML_Route.idle {
    // The wait route creates the pause between two mainline train runs.
    name = "ML Wait"

    // Wait 1 minute (or wait 2 if one of the routes is disabled)
    val ML_Timer_Wait = 1.minutes

    onActivate {
        ML_Requires_Wait_After_Run = false

        after (ML_Timer_Wait) then {
            ML_Idle_Route.activate()
        }
    }
}


// Speeds: Doodlebug: 8/4; RDC: 20/12; 804: 16/12/4; 6580: 8/6/2; 655: 16/12/8; 2468: 28/20/12; 1840: 20/16/12; 5278:16/16/12; 024: 8/6/2
// Default values currently for FR is Beeline 1067 or 1072
data class _FR_Data(
    /// True: F8=1 engages mute, 0 engages sound. False: F8=1 engages sound, 0 engages mute).
    // Tsunami Sound for 1067, 1072, 1225
    val F8_is_Mute: Boolean = true,

    val Forward_Speed: DccSpeed     = 8.speed,
    val Reverse_Speed: DccSpeed     = 4.speed,
    val Station_Speed: DccSpeed     = 2.speed,

    val Delay_Sound_Started: Delay  = 2.seconds,
    /// Time to go from Station speed to full Forward speed at startup.
    val Delay_Up_Station: Delay     = 10.seconds,
    /// B321 time to station speed: RDC=40, Doodlebug=60, 804=60.
    val Delay_Up_Slow: Delay        = 35.seconds,
    /// Time on slow down before stop
    val Delay_Up_Stop: Delay        = 14.seconds,
    /// Time stopped before reverse
    val Delay_Up_Reverse: Delay     = 28.seconds,
    val Delay_Reverse_Horn: Delay   = 2.seconds,
    /// Time before slow on B311. RDC=10, Doodlebug or 804=18. 024=21, 5278=12.
    val Delay_Down_Slow: Delay      = 24.seconds,
    /// Time on slow down before stop.
    val Delay_Down_Stop: Delay      = 6.seconds,
    val Delay_Down_Off: Delay       = 20.seconds,
    val Delay_Sound_Stopped: Delay  = 2.seconds,

    // Saturday Mode
    val Saturday_Speed: DccSpeed        = 6.speed,
    val Delay_Saturday_Into_B321: Delay = 10.seconds,
    val Delay_Saturday_Into_B503: Delay = 5.seconds,
    val Delay_Saturday_Stop: Delay      = 6.seconds,

    val PSA_Name: String = "Freight",

    val B321_maxSecondsOnBlock: Int = 3*60,
)

val FR_Data = _FR_Data()



val Freight_Route = ML_Route.sequence {
    // The mainline freight route sequence.
    name = "Freight"
    throttle = FR
    minSecondsOnBlock = 10
    maxSecondsOnBlock = 120 // 2 minutes per block max
    maxSecondsEnterBlock = 30

    onError {
        // no-op
    }

    onActivate {
        throttle.incActivationCount()
    }

    val B311_start = node(B311) {
        onEnter {
            ML_Requires_Wait_After_Run = true

            FR.light(On)
            FR_marker(On)
            FR.sound(On)
            after (FR_Data.Delay_Sound_Started) then {
                FR.horn()
                FR.light(On)
                FR.bell(Off)
                FR.forward(FR_Data.Station_Speed)
            } and_after (FR_Data.Delay_Up_Station) then {
                FR.forward(FR_Data.Forward_Speed)
                FR.horn()
            }
        }
    }

    val B321_fwd = node(B321) {
        maxSecondsOnBlock = FR_Data.B321_maxSecondsOnBlock
        onEnter {
            FR.forward(FR_Data.Forward_Speed)
            after (FR_Data.Delay_Up_Slow) then {
                FR.horn()
                FR.bell(On)
                FR.forward(FR_Data.Station_Speed)
            } and_after (FR_Data.Delay_Up_Stop) then {
                // Stop in B321. Normal case is to *not* go into B330.
                FR.horn()
                FR.stop()
                FR.bell(Off)
                // This is the long stop at the station.
            } and_after (1.seconds) then {
                FR.stop()
            } and_after (1.seconds) then {
                FR.stop()
            } and_after (FR_Data.Delay_Up_Reverse) then {
                // Start reversing after the long stop.
                FR.horn()
                FR.reverse(FR_Data.Reverse_Speed)
                FR.bell(On)
            } and_after (FR_Data.Delay_Reverse_Horn) then {
                FR.horn()
            } and_after (FR_Data.Delay_Reverse_Horn) then {
                FR.horn()
                FR.bell(Off)
                // Normal case: continue to B311_rev.
            }
        }
    }

    val B311_rev = node(B311) {
        onEnter {
            after (FR_Data.Delay_Down_Slow) then {
                FR.bell(On)
            } and_after (FR_Data.Delay_Down_Stop) then {
                FR.bell(Off)
                FR.horn()
                FR.stop()
            } and_after (FR_Data.Delay_Down_Off) then {
                FR.horn()
                FR.bell(Off)
                FR.light(Off)
                FR_marker(Off)
            } and_after (FR_Data.Delay_Sound_Stopped) then {
                FR.sound(Off)
                ML_Wait_Route.activate()
            }
        }
    }

    sequence = listOf(B311_start, B321_fwd, B311_rev)
}


// ---------
