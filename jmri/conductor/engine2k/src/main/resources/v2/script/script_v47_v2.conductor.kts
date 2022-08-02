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

@file:Suppress("FunctionName", "LocalVariableName", "PropertyName", "ClassName")

import com.alfray.conductor.v2.script.dsl.*


// Variables and local declaration.

val On = true
val Off = false

// sensors

val BLParked     = block ("NS752") named "BLParked"     // 48:1
val BLStation    = block ("NS753") named "BLStation"    // 48:2
val BLTunnel     = block ("NS754") named "BLTunnel"     // 48:3
val BLReverse    = block ("NS755") named "BLReverse"    // 48:4

val B310         = block ("NS768") named "B310"         // 49:1
val B311         = block ("NS769") named "B311"         // 49:2
val B320         = block ("NS770") named "B320"         // 49:3
val B321         = block ("NS771") named "B321"         // 49:4
val B330         = block ("NS773") named "B330"         // 49:6
val B340         = block ("NS774") named "B340"         // 49:7
val B360         = block ("NS775") named "B360"         // 49:8
val B370         = block ("NS776") named "B370"         // 49:9

val B503a        = block ("NS786") named "B503a"        // 50:3
val B503b        = block ("NS787") named "B503b"        // 50:4
val AIU_Motion   = sensor("NS797") named "AIU-Motion"   // 50:14

val BL_Toggle    = sensor("NS828") named "BL-Toggle"    // 52:13
val PA_Toggle    = sensor("NS829") named "PA-Toggle"    // 52:14


// turnouts

val T311        = turnout("NT311")
val T320        = turnout("NT320")
val T321        = turnout("NT321")
val T322        = turnout("NT322")
val T324        = turnout("NT324")
val T326        = turnout("NT326")
val T330        = turnout("NT330")
val T370        = turnout("NT370")
val T450        = turnout("NT450")  // SW8 - Bridgeport
val T504        = turnout("NT504")
val T410        = turnout("NT410")  // DS64 - Sultan
val T150        = turnout("NT150")  // DS64 - Mainline to Sultan
val T151        = turnout("NT151")  // DS64 - Mainline to Napa Yard
val T160        = turnout("NT160")  // DS64 - to Richmond Yard


// Maps

map {
    name = "Mainline"
    svg  = "Map Mainline 1.svg"   // relative to script
}

// JSON tracking

exportedVars.JSON_URL = "@~/bin/JMRI/rtac_json_url.txt"

// GA Tracking

exportedVars.GA_Tracking_Id = "@~/bin/JMRI/rtac_ga_tracking_id.txt"
exportedVars.GA_URL = "http://consist.alfray.com/train/"


// -----------------
// Motion
// -----------------


// ---------
// End of the Day
// ---------


// ---------
// Events PA (mainline)
// ---------

enum class EPA_State { Idle, Station, Running, Manual, Error, Wait }
enum class EPA_Train { Passenger, Freight, }

var PA_State = EPA_State.Idle
var PA_Train = EPA_Train.Passenger

val AM = throttle(8749) named "AM"     // Full Amtrak route
val SP = throttle(1072) named "SP"     // "Short Passenger" (now Freight) on limited Amtrak route

fun PA_Fn_Release_Route() {
    T311.normal()
    T320.normal()
    T321.normal()
}

val PA_Route = activeRoute {
    name = "Mainline"
    toggle = PA_Toggle
    status = { "$PA_State $PA_Train" }

    onError {
        // Note this state can only be cleared using an RTAC reset.
        PA_State = EPA_State.Error
        estop()
    }
}

val PA_Idle_Route = PA_Route.idle {
    onActivate {
        AM.stop()
        AM.light(On)
        AM.f1(Off)
    }
}

