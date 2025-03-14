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
val ML_Toggle    = sensor("NS829") named "ML-Toggle"    // 52:14


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
    displayOn = SvgMapTarget.Conductor
}
map {
    name = "Mainline"
    svg  = "Map Mainline 1.svg"   // relative to script
    displayOn = SvgMapTarget.RTAC
}

// JSON tracking

exportedVars.jsonUrl = "@~/bin/JMRI/rtac_json_url.txt"

// GA Tracking

analytics.configure("@~/bin/JMRI/rtac_ga_tracking_id.txt")
val GA_URL = "http://consist.alfray.com/train/"


// -----------------
// Motion
// -----------------

var AIU_Motion_Counter = 0

on { AIU_Motion } then {
    exportedVars.rtacMotion = On
    AIU_Motion_Counter += 1
    analytics.gaEvent {
        category = "Motion"
        action = "Start"
        label = "AIU"
        user = AIU_Motion_Counter.toString()
    }
}

on { !AIU_Motion } then {
    exportedVars.rtacMotion = Off
    analytics.gaEvent {
        category = "Motion"
        action = "Stop"
        label = "AIU"
        user = AIU_Motion_Counter.toString()
    }
}

// ---------
// End of the Day
// ---------

val End_Of_Day_HHMM = 1650

on { ML_Toggle.active && exportedVars.conductorTime == End_Of_Day_HHMM } then {
    ML_Toggle.active(Off)
}

on { BL_Toggle.active && exportedVars.conductorTime == End_Of_Day_HHMM } then {
    BL_Toggle.active(Off)
}

on { ML_Toggle } then {
    exportedVars.rtacPsaText = "Automation Started"
}

on { !ML_Toggle && exportedVars.conductorTime == End_Of_Day_HHMM } then {
    exportedVars.rtacPsaText = "{c:red}Automation Turned Off\nat 4:50 PM"
}

on { !ML_Toggle && exportedVars.conductorTime != End_Of_Day_HHMM } then {
    exportedVars.rtacPsaText = "{c:red}Automation Stopped"
}

// ---------------------
// Events & RTAC Display
// ---------------------

on { ML_Toggle  } then {
    analytics.gaEvent {
        category = "Automation"
        action = "On"
        label = "Passenger"
        user = "Staff"
    }
    jsonEvent {
        key1 = "Toggle"
        key2 = "Passenger"
        value = "On"
    }
}
on { !ML_Toggle } then {
    analytics.gaEvent {
        category = "Automation"
        action = "Off"
        label = "Passenger"
        user = "Staff"
    }
    jsonEvent {
        key1 = "Toggle"
        key2 = "Passenger"
        value = "Off"
    }
}

on { ML_Toggle } then {
    PA.repeat(2.seconds)
    FR.repeat(2.seconds)
}

on { !ML_Toggle } then {
    PA.repeat(2.seconds)
    FR.repeat(2.seconds)
    PA_bell(Off)
    FR_bell(Off)
}


on { ML_State == EML_State.Ready && ML_Toggle.active && ML_Train == EML_Train.Passenger } then {
    exportedVars.rtacPsaText = "{c:blue}Next Train:\n\nPassenger"
}
on { ML_State == EML_State.Wait  && ML_Toggle.active && ML_Train == EML_Train.Passenger } then {
    exportedVars.rtacPsaText = "{c:blue}Next Train:\n\nPassenger\n\nLeaving in 1 minute"
}
on { ML_State == EML_State.Ready && ML_Toggle.active && ML_Train == EML_Train.Freight  } then {
    exportedVars.rtacPsaText = "{c:#FF008800}Next Train:\n\nFreight"
}
on { ML_State == EML_State.Wait  && ML_Toggle.active && ML_Train == EML_Train.Freight  } then {
    exportedVars.rtacPsaText = "{c:#FF008800}Next Train:\n\nFreight\n\nLeaving in 1 minute"
}

// --------------------
// Events Mainline (ML)
// --------------------

