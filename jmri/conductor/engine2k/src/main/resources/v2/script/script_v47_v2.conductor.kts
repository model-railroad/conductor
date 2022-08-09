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

var AIU_Motion_Counter = 0

on { AIU_Motion } then {
    exportedVars.RTAC_Motion = On
    AIU_Motion_Counter += 1
    ga_event {
        category = "Motion"
        action = "Start"
        label = "AIU"
        user = AIU_Motion_Counter.toString()
    }
}

on { !AIU_Motion } then {
    exportedVars.RTAC_Motion = Off
    ga_event {
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

on { PA_Toggle.active && exportedVars.Conductor_Time == End_Of_Day_HHMM } then {
    PA_Toggle.active(Off)
}

on { BL_Toggle.active && exportedVars.Conductor_Time == End_Of_Day_HHMM } then {
    BL_Toggle.active(Off)
}

on { PA_Toggle } then {
    exportedVars.RTAC_PSA_Text = "Automation Started"
}

on { !PA_Toggle && exportedVars.Conductor_Time == End_Of_Day_HHMM } then {
    exportedVars.RTAC_PSA_Text = "{c:red}Automation Turned Off\nat 4:50 PM"
}

on { !PA_Toggle && exportedVars.Conductor_Time != End_Of_Day_HHMM } then {
    exportedVars.RTAC_PSA_Text = "{c:red}Automation Stopped"
}

// ---------------------
// Events & RTAC Display
// ---------------------

on { PA_Toggle  } then {
    ga_event {
        category = "Automation"
        action = "On"
        label = "Passenger"
        user = "Staff"
    }
    json_event {
        key1 = "Toggle"
        key2 = "Passenger"
        value = "On"
    }
}
on { !PA_Toggle } then {
    ga_event {
        category = "Automation"
        action = "Off"
        label = "Passenger"
        user = "Staff"
    }
    json_event {
        key1 = "Toggle"
        key2 = "Passenger"
        value = "Off"
    }
}

on { PA_Toggle } then {
    AM.repeat(2.seconds)
    SP.repeat(2.seconds)
}

on { !PA_Toggle } then {
    AM.repeat(2.seconds)
    SP.repeat(2.seconds)
    AM_bell(Off)
    SP_bell(Off)
}


on { PA_State == EPA_State.Ready && PA_Toggle.active && PA_Train == EPA_Train.Passenger } then {
    exportedVars.RTAC_PSA_Text = "{c:blue}Next Train:\n\nPassenger"
}
on { PA_State == EPA_State.Wait  && PA_Toggle.active && PA_Train == EPA_Train.Passenger } then {
    exportedVars.RTAC_PSA_Text = "{c:blue}Next Train:\n\nPassenger\n\nLeaving in 1 minute"
}
on { PA_State == EPA_State.Ready && PA_Toggle.active && PA_Train == EPA_Train.Freight  } then {
    exportedVars.RTAC_PSA_Text = "{c:#FF008800}Next Train:\n\nFreight"
}
on { PA_State == EPA_State.Wait  && PA_Toggle.active && PA_Train == EPA_Train.Freight  } then {
    exportedVars.RTAC_PSA_Text = "{c:#FF008800}Next Train:\n\nFreight\n\nLeaving in 1 minute"
}

// --------------------
// Events PA (mainline)
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

enum class EPA_State { Ready, Wait, Running, Recover }
enum class EPA_Train { Passenger, Freight, }

var PA_State = EPA_State.Ready
var PA_Train = EPA_Train.Passenger
var PA_Start_Counter = 0
val PA_Timer_Wait = 60.seconds  // 1 minute

val AM = throttle(8749) named "AM"     // Full Amtrak route
val SP = throttle(1072) named "SP"     // "Short Passenger" (now Freight) on limited Amtrak route

fun AM_bell   (on: Boolean) { AM.f1(on)  }
fun AM_sound  (on: Boolean) { AM.f8(!on) }
fun AM_doppler(on: Boolean) { /* no-op on 8749 */ }
fun SP_bell   (on: Boolean) { SP.f1(on)  }
fun SP_sound  (on: Boolean) { SP.f8(!on) }
fun SP_marker (on: Boolean) { /* no-op on 1072 */ }

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
        // The current route will trigger the corresponding PA_Recover_Route.
        AM.repeat(1.seconds)
        SP.repeat(1.seconds)
        AM.stop()
        SP.stop()
        AM_sound(Off) ; AM.light(Off) ; AM_bell(Off)
        SP_sound(Off) ; SP.light(Off) ; SP_bell(Off) ; SP_marker(Off)
        ga_event {
            category = "Automation"
            action = "Error"
            label = "Passenger"
            user = "Staff"
        }
        exportedVars.RTAC_PSA_Text = "{b:red}{c:white}Automation ERROR"

        PA_Fn_Try_Recover_Route()
    }
}