val Passenger_Route = PA_Route.sequence {
    throttle = AM
    timeout = 60 // 1 minute

    val AM_Leaving_Speed    = 6.speed
    val AM_Station_Speed    = 12.speed
    val AM_Summit_Speed     = 6.speed
    val AM_Summit_Bridge_Speed = 4.speed
    val AM_Sonora_Speed     = 8.speed
    val AM_Crossover_Speed  = 4.speed
    val AM_Full_Speed       = 12.speed

    val AM_Delayed_Horn             = 2.seconds
    val AM_Leaving_To_Full_Speed    = 15.seconds
    val AM_Timer_B321_Up_Doppler    = 27.seconds
    val AM_Timer_B330_Up_Resume     = 12.seconds
    val AM_Timer_B340_Up_Horn       = 5.seconds
    val AM_Timer_B370_Forward_Stop  = 17.seconds  // time running at AM_Summit_Speed before stopping
    val AM_Timer_B370_Pause_Delay   = 16.seconds
    val AM_Timer_B360_Full_Reverse  = 12.seconds
    val AM_Timer_B330_Down_Speed    = 8.seconds
    val AM_Timer_B321_Down_Crossover = 27.seconds
    val AM_Timer_B503b_Down_Stop    = 20.seconds
    val AM_Timer_Down_Station_Lights_Off = 10.seconds

    fun AM_Fn_Acquire_Route() {
        T311.reverse()
        T320.normal()
        T321.normal()
        T322.normal()
        T326.normal()
        T330.reverse()
        T370.normal()
        T504.normal()
    }

    onActivate {
        AM.light(On)
        AM.f1(Off)
        AM.sound(On)
        SP.sound(Off)
    }

    val B503b_start = node(B503b) {
        onEnter {
            SP.sound(Off)
            AM.horn()
            AM_Fn_Acquire_Route()
            AM.f1(On)
            after(AM_Delayed_Horn) then {
                AM.horn()
                AM.forward(AM_Leaving_Speed)
            }
        }
    }

    val B503a_fwd = node(B503a) {
        whileOccupied {
            AM.f1(Off)
        }
    }

    val B321_fwd = node(B321) {
        onEnter {
            // Wait for train to have cleared up T320 before switching to full speed.
            // The problem is that B321 activates as train hits diverted leg of turnout.
            after(AM_Leaving_To_Full_Speed) then {
                if (PA_Toggle.active) {
                    AM.forward(AM_Full_Speed)
                }
            }

            // Mid_Station doppler on the way up
            after(AM_Timer_B321_Up_Doppler) then {
                AM.f9(On)
            } and_after(1.seconds) then {
                AM.f9(Off)
            }
        }

        whileOccupied {
            // PA_Toggle off reverts train to down but only on specific "safe" blocks
            // (e.g. not in B503a/B503b because stop distances puts us in the next block)
            if (!PA_Toggle) {
                AM.reverse(AM_Summit_Speed)
            }
        }
    }

    val B330_fwd = node(B330) {
        onEnter {
            // Sonora speed reduction
            AM.forward(AM_Sonora_Speed)
            after(AM_Timer_B330_Up_Resume) then {
                AM.forward(AM_Full_Speed)
                AM.horn()
            }
        }

        whileOccupied {
            // PA_Toggle off reverts train to down but only on specific "safe" blocks
            // (e.g. not in B503a/B503b because stop distances puts us in the next block)
            if (!PA_Toggle) {
                AM.reverse(AM_Summit_Speed)
            }
        }
    }

    val B340_fwd = node(B340) {
        onEnter {
            // After tunnel on the way up
            after(AM_Timer_B340_Up_Horn) then {
                AM.horn()
            }
        }

        whileOccupied {
            // PA_Toggle off reverts train to down but only on specific "safe" blocks
            // (e.g. not in B503a/B503b because stop distances puts us in the next block)
            if (!PA_Toggle) {
                AM.reverse(AM_Summit_Speed)
            }
        }
    }

    //--- PA State: AM Summit

    val B360_fwd = node(B360) {
    }

    val B370_end = node(B370) {
        onEnter {
            after(4.seconds) then {
                // Forward
                AM.forward(AM_Summit_Speed)
                AM.f1(On)
            } and_after(AM_Timer_B370_Forward_Stop) then {
                AM.stop()
                AM.horn()
            } and_after(AM_Timer_B370_Pause_Delay) then {
                // Stopped
                AM.f1(Off)
                AM.horn()
                AM.reverse(AM_Summit_Speed)
            }
        }
    }

    val B360_rev = node(B360) {
        onEnter {
            after(AM_Timer_B360_Full_Reverse) then {
                AM.reverse(AM_Full_Speed)
            }
        }
    }

    val B340_rev = node(B340) {
    }

    val B330_rev = node(B330) {
        whileOccupied {
            after(AM_Timer_B330_Down_Speed) then {
                AM.horn()
                AM.reverse(AM_Sonora_Speed)
            }
        }
    }

    val B321_rev = node(B321) {
        whileOccupied {
            AM.reverse(AM_Full_Speed)
            // Doppler sound
            AM.f9(On)
            after(1.seconds) then {
                AM.f9(Off)
            }
            after(AM_Timer_B321_Down_Crossover) then {
                AM.horn()
                AM.f1(On)
                AM.reverse(AM_Crossover_Speed)
            }
        }
    }

    val B503a_rev = node(B503a) {
    }

    val B503b_rev = node(B503b) {
        whileOccupied {
            after(AM_Timer_B503b_Down_Stop) then {
                AM.stop()
                AM.horn()
                AM.f1(Off)
            } and_after (AM_Timer_Down_Station_Lights_Off) then {
                // TBD insert ga_event
                PA_Train = EPA_Train.Freight
                PA_State = EPA_State.Wait
                route.activate(PA_Idle_Route)
            }
        }
    }

    sequence = listOf(
        B503b_start, B503a_fwd, B321_fwd, B330_fwd, B340_fwd, B360_fwd,
        B370_end,
        B360_rev, B340_rev, B330_rev, B321_rev, B503a_rev, B503b_rev)
}