/*
Note on sound decoders: typically LokSound as a "sound" function (a.k.a F8=1 for sound),
whereas Tsunami/BLI has a "mute" function (a.k.a F8=0 for _no_ sound).
MTH works very differently ==> this thing is basically unusable for automation
as most functions need to be set on twice as they acts as a toggle so we can't know the actual state.
TCS WOW is also best avoided on automation as may features require multiple presses of the
same button so it's impossible for this script to guarantee the state. Basically in the MTH and
TCS system, the human is the feedback loop to know how many button presses to issue, and we don't
have that feedback with the write-only DCC system with no programmatic read back of function states.


Amtrak engines: Rapido F40PH, both on address 204
- F0 is light (use PA Light)
- F1 is bell
- F2 is horn (use PA Horn)
- F3 = horn doppler effect (set F3 1 then 0)
- F6 = Strobe lights
- F8 is sound (0 for mute, 1 for sound, LokSound)
- F9 = Red Markers (end unit)

Amtrak engines: Atlas Master line GE Dash 8-32BWH
- F0 is light (use PA Light)
- F1 is bell
- F2 is horn (use PA Horn)
- F9 is horn grade crossing formation (set F9 1 then 0)
>> Timing for CV Momentum: CV 3 = CV 4 = 18

Walthers Mainline UP 8736 / 8749:
>> Timings for NCE Momentum #2

On the RDC SP-10:
- F0 is light (use BL Light)
- F1 is bell
- F2 is horn (use BL Horn)
- F5 is a doppler horn, too long so not using it
- F6 is disabled (function used by ESU PowerPack)
- F7 for dim lights for stations
- F8 is sound (0 for mute, 1 for sound, LokSound)
- F9 for red markers on the "end" side
>> Timings for NCE Momentum #3
*/

enum class EML_State { Ready, Wait, Running, Recover }
enum class EML_Train { Passenger, Freight, }

var ML_State = EML_State.Ready
var ML_Train = EML_Train.Passenger
var ML_Start_Counter = 0
val ML_Timer_Wait = 60.seconds  // 1 minute

val PA = throttle(8749) named "PA"     // Full mainline route -- Passenger.
val FR = throttle(1072) named "FR"     // Short mainline route -- Freight.

fun PA_bell   (on: Boolean) { PA.f1(on)  }
fun PA_sound  (on: Boolean) { PA.f8(!on) }
fun PA_doppler(on: Boolean) { /* no-op on 8749 */ }
fun FR_bell   (on: Boolean) { FR.f1(on)  }
fun FR_sound  (on: Boolean) { FR.f8(!on) }
fun FR_marker (on: Boolean) { /* no-op on 1072 */ }

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

fun SP_Fn_Acquire_Route() {
    T311.normal()
    T320.normal()
    T321.normal()
    T322.normal()
    T326.normal()
    T330.reverse()
    T504.normal()
}

fun ML_Fn_Release_Route() {
    T311.normal()
    T320.normal()
    T321.normal()
}

val ML_Route = routes {
    name = "Mainline"
    toggle = ML_Toggle
    status = { "$ML_State $ML_Train" }

    onError {
        // The current route will trigger the corresponding ML_Recover_Route.
        PA.repeat(1.seconds)
        FR.repeat(1.seconds)
        PA.stop()
        FR.stop()
        PA_sound(Off) ; PA.light(Off) ; PA_bell(Off)
        FR_sound(Off) ; FR.light(Off) ; FR_bell(Off) ; FR_marker(Off)
        analytics.gaEvent {
            category = "Automation"
            action = "Error"
            label = "Passenger"
            user = "Staff"
        }
        exportedVars.rtacPsaText = "{b:red}{c:white}Automation ERROR"

        ML_Fn_Try_Recover_Route()
    }
}

var ML_Idle_Route: IRoute
ML_Idle_Route = ML_Route.idle {
    onActivate {
        ML_State = EML_State.Ready
        PA.stop()
        PA.light(On)
        PA_bell(Off)

        FR.stop()
        FR.light(On)
        FR_bell(Off)
    }

    onIdle {
        if (ML_Toggle.active && AIU_Motion.active && PA.stopped && FR.stopped) {
            when (ML_Train) {
                EML_Train.Passenger -> ML_Route.activate(Passenger_Route)
                EML_Train.Freight -> ML_Route.activate(Freight_Route)
            }
        }
    }
}

on { ML_State == EML_State.Ready && ML_Toggle.active } then {
    PA_sound(On);   PA.light(On)
    FR_sound(On);   FR.light(On)
    PA.stop();      FR.stop()
    ML_Fn_Release_Route()
}

val ML_Timer_Stop_Sound_Off = 10.seconds
on { ML_State == EML_State.Ready && !ML_Toggle } then {
    PA.light(Off);  FR.light(Off)
    PA.stop();      FR.stop()
    after(ML_Timer_Stop_Sound_Off) then {
        PA_sound(Off)
        FR_sound(Off)
    }
    ML_Fn_Release_Route()
}