var PA_Idle_Route: IRoute
PA_Idle_Route = PA_Route.idle {
    onActivate {
        PA_State = EPA_State.Ready
        AM.stop()
        AM.light(On)
        AM_bell(Off)

        SP.stop()
        SP.light(On)
        SP_bell(Off)
    }

    onIdle {
        if (PA_Toggle.active && AIU_Motion.active && AM.stopped && SP.stopped) {
            when (PA_Train) {
                EPA_Train.Passenger -> PA_Route.activate(Passenger_Route)
                EPA_Train.Freight -> PA_Route.activate(Freight_Route)
            }
        }
    }
}

on { PA_State == EPA_State.Ready && PA_Toggle.active } then {
    AM_sound(On);   AM.light(On)
    SP_sound(On);   SP.light(On)
    AM.stop();      SP.stop()
    PA_Fn_Release_Route()
}

val PA_Timer_Stop_Sound_Off = 10.seconds
on { PA_State == EPA_State.Ready && !PA_Toggle } then {
    AM.light(Off);  SP.light(Off)
    AM.stop();      SP.stop()
    after(PA_Timer_Stop_Sound_Off) then {
        AM_sound(Off)
        SP_sound(Off)
    }
    PA_Fn_Release_Route()
}

fun PA_Fn_Send_Start_GaEvent() {
    PA_Start_Counter += 1
    ga_page {
        url = exportedVars.GA_URL
        path = PA_Train.name
        user = PA_Start_Counter.toString()
    }
    ga_event {
        category = "Automation"
        action = "Start"
        label = PA_Train.name
        user = PA_Start_Counter.toString()
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

val Passenger_Route = PA_Route.sequence {
    throttle = AM
    timeout = 60 // 1 minute

    onRecover {
        // no-op
    }

    onActivate {
        PA_Train = EPA_Train.Passenger
        PA_State = EPA_State.Running
        PA_Fn_Send_Start_GaEvent()
        exportedVars.RTAC_PSA_Text = "{c:blue}Currently Running:\n\nPassenger"
        json_event {
            key1 = "Depart"
            key2 = "Passenger"
        }
        AM.light(On)
        AM_bell(Off)
        AM_sound(On)
        SP_sound(Off)
    }

    val B503b_start = node(B503b) {
        onEnter {
            SP_sound(Off)
            AM.horn()
            AM_Fn_Acquire_Route()
            AM_bell(On)
            after (AM_Delayed_Horn) then {
                AM.horn()
                AM.forward(AM_Leaving_Speed)
            }
        }
    }

    val B503a_fwd = node(B503a) {
        onEnter {
            AM_bell(Off)
        }
    }

    val B321_fwd = node(B321) {
        onEnter {
            // Wait for train to have cleared up T320 before switching to full speed.
            // The problem is that B321 activates as train hits diverted leg of turnout.
            after (AM_Leaving_To_Full_Speed) then {
                AM.forward(AM_Full_Speed)
            }

            // Mid_Station doppler on the way up
            after (AM_Timer_B321_Up_Doppler) then {
                AM_doppler(On)
            } and_after (1.seconds) then {
                AM_doppler(Off)
            }
        }
    }

    val B330_fwd = node(B330) {
        onEnter {
            // Sonora speed reduction
            AM.forward(AM_Sonora_Speed)
            after (AM_Timer_B330_Up_Resume) then {
                AM.forward(AM_Full_Speed)
                AM.horn()
            }
        }
    }

    val B340_fwd = node(B340) {
        onEnter {
            // After tunnel on the way up
            after (AM_Timer_B340_Up_Horn) then {
                AM.horn()
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
                AM.forward(AM_Summit_Speed)
                AM_bell(On)
            } and_after (AM_Timer_B370_Forward_Stop) then {
                AM.stop()
                AM.horn()
            } and_after (AM_Timer_B370_Pause_Delay) then {
                // Stopped
                AM_bell(Off)
                AM.horn()
                AM.reverse(AM_Summit_Speed)
            }
        }
    }

    val B360_rev = node(B360) {
        onEnter {
            after (AM_Timer_B360_Full_Reverse) then {
                AM.reverse(AM_Full_Speed)
            }
        }
    }

    val B340_rev = node(B340) {
    }

    val B330_rev = node(B330) {
        whileOccupied {
            after (AM_Timer_B330_Down_Speed) then {
                AM.horn()
                AM.reverse(AM_Sonora_Speed)
            }
        }
    }

    val B321_rev = node(B321) {
        onEnter {
            AM_Fn_Acquire_Route()
        }
        whileOccupied {
            AM.reverse(AM_Full_Speed)
            // Doppler sound
            AM_doppler(On)
            after (1.seconds) then {
                AM_doppler(Off)
            }
            after (AM_Timer_B321_Down_Crossover) then {
                AM.horn()
                AM_bell(On)
                AM.reverse(AM_Crossover_Speed)
            }
        }
    }

    val B503a_rev = node(B503a) {
        onEnter {
            AM.horn()
        }
    }

    val B503b_rev = node(B503b) {
        whileOccupied {
            after (AM_Timer_B503b_Down_Stop) then {
                AM.stop()
                AM.horn()
                AM_bell(Off)
            } and_after (AM_Timer_Down_Station_Lights_Off) then {
                ga_event {
                    category = "Activation"
                    action = "Stop"
                    label = PA_Train.name
                    user = PA_Start_Counter.toString()
                }
                PA_Train = EPA_Train.Freight
                PA_State = EPA_State.Wait
            } and_after (PA_Timer_Wait) then {
                route.activate(PA_Idle_Route)
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

val Freight_Route = PA_Route.sequence {
    throttle = SP
    timeout = 60

    onRecover {
        // no-op
    }

    onActivate {
        PA_Train = EPA_Train.Freight
        PA_State = EPA_State.Running
        PA_Fn_Send_Start_GaEvent()
        exportedVars.RTAC_PSA_Text = "{c:#FF008800}Currently Running:\n\nFreight"
        json_event {
            key1 = "Depart"
            key2 = "Freight"
        }
    }

    val B311_start = node(B311) {
        whileOccupied {
            SP.light(On)
            SP_marker(On)
            SP_sound(On)
            AM_sound(Off)
            after (SP_Sound_Started) then {
                SP.horn()
                SP.light(On)
                SP_bell(Off)
                SP.forward(SP_Forward_Speed)
                SP_Fn_Acquire_Route()
            } and_after (2.seconds) then {
                SP.horn()
            }
        }
    }

    val B321_fwd = node(B321) {
        onEnter {
            SP.forward(SP_Forward_Speed)
            after (SP_Timer_Up_Slow) then {
                SP.horn()
                SP_bell(On)
                SP.forward(SP_Station_Speed)
            } and_after (SP_Timer_Up_Stop) then {
                // Stop in B321. Normal case is to *not* go into B330.
                SP.horn()
                SP.stop()
                SP_bell(Off)
                // This is the long stop at the station.
            } and_after (SP_Timer_Up_Reverse) then {
                // Start reversing after the long stop.
                SP.horn()
                SP.reverse(SP_Reverse_Speed)
                SP_bell(On)
            } and_after (SP_Timer_Reverse_Horn) then {
                SP.horn()
            } and_after (SP_Timer_Reverse_Horn) then {
                SP.horn()
                SP_bell(Off)
                // Normal case: continue to B311_rev.
            }
        }
    }

    val B330_fwd = node(B330) {
        // Error case: we should never reach block B330.
        // If we do, reverse and act on B321_rev
        onEnter {
            SP.reverse(SP_Reverse_Speed)
            SP_bell(On)
        }
    }

    val B321_rev = node(B321) {
        // Error case coming back to B321 after overshooting in B330
        onEnter {
            SP.horn()
            after (SP_Timer_Reverse_Horn) then {
                SP.horn()
            }
        }
    }

    val B311_rev = node(B311) {
        onEnter {
            after (SP_Timer_Down_Slow) then {
                SP_bell(On)
            } and_after (SP_Timer_Down_Stop) then {
                SP_bell(Off)
                SP.horn()
                SP.stop()
            } and_after (SP_Timer_Down_Off) then {
                SP.horn()
                SP_bell(Off)
                SP.light(Off)
                SP_marker(Off)
            } and_after (SP_Sound_Stopped) then {
                SP_sound(Off)
                AM_sound(On)
                ga_event {
                    category = "Activation"
                    action = "Stop"
                    label = PA_Train.name
                    user = PA_Start_Counter.toString()
                }
                PA_Train = EPA_Train.Passenger
                PA_State = EPA_State.Wait
            } and_after (PA_Timer_Wait) then {
                route.activate(PA_Idle_Route)
            }
        }
    }

    sequence = listOf(B311_start, B321_fwd, B311_rev)
    branches += listOf(B321_fwd, B330_fwd, B321_rev, B311_rev)
}

fun PA_Fn_Try_Recover_Route() {
    if (!PA_Toggle) {
        PA_Route.activate(PA_Idle_Route)
        return
    }

    val PA_blocks = (Passenger_Route as IRouteSequence).sequence.map { it.block }.distinct()
    val FR_blocks = (Freight_Route as IRouteSequence).sequence.map { it.block }.distinct()
    val PA_start = PA_blocks.first().active
    val FR_start = FR_blocks.first().active
    val PA_occup = PA_blocks.subList(1, PA_blocks.size).count { it.active }
    val FR_occup = FR_blocks.subList(1, FR_blocks.size).count { it.active }
    val PA_total = PA_blocks.count { it.active }
    val FR_total = FR_blocks.count { it.active }

    if (!PA_start && FR_start && PA_occup == 1) {
        // FR train is accounted for, where expected.
        // PA train is likely somewhere on its route... try to recover that one.
        println("@@ PA Recovery: Recover Passenger")
        PA_Route.activate(PA_Recover_Passenger_Route)

    } else if (PA_start && !FR_start && FR_occup == 1) {
        // PA train is accounted for, where expected.
        // FR train is likely somewhere on its route... try to recover that one.
        println("@@ PA Recovery: Recover Freight")
        PA_Route.activate(PA_Recover_Freight_Route)

    } else if (!PA_start && FR_start && PA_occup == 0) {
        // PA train is either missing or on a dead block.
        // In that case we can still run the FR route because it's a subset of the large route.
        println("@@ PA Recovery: Ignore Passenger, Activate Freight")
        PA_Route.activate(Freight_Route)

    } else if (PA_total > 1 || FR_total > 1) {
        // We have more than one block occupied on the route. This is not recoverable
        // but that's a case special enough we can ask for the track to be cleared.
        val PA_names = PA_blocks.filter { it.active }
        val FR_names = FR_blocks.filter { it.active }
        println("@@ PA Recovery: Track occupied (Passenger: $PA_names, Freight: $FR_names)")

        val names = PA_names.plus(FR_names).map { it.name }.distinct()
        exportedVars.RTAC_PSA_Text = "{b:blue}{c:white}Automation Warning\nCheck Track $names"
        ga_event {
            category = "Automation"
            action = "Warning"
            label = "Passenger"
            user = "Staff"
        }
        PA_Route.activate(PA_Idle_Route)

    } else {
        println("@@ PA Recovery: Unknown situation. Cannot recover.")
        PA_Route.activate(PA_Idle_Route)
    }
}

val PA_Recover_Passenger_Route = PA_Route.sequence {
    throttle = AM
    timeout = 60 // 1 minute

    fun move() {
        AM_bell(On)
        AM_sound(On)
        AM.horn()
    }

    val B370_rev = node(B370) {
        onEnter {
            move()
            AM.reverse(AM_Summit_Speed)
        }
    }

    val B360_rev = node(B360) {
        onEnter {
            move()
            AM.reverse(AM_Summit_Speed)
            after (AM_Timer_B360_Full_Reverse) then {
                AM.reverse(AM_Full_Speed)
            }
        }
    }

    val B340_rev = node(B340) {
        onEnter {
            move()
            AM.reverse(AM_Full_Speed)
        }
    }

    val B330_rev = node(B330) {
        onEnter {
            move()
            AM.reverse(AM_Sonora_Speed)
        }
    }

    val B321_rev = node(B321) {
        onEnter {
            AM_Fn_Acquire_Route()
            move()
            AM.reverse(AM_Full_Speed)
            after (AM_Timer_B321_Down_Crossover) then {
                AM.horn()
                AM_bell(On)
                AM.reverse(AM_Crossover_Speed)
            }
        }
    }

    val B503a_rev = node(B503a) {
        onEnter {
            move()
            AM.reverse(AM_Crossover_Speed)
        }
    }

    val B503b_rev = node(B503b) {
        onEnter {
            // Note: this is the normal start block and is never
            // a recover block. We do not set any speed on purpose.
            after (AM_Timer_B503b_Down_Stop) then {
                AM.stop()
                AM.horn()
                AM_bell(Off)
            } and_after (5.seconds) then {
                AM_sound(Off)
            } and_after (2.seconds) then {
                if (PA_Toggle.active) {
                    Passenger_Route.activate()
                } else {
                    PA_Idle_Route.activate()
                }
            }
        }
    }

    onActivate {
        PA_State = EPA_State.Recover
        PA_Train = EPA_Train.Passenger
        if (B370.active) {
            PA_Route.active.start_node(B370_rev)
        } else if (B360.active) {
            PA_Route.active.start_node(B360_rev)
        } else if (B340.active) {
            PA_Route.active.start_node(B340_rev)
        } else if (B330.active) {
            PA_Route.active.start_node(B330_rev)
        } else if (B321.active) {
            PA_Route.active.start_node(B321_rev)
        } else if (B503a.active) {
            PA_Route.active.start_node(B503a_rev)
        } else if (B503b.active) {
            PA_Route.active.start_node(B503b_rev)
        }
    }

    onRecover {
        // We cannot recover from an error during the recover route.
        if (PA_Toggle.active) {
            AM.stop()
            PA_Idle_Route.activate()
        }
    }

    sequence = listOf(
        B370_rev, B360_rev, B340_rev, B330_rev, B321_rev, B503a_rev, B503b_rev)
}

val PA_Recover_Freight_Route = PA_Route.sequence {
    throttle = SP
    timeout = 60 // 1 minute

    fun move() {
        SP_bell(On)
        SP_sound(On)
        SP.horn()
        SP.reverse(SP_Reverse_Speed)
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
                SP_bell(On)
            } and_after (SP_Timer_Down_Stop) then {
                SP_bell(Off)
                SP.horn()
                SP.stop()
            } and_after (5.seconds) then {
                SP_bell(Off)
                SP_sound(Off)
            } and_after (2.seconds) then {
                if (PA_Toggle.active) {
                    Freight_Route.activate()
                } else {
                    PA_Idle_Route.activate()
                }
            }
        }
    }

    onActivate {
        PA_State = EPA_State.Recover
        PA_Train = EPA_Train.Freight
        if (B321.active) {
            PA_Route.active.start_node(B321_rev)
        } else if (B311.active) {
            PA_Route.active.start_node(B311_rev)
        }
    }

    onRecover {
        // We cannot recover from an error during the recover route.
        if (PA_Toggle.active) {
            SP.stop()
            PA_Idle_Route.activate()
        }
    }

    sequence = listOf(B321_rev, B311_rev)
}


// ---------
// Events BL
// ---------

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
    ga_event {
        category = "Automation"
        action = "On"
        label = "Branchline"
        user = "Staff"
    }
    json_event {
        key1 = "Toggle"
        key2 = "Branchline"
        value = "On"
    }
}
on { !BL_Toggle } then {
    ga_event {
        category = "Automation"
        action = "Off"
        label = "Branchline"
        user = "Staff"
    }
    json_event {
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

val BL_Route = activeRoute {
    name = "Branchline"
    toggle = BL_Toggle
    status = { BL_State.toString() }

    onError {
        // --- BL State: Error
        // The current route will trigger the corresponding BL_Recover_Route.
        BL.repeat(1.seconds)
        BL.stop()
        BL_sound(Off)
        ga_event {
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
    timeout = 60 // 1 minute

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
        json_event {
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
            BL_Route.active.start_node(BLParked_fwd)
        } else if (!BLParked && BLStation.active) {
            BL_Route.active.start_node(BLStation_fwd)
        }
    }

    onRecover {
        BL_Route.activate(BL_Recover_Route)
    }

    sequence = listOf(BLParked_fwd, BLStation_fwd, BLTunnel_fwd,
        BLReverse_fwd,
        BLTunnel_rev, BLStation_rev, BLParked_rev)

}

val BL_Recover_Route = BL_Route.sequence {
    throttle = BL
    timeout = 60 // 1 minute

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
            BL_Route.active.start_node(BLReverse_rev)
        } else if (BLTunnel.active) {
            BL_Route.active.start_node(BLTunnel_rev)
        } else if (BLStation.active) {
            BL_Route.active.start_node(BLStation_rev)
        } else if (BLParked.active) {
            BL_Route.active.start_node(BLParked_rev)
        }
    }

    onRecover {
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

on { !PA_Toggle && !B330 &&  B320.active && !B321 } then { T330.normal() }
on { !PA_Toggle && !B330 && !B320 &&  B321.active } then { T330.reverse() }


// ---------