// --- PA State: Idle

on { PA_State == EPA_State.Idle && PA_Toggle.active } then {
    AM.sound(On);   AM.light(On)
    SP.sound(On);   SP.light(On)
    AM.stop();      SP.stop()
    AM.repeat(2.seconds);
    SP.repeat(2.seconds)
    PA_Fn_Release_Route()
    PA_State = EPA_State.Station
}

on { PA_Toggle } then {
    AM.repeat(2.seconds)
    SP.repeat(2.seconds)
}

on { !PA_Toggle } then {
    AM.repeat(2.seconds)
    SP.repeat(2.seconds)
    AM.f1(Off)
    SP.f1(Off)
}

// --- PA State: Wait (to station)

val PA_Timer_Wait = 60.seconds  // 1 minute

on { PA_State == EPA_State.Wait } then {
    after(PA_Timer_Wait) then {
        PA_State = EPA_State.Station
    }
}


// --- PA State: Station

// Departure from Station (going up)
// REQUIRES both trains stopped.
on { PA_Train == EPA_Train.Passenger
        && PA_State == EPA_State.Station
        && AM.stopped && SP.stopped
        && PA_Toggle.active
        && AIU_Motion.active } then {
    PA_State = EPA_State.Running
    PA_Route.activate(Passenger_Route)
}

on { PA_State == EPA_State.Station && !PA_Toggle } then {
    PA_State = EPA_State.Idle
    PA_Route.activate(PA_Idle_Route)
}


// ---------
// Events BL
// ---------

// TBD


// ------------------
// Automatic Turnouts
// ------------------

// If automation is off, T330 is automatically selected:
// B320 -> B330 via T330 Normal
// B321 -> B330 via T330 Reverse

on { !PA_Toggle && !B330 &&  B320.active && !B321 } then { T330.normal() }
on { !PA_Toggle && !B330 && !B320 &&  B321.active } then { T330.reverse() }


// ---------