fun ML_Fn_Send_Start_GaEvent() {
    ML_Start_Counter += 1
    analytics.gaEvent {
        category = "Automation"
        action = "Start"
        label = ML_Train.name
        user = ML_Start_Counter.toString()
    }
    // reset_timers("PA", "AM", "SP") -- obsolete
}

val AM_Leaving_Speed        = 6.speed
val AM_Station_Speed        = 12.speed
val AM_Summit_Speed         = 6.speed
val AM_Summit_Bridge_Speed  = 4.speed
val AM_Sonora_Speed         = 8.speed
val AM_Crossover_Speed      = 4.speed
val AM_Full_Speed           = 12.speed

val AM_Delayed_Horn              = 2.seconds
val AM_Leaving_To_Full_Speed     = 15.seconds
val AM_Timer_B321_Up_Doppler     = 27.seconds
val AM_Timer_B330_Up_Resume      = 12.seconds
val AM_Timer_B340_Up_Horn        = 5.seconds
val AM_Timer_B370_Forward_Stop   = 17.seconds  // time running at AM_Summit_Speed before stopping
val AM_Timer_B370_Pause_Delay    = 16.seconds
val AM_Timer_B360_Full_Reverse   = 12.seconds
val AM_Timer_B330_Down_Speed     = 8.seconds
val AM_Timer_B321_Down_Crossover = 27.seconds
val AM_Timer_B503b_Down_Stop     = 20.seconds
val AM_Timer_Down_Station_Lights_Off = 10.seconds

val Passenger_Route = ML_Route.sequence {
    throttle = PA
    maxSecondsOnBlock = 60 // 1 minute

    onError {
        // no-op
    }

    onActivate {
        ML_Train = EML_Train.Passenger
        ML_State = EML_State.Running
        ML_Fn_Send_Start_GaEvent()
        exportedVars.rtacPsaText = "{c:blue}Currently Running:\n\nPassenger"
        jsonEvent {
            key1 = "Depart"
            key2 = "Passenger"
        }
        PA.light(On)
        PA_bell(Off)
        PA_sound(On)
        FR_sound(Off)
    }

    val B503b_start = node(B503b) {
        onEnter {
            FR_sound(Off)
            PA.horn()
            AM_Fn_Acquire_Route()
            PA_bell(On)
            after (AM_Delayed_Horn) then {
                PA.horn()
                PA.forward(AM_Leaving_Speed)
            }
        }
    }

    val B503a_fwd = node(B503a) {
        onEnter {
            PA_bell(Off)
        }
    }

    val B321_fwd = node(B321) {
        onEnter {
            // Wait for train to have cleared up T320 before switching to full speed.
            // The problem is that B321 activates as train hits diverted leg of turnout.
            after (AM_Leaving_To_Full_Speed) then {
                PA.forward(AM_Full_Speed)
            }

            // Mid_Station doppler on the way up
            after (AM_Timer_B321_Up_Doppler) then {
                PA_doppler(On)
            } and_after (1.seconds) then {
                PA_doppler(Off)
            }
        }
    }

    val B330_fwd = node(B330) {
        onEnter {
            // Sonora speed reduction
            PA.forward(AM_Sonora_Speed)
            after (AM_Timer_B330_Up_Resume) then {
                PA.forward(AM_Full_Speed)
                PA.horn()
            }
        }
    }

    val B340_fwd = node(B340) {
        onEnter {
            // After tunnel on the way up
            after (AM_Timer_B340_Up_Horn) then {
                PA.horn()
            }
        }
    }

    //--- PA State: AM Summit

    val B360_fwd = node(B360) {
    }

    val B370_end = node(B370) {
        onEnter {
            after (4.seconds) then {
                // Forward
                PA.forward(AM_Summit_Speed)
                PA_bell(On)
            } and_after (AM_Timer_B370_Forward_Stop) then {
                PA.stop()
                PA.horn()
            } and_after (AM_Timer_B370_Pause_Delay) then {
                // Stopped
                PA_bell(Off)
                PA.horn()
                PA.reverse(AM_Summit_Speed)
            }
        }
    }

    val B360_rev = node(B360) {
        onEnter {
            after (AM_Timer_B360_Full_Reverse) then {
                PA.reverse(AM_Full_Speed)
            }
        }
    }

    val B340_rev = node(B340) {
    }

    val B330_rev = node(B330) {
        whileOccupied {
            after (AM_Timer_B330_Down_Speed) then {
                PA.horn()
                PA.reverse(AM_Sonora_Speed)
            }
        }
    }

    val B321_rev = node(B321) {
        onEnter {
            AM_Fn_Acquire_Route()
        }
        whileOccupied {
            PA.reverse(AM_Full_Speed)
            // Doppler sound
            PA_doppler(On)
            after (1.seconds) then {
                PA_doppler(Off)
            }
            after (AM_Timer_B321_Down_Crossover) then {
                PA.horn()
                PA_bell(On)
                PA.reverse(AM_Crossover_Speed)
            }
        }
    }

    val B503a_rev = node(B503a) {
        onEnter {
            PA.horn()
        }
    }

    val B503b_rev = node(B503b) {
        whileOccupied {
            after (AM_Timer_B503b_Down_Stop) then {
                PA.stop()
                PA.horn()
                PA_bell(Off)
            } and_after (AM_Timer_Down_Station_Lights_Off) then {
                analytics.gaEvent {
                    category = "Activation"
                    action = "Stop"
                    label = ML_Train.name
                    user = ML_Start_Counter.toString()
                }
                ML_Train = EML_Train.Freight
                ML_State = EML_State.Wait
            } and_after (ML_Timer_Wait) then {
                routes.activate(ML_Idle_Route)
            }
        }
    }

    sequence = listOf(
        B503b_start, B503a_fwd, B321_fwd, B330_fwd, B340_fwd, B360_fwd,
        B370_end,
        B360_rev, B340_rev, B330_rev, B321_rev, B503a_rev, B503b_rev)
}


// Speeds: Doodlebug: 8/4; RDC: 20/12; 804: 16/12/4; 6580: 8/6/2; 655: 16/12/8; 2468: 28/20/12; 1840: 20/16/12; 5278:16/16/12; 024: 8/6/2
val SP_Forward_Speed    = 8.speed
val SP_Reverse_Speed    = 4.speed
val SP_Station_Speed    = 2.speed

val SP_Sound_Started    = 2.seconds
val SP_Timer_Up_Slow    = 40.seconds    // B321 time to station speed: RDC=40, Doodlebug=60, 804=60.
val SP_Timer_Up_Stop    = 12.seconds    // Time on slow down before stop
val SP_Timer_Up_Reverse = 30.seconds    // Time stopped before reverse
val SP_Timer_Reverse_Horn = 2.seconds
val SP_Timer_Down_Slow  = 24.seconds    // Time before slow on B311. RDC=10, Doodlebug or 804=18. 024=21, 5278=12.
val SP_Timer_Down_Stop  = 16.seconds    // Time on slow down before stop.
val SP_Timer_Down_Off   = 20.seconds
val SP_Sound_Stopped    = 2.seconds

val Freight_Route = ML_Route.sequence {
    throttle = FR
    maxSecondsOnBlock = 60

    onError {
        // no-op
    }

    onActivate {
        ML_Train = EML_Train.Freight
        ML_State = EML_State.Running
        ML_Fn_Send_Start_GaEvent()
        exportedVars.rtacPsaText = "{c:#FF008800}Currently Running:\n\nFreight"
        jsonEvent {
            key1 = "Depart"
            key2 = "Freight"
        }
    }

    val B311_start = node(B311) {
        whileOccupied {
            FR.light(On)
            FR_marker(On)
            FR_sound(On)
            PA_sound(Off)
            after (SP_Sound_Started) then {
                FR.horn()
                FR.light(On)
                FR_bell(Off)
                FR.forward(SP_Forward_Speed)
                SP_Fn_Acquire_Route()
            } and_after (2.seconds) then {
                FR.horn()
            }
        }
    }

    val B321_fwd = node(B321) {
        onEnter {
            FR.forward(SP_Forward_Speed)
            after (SP_Timer_Up_Slow) then {
                FR.horn()
                FR_bell(On)
                FR.forward(SP_Station_Speed)
            } and_after (SP_Timer_Up_Stop) then {
                // Stop in B321. Normal case is to *not* go into B330.
                FR.horn()
                FR.stop()
                FR_bell(Off)
                // This is the long stop at the station.
            } and_after (SP_Timer_Up_Reverse) then {
                // Start reversing after the long stop.
                FR.horn()
                FR.reverse(SP_Reverse_Speed)
                FR_bell(On)
            } and_after (SP_Timer_Reverse_Horn) then {
                FR.horn()
            } and_after (SP_Timer_Reverse_Horn) then {
                FR.horn()
                FR_bell(Off)
                // Normal case: continue to B311_rev.
            }
        }
    }

    val B330_fwd = node(B330) {
        // Error case: we should never reach block B330.
        // If we do, reverse and act on B321_rev
        onEnter {
            FR.reverse(SP_Reverse_Speed)
            FR_bell(On)
        }
    }

    val B321_rev = node(B321) {
        // Error case coming back to B321 after overshooting in B330
        onEnter {
            FR.horn()
            after (SP_Timer_Reverse_Horn) then {
                FR.horn()
            }
        }
    }

    val B311_rev = node(B311) {
        onEnter {
            after (SP_Timer_Down_Slow) then {
                FR_bell(On)
            } and_after (SP_Timer_Down_Stop) then {
                FR_bell(Off)
                FR.horn()
                FR.stop()
            } and_after (SP_Timer_Down_Off) then {
                FR.horn()
                FR_bell(Off)
                FR.light(Off)
                FR_marker(Off)
            } and_after (SP_Sound_Stopped) then {
                FR_sound(Off)
                PA_sound(On)
                analytics.gaEvent {
                    category = "Activation"
                    action = "Stop"
                    label = ML_Train.name
                    user = ML_Start_Counter.toString()
                }
                ML_Train = EML_Train.Passenger
                ML_State = EML_State.Wait
            } and_after (ML_Timer_Wait) then {
                routes.activate(ML_Idle_Route)
            }
        }
    }

    sequence = listOf(B311_start, B321_fwd, B311_rev)
    branches += listOf(B321_fwd, B330_fwd, B321_rev, B311_rev)
}

fun ML_Fn_Try_Recover_Route() {
    if (!ML_Toggle) {
        ML_Route.activate(ML_Idle_Route)
        return
    }

    val PA_blocks = (Passenger_Route as ISequenceRoute).sequence.map { it.block }.distinct()
    val FR_blocks = (Freight_Route as ISequenceRoute).sequence.map { it.block }.distinct()
    val PA_start = PA_blocks.first().active
    val FR_start = FR_blocks.first().active
    val PA_occup = PA_blocks.subList(1, PA_blocks.size).count { it.active }
    val FR_occup = FR_blocks.subList(1, FR_blocks.size).count { it.active }
    val PA_total = PA_blocks.count { it.active }
    val FR_total = FR_blocks.count { it.active }

    if (!PA_start && FR_start && PA_occup == 1) {
        // FR train is accounted for, where expected.
        // PA train is likely somewhere on its route... try to recover that one.
        println("@@ ML Recovery: Recover Passenger")
        ML_Route.activate(ML_Recover_Passenger_Route)

    } else if (PA_start && !FR_start && FR_occup == 1) {
        // PA train is accounted for, where expected.
        // FR train is likely somewhere on its route... try to recover that one.
        println("@@ ML Recovery: Recover Freight")
        ML_Route.activate(ML_Recover_Freight_Route)

    } else if (!PA_start && FR_start && PA_occup == 0) {
        // PA train is either missing or on a dead block.
        // In that case we can still run the FR route because it's a subset of the large route.
        println("@@ ML Recovery: Ignore Passenger, Activate Freight")
        ML_Route.activate(Freight_Route)

    } else if (PA_total > 1 || FR_total > 1) {
        // We have more than one block occupied on the route. This is not recoverable
        // but that's a case special enough we can ask for the track to be cleared.
        val PA_names = PA_blocks.filter { it.active }
        val FR_names = FR_blocks.filter { it.active }
        println("@@ ML Recovery: Track occupied (Passenger: $PA_names, Freight: $FR_names)")

        val names = PA_names.plus(FR_names).map { it.name }.distinct()
        exportedVars.rtacPsaText = "{b:blue}{c:white}Automation Warning\nCheck Track $names"
        analytics.gaEvent {
            category = "Automation"
            action = "Warning"
            label = "Passenger"
            user = "Staff"
        }
        ML_Route.activate(ML_Idle_Route)

    } else {
        println("@@ ML Recovery: Unknown situation. Cannot recover.")
        ML_Route.activate(ML_Idle_Route)
    }
}

val ML_Recover_Passenger_Route = ML_Route.sequence {
    throttle = PA
    maxSecondsOnBlock = 60 // 1 minute

    fun move() {
        PA_bell(On)
        PA_sound(On)
        PA.horn()
    }

    val B370_rev = node(B370) {
        onEnter {
            move()
            PA.reverse(AM_Summit_Speed)
        }
    }

    val B360_rev = node(B360) {
        onEnter {
            move()
            PA.reverse(AM_Summit_Speed)
            after (AM_Timer_B360_Full_Reverse) then {
                PA.reverse(AM_Full_Speed)
            }
        }
    }

    val B340_rev = node(B340) {
        onEnter {
            move()
            PA.reverse(AM_Full_Speed)
        }
    }

    val B330_rev = node(B330) {
        onEnter {
            move()
            PA.reverse(AM_Sonora_Speed)
        }
    }

    val B321_rev = node(B321) {
        onEnter {
            AM_Fn_Acquire_Route()
            move()
            PA.reverse(AM_Full_Speed)
            after (AM_Timer_B321_Down_Crossover) then {
                PA.horn()
                PA_bell(On)
                PA.reverse(AM_Crossover_Speed)
            }
        }
    }

    val B503a_rev = node(B503a) {
        onEnter {
            move()
            PA.reverse(AM_Crossover_Speed)
        }
    }

    val B503b_rev = node(B503b) {
        onEnter {
            // Note: this is the normal start block and is never
            // a recover block. We do not set any speed on purpose.
            after (AM_Timer_B503b_Down_Stop) then {
                PA.stop()
                PA.horn()
                PA_bell(Off)
            } and_after (5.seconds) then {
                PA_sound(Off)
            } and_after (2.seconds) then {
                if (ML_Toggle.active) {
                    Passenger_Route.activate()
                } else {
                    ML_Idle_Route.activate()
                }
            }
        }
    }

    onActivate {
        ML_State = EML_State.Recover
        ML_Train = EML_Train.Passenger
        if (B370.active) {
            ML_Route.active.startNode(B370_rev)
        } else if (B360.active) {
            ML_Route.active.startNode(B360_rev)
        } else if (B340.active) {
            ML_Route.active.startNode(B340_rev)
        } else if (B330.active) {
            ML_Route.active.startNode(B330_rev)
        } else if (B321.active) {
            ML_Route.active.startNode(B321_rev)
        } else if (B503a.active) {
            ML_Route.active.startNode(B503a_rev)
        } else if (B503b.active) {
            ML_Route.active.startNode(B503b_rev)
        }
    }

    onError {
        // We cannot recover from an error during the recover route.
        if (ML_Toggle.active) {
            PA.stop()
            ML_Idle_Route.activate()
        }
    }

    sequence = listOf(
        B370_rev, B360_rev, B340_rev, B330_rev, B321_rev, B503a_rev, B503b_rev)
}

val ML_Recover_Freight_Route = ML_Route.sequence {
    throttle = FR
    maxSecondsOnBlock = 60 // 1 minute

    fun move() {
        FR_bell(On)
        FR_sound(On)
        FR.horn()
        FR.reverse(SP_Reverse_Speed)
    }

    val B321_rev = node(B321) {
        onEnter {
            SP_Fn_Acquire_Route()
            move()
        }
    }

    val B311_rev = node(B311) {
        onEnter {
            // Note: this is the normal start block and is never
            // a recover block. We do not set any speed on purpose.
            SP_Fn_Acquire_Route()
            after (SP_Timer_Down_Slow) then {
                FR_bell(On)
            } and_after (SP_Timer_Down_Stop) then {
                FR_bell(Off)
                FR.horn()
                FR.stop()
            } and_after (5.seconds) then {
                FR_bell(Off)
                FR_sound(Off)
            } and_after (2.seconds) then {
                if (ML_Toggle.active) {
                    Freight_Route.activate()
                } else {
                    ML_Idle_Route.activate()
                }
            }
        }
    }

    onActivate {
        ML_State = EML_State.Recover
        ML_Train = EML_Train.Freight
        if (B321.active) {
            ML_Route.active.startNode(B321_rev)
        } else if (B311.active) {
            ML_Route.active.startNode(B311_rev)
        }
    }

    onError {
        // We cannot recover from an error during the recover route.
        if (ML_Toggle.active) {
            FR.stop()
            ML_Idle_Route.activate()
        }
    }

    sequence = listOf(B321_rev, B311_rev)
}


// ----------------------
// Events Branchline (BL)
// ----------------------

/*
On the Rapido RDC SP-10 / SantaFe 191:
- F5 is a doppler horn, too long so not using it
- F6 is disabled (function used by ESU PowerPack)
- F7 for dim lights for stations
- F8 is sound (0 for mute, 1 for sound, LokSound)
- F9 for red markers on the "end" side

On the Mopac 153:
- F8 is a mute (1 for silence, 0 for sound)
- F9 is a crossing horn.

On RS3 4070:
- F5 is the gyro light.
- F8 is mute (1 for silence, 0 for sound)

Caboose UP 25520 --> DCC 2552
- Lights on for lights
- Chimney denotes front
- FWD for red rear marker, REV for red front marker.
- F3 on for green rear marker, F4 on for green front marker.
*/


val BL = throttle(4070) named "BL"
val CAB = throttle(2552) named "Cab"

fun BL_bell (on: Boolean) { BL.f1(on)  }
fun BL_sound(on: Boolean) { BL.f8(!on) }
fun BL_gyro (on: Boolean) { BL.f5(on)  }

val BL_Speed = 10.speed
val BL_Speed_Station = 6.speed

enum class EBL_State { Ready, Wait, Running, Recover }

var BL_State = EBL_State.Ready

var BL_Start_Counter = 0


on { BL_Toggle } then {
    BL.repeat(2.seconds)
    BL.light(Off)
}

on { !BL_Toggle } then {
    BL.repeat(0.seconds)
    BL.light(Off)
    BL_bell(Off)
    BL_sound(Off)
    BL_gyro(Off)
}

on { BL_Toggle.active } then {
    analytics.gaEvent {
        category = "Automation"
        action = "On"
        label = "Branchline"
        user = "Staff"
    }
    jsonEvent {
        key1 = "Toggle"
        key2 = "Branchline"
        value = "On"
    }
}
on { !BL_Toggle } then {
    analytics.gaEvent {
        category = "Automation"
        action = "Off"
        label = "Branchline"
        user = "Staff"
    }
    jsonEvent {
        key1 = "Toggle"
        key2 = "Branchline"
        value = "Off"
    }
}

// --- BL Static State for 4070 and its Caboose

// #4070 Gyro light on when moving.
// Caboose lights are controled by the engine's direction.
on {  BL.stopped } then {
    BL_gyro(Off)
    CAB.light(On)
    CAB.f3(Off)
    CAB.f4(Off)
}
on { BL.forward } then {
    BL_gyro(On)
    CAB.light(On)
    CAB.forward(1.speed)
    CAB.f3(Off)
    CAB.f4(On)
}
on { BL.reverse } then {
    BL_gyro(On)
    CAB.light(On)
    CAB.reverse(1.speed)
    CAB.f3(On)
    CAB.f4(Off)
}

val BL_Route = routes {
    name = "Branchline"
    toggle = BL_Toggle
    status = { BL_State.toString() }

    onError {
        // --- BL State: Error
        // The current route will trigger the corresponding BL_Recover_Route.
        BL.repeat(1.seconds)
        BL.stop()
        BL_sound(Off)
        analytics.gaEvent {
            category = "Automation"
            action = "Error"
            label = "Branchline"
            user = "Staff"
        }
    }
}

var BL_Idle_Route: IRoute
BL_Idle_Route = BL_Route.idle {
    onActivate {
        BL_State = EBL_State.Ready
    }

    onIdle {
        if (BL_Toggle.active && AIU_Motion.active) {
            BL_Route.activate(BL_Shuttle_Route)
        }
    }
}

val BL_Shuttle_Route = BL_Route.sequence {
    throttle = BL
    maxSecondsOnBlock = 60 // 1 minute

    val BL_Timer_Start_Delay = 5.seconds
    val BL_Timer_Bell_Delay = 2.seconds
    val BL_Timer_RevStation_Stop = 4.seconds
    val BL_Timer_RevStation_Pause = 25.seconds
    val BL_Timer_Station_Stop = 6.seconds
    val BL_Timer_Station_Rev3 = 8.seconds
    val BL_Timer_Wait = 30.seconds  // 300=5 minutes -- change for debugging

    fun doStart() {
        BL_sound(On)
        BL.horn()
        BL_bell(On)
        after (BL_Timer_Start_Delay) then {
            BL_bell(Off)
            BL.horn()
            BL.forward(BL_Speed_Station)
        }
        jsonEvent {
            key1 = "Depart"
            key2 = "Branchline"
        }
    }

    val BLParked_fwd = node(BLParked) {
        onEnter {
            doStart()
        }
    }

    val BLStation_fwd = node(BLStation) {
        onEnter {
            doStart()
        }
    }

    val BLTunnel_fwd = node(BLTunnel) {
        onEnter {
            BL.horn()
        }
    }

    val BLReverse_fwd = node(BLReverse) {
        onEnter {
            BL.horn()
            BL_bell(On)
            after (BL_Timer_RevStation_Stop) then {
                // Toggle Stop/Reverse/Stop to turn off the Reverse front light.
                // Next event should still be the BLReverse Stopped one.
                BL.stop()
                BL.horn()
                BL.reverse(1.speed)
                BL.stop()
            } and_after (BL_Timer_Bell_Delay) then {
                BL_bell(Off)
            } and_after (BL_Timer_RevStation_Pause) then {
                BL_bell(On)
                BL.horn()
            } and_after (5.seconds) then {
                BL.reverse(BL_Speed)
            } and_after (BL_Timer_Bell_Delay) then {
                BL.light(On)
                BL_bell(Off)
                BL_sound(On)
                BL.horn()
            }
        }
    }

    val BLTunnel_rev = node(BLTunnel) {
        onEnter {
            BL.horn()
        }
    }

    val BLStation_rev = node(BLStation) {
        onEnter {
            BL_bell(On)
            T324.normal()
            BL.reverse(BL_Speed_Station)
            after (BL_Timer_Station_Stop) then {
                BL.stop()
                BL.horn()
            } and_after (BL_Timer_Station_Rev3) then {
                BL.horn()
                BL.reverse(BL_Speed_Station)
            }
        }
    }

    val BLParked_rev = node(BLParked) {
        onEnter {
            // We went too far, but it's not a problem / not an error.
            BL.stop()
            after (3.seconds) then {
                BL_bell(Off)
            } and_after (2.seconds) then {
                BL_sound(Off)
                BL_State = EBL_State.Wait
            } and_after (BL_Timer_Wait) then {
                BL_Route.activate(BL_Idle_Route)
            }
        }
    }

    onActivate {
        BL_State = EBL_State.Running
        BL_Start_Counter++
        // It's ok to start either from BLParked or BLStation
        if (BLParked.active) {
            BL_Route.active.startNode(BLParked_fwd)
        } else if (!BLParked && BLStation.active) {
            BL_Route.active.startNode(BLStation_fwd)
        }
    }

    onError {
        BL_Route.activate(BL_Recover_Route)
    }

    sequence = listOf(BLParked_fwd, BLStation_fwd, BLTunnel_fwd,
        BLReverse_fwd,
        BLTunnel_rev, BLStation_rev, BLParked_rev)

}

val BL_Recover_Route = BL_Route.sequence {
    throttle = BL
    maxSecondsOnBlock = 60 // 1 minute

    fun move() {
        BL_bell(On)
        BL_sound(On)
        BL.horn()
        BL.reverse(BL_Speed_Station)
    }

    val BLReverse_rev = node(BLReverse) {
        onEnter {
            move()
        }
    }

    val BLTunnel_rev = node(BLTunnel) {
        onEnter {
            move()
        }
    }

    val BLStation_rev = node(BLStation) {
        onEnter {
            move()
        }
    }

    val BLParked_rev = node(BLParked) {
        onEnter {
            BL.stop()
            after (5.seconds) then {
                BL_bell(Off)
                BL_sound(Off)
            } and_after (2.seconds) then {
                BL_Route.activate(BL_Idle_Route)
            }
        }
    }

    onActivate {
        BL_State = EBL_State.Recover
        if (BLReverse.active) {
            BL_Route.active.startNode(BLReverse_rev)
        } else if (BLTunnel.active) {
            BL_Route.active.startNode(BLTunnel_rev)
        } else if (BLStation.active) {
            BL_Route.active.startNode(BLStation_rev)
        } else if (BLParked.active) {
            BL_Route.active.startNode(BLParked_rev)
        }
    }

    onError {
        // We cannot recover from an error during the recover route.
        if (BL_Toggle.active) {
            BL.stop()
        }
    }

    sequence = listOf(BLReverse_rev, BLTunnel_rev, BLStation_rev, BLParked_rev)
}



// ------------------
// Automatic Turnouts
// ------------------

// If automation is off, T330 is automatically selected:
// B320 -> B330 via T330 Normal
// B321 -> B330 via T330 Reverse

on { !ML_Toggle && !B330 &&  B320.active && !B321 } then { T330.normal() }
on { !ML_Toggle && !B330 && !B320 &&  B321.active } then { T330.reverse() }


// ---------
