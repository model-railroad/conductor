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
val BLRun        = block ("NS765") // 48:14, Disconnected BL activation button

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
val T504        = turnout("NT504")

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

// ---------
// Events PA
// ---------

enum class EPA_State { Idle, Station, Shuttle, Manual, Error, Wait }
enum class EPA_Train { Passenger, Freight, }

var PA_State = EPA_State.Idle
var PA_Train = EPA_Train.Passenger
var PA_Start_Counter = 0

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
    status = { PA_State.toString() }
    onError {
        // --- PA State: Error
        // Note this state can only be cleared using an RTAC reset.
        PA_State = EPA_State.Error
        AM.repeat(0.seconds)
        SP.repeat(0.seconds)
        AM.stop()
        SP.stop()
        AM.sound(Off) ; AM.light(Off) ; AM.f1(Off)
        SP.sound(Off) ; SP.light(Off) ; SP.f1(Off) ; SP.f5(Off)
        ga_event {
            category = "Automation"
            action = "Error"
            label = "Passenger"
            user = "Staff"
        }
        exportedVars.RTAC_PSA_Text = "{b:red}{c:white}Automation ERROR" ;
        estop()
    }
}

val PA_Idle_Route = PA_Route.idle {}

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
        exportedVars.RTAC_PSA_Text = "{c:blue}Currently Running:\n\nPassenger"
        json_event {
            key1 = "Depart"
            key2 = "Passenger"
        }
        AM.light(On)
        AM.sound(On)
        SP.sound(Off)
        AM.forward(AM_Leaving_Speed)
    }

    val B503b_start = node(B503b) {
        whileOccupied {
            if (AM.stopped) {
                AM.forward(AM_Leaving_Speed)
                AM.f1(Off)
                SP.sound(Off)
                AM.horn()
                AM_Fn_Acquire_Route()
                AM.f1(On)
                after(AM_Delayed_Horn) then {
                    AM.horn()
                }
            }
        }
    }

    val B503a_fwd = node(B503a) {
    }

    val B321_fwd = node(B321) {
        onEnter {
            // Wait for train to have cleared up T320 before switching to full speed.
            // The problem is that B321 activates as train hits diverted leg of turnout.
            after(AM_Leaving_To_Full_Speed) then {
                // TBD: validate PA_Toggle is still on
                AM.forward(AM_Full_Speed)
            }

            // Mid_Station doppler on the way up
            after(AM_Timer_B321_Up_Doppler) then {
                AM.f9(On)
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
        after(AM_Timer_B330_Down_Speed) then {
            AM.horn()
            AM.reverse(AM_Sonora_Speed)
        }
    }

    val B321_rev = node(B321) {
        AM.reverse(AM_Full_Speed)
        AM.f9(On)
        AM.f9(Off)
        after(AM_Timer_B321_Down_Crossover) then {
            AM.horn()
            AM.f1(On)
            AM.reverse(AM_Crossover_Speed)
        }
    }

    val B503a_rev = node(B503a) {
    }

    val B503b_rev = node(B503b) {
        after(AM_Timer_B503b_Down_Stop) then {
            AM.stop()
            AM.horn()
            AM.f1(Off)
        } and_after(AM_Timer_Down_Station_Lights_Off) then {
            ga_event {
                category = "Activation"
                action = "Stop"
                label = PA_Train.name
                user = PA_Start_Counter.toString()
            }
            PA_Train = EPA_Train.Freight
            PA_State = EPA_State.Wait
            route.activate(PA_Idle_Route)
        }
    }

    sequence = listOf(
        B503b_start, B503a_fwd, B321_fwd, B330_fwd, B340_fwd, B360_fwd,
        B370_end,
        B360_rev, B340_rev, B330_rev, B321_rev, B503a_rev, B503b_rev)
}


//val Freight_Route = PA_Route.sequence {
//    throttle = SP
//    timeout = 60
//
//    // Speeds: Doodlebug: 8/4; RDC: 20/12; 804: 16/12/4; 6580: 8/6/2; 655: 16/12/8; 2468: 28/20/12; 1840: 20/16/12; 5278:16/16/12; 024: 8/6/2
//    val SP_Forward_Speed    = 8.speed
//    val SP_Reverse_Speed    = 4.speed
//    val SP_Station_Speed    = 2.speed
//
//    val SP_Sound_Started    = 2.seconds
//    val SP_Timer_Up_Slow    = 40.seconds    // B321 time to station speed: RDC=40, Doodlebug=60, 804=60.
//    val SP_Timer_Up_Stop    = 12.seconds    // Time on slow down before stop
//    val SP_Timer_Up_Reverse = 30.seconds    // Time stopped before reverse
//    val SP_Timer_Reverse_Horn = 2.seconds
//    val SP_Timer_Down_Slow  = 24.seconds    // Time before slow on B311. RDC=10, Doodlebug or 804=18. 024=21, 5278=12.
//    val SP_Timer_Down_Stop  = 16.seconds    // Time on slow down before stop.
//    val SP_Timer_Down_Off   = 20.seconds
//    val SP_Sound_Stopped    = 2.seconds
//
//    fun SP_Fn_Acquire_Route() {
//        T311.normal()
//        T320.normal()
//        T321.normal()
//        T322.normal()
//        T326.normal()
//        T330.reverse()
//        T504.normal()
//    }
//
//    onActivate {
//        exportedVars.RTAC_PSA_Text = "{c:#FF008800}Currently Running:\n\nFreight"
//        json_event {
//            key1 = "Depart"
//            key2 = "Freight"
//        }
//    }
//
//    val B311_start = node(B311) {
//        whileOccupied {
//            SP.light(On)
//            SP.f5(On)
//            SP.sound(On)
//            AM.sound(Off)
//            after(SP_Sound_Started) then {
//                SP.horn()
//                SP.light(On)
//                SP.f1(Off)
//                SP.forward(SP_Forward_Speed)
//                SP_Fn_Acquire_Route()
//            } and_after(2.seconds) then {
//                SP.horn()
//            }
//        }
//    }
//
//    val B321_fwd = node(B321) {
//        onEnter {
//            SP.forward(SP_Forward_Speed)
//            after(SP_Timer_Up_Slow) then {
//                SP.horn()
//                SP.f1(On)
//                SP.forward(SP_Station_Speed)
//            } and_after(SP_Timer_Up_Stop) then {
//                // Stop in B321. Normal case is to *not* go into B330.
//                SP.horn()
//                SP.stop()
//                SP.f1(Off)
//                // This is the long stop at the station.
//            } and_after(SP_Timer_Up_Reverse) then {
//                // Start reversing after the long stop.
//                SP.horn()
//                SP.reverse(SP_Reverse_Speed)
//                SP.f1(On)
//            } and_after(SP_Timer_Reverse_Horn) then {
//                SP.horn()
//            } and_after(SP_Timer_Reverse_Horn) then {
//                SP.horn()
//                SP.f1(Off)
//                // Normal case: continue to B311_rev.
//            }
//        }
//
//        whileOccupied {
//            // PA_Toggle off reverts train to down but not in the start block.
//            // (we can't change the timer to stop in block B311 if we're already in it).
//            if (!PA_Toggle) {
//                SP.stop()
//            }
//        }
//    }
//
//    val B330_fwd = node(B330) {
//        // Error case: we should never reach block B330.
//        // If we do, reverse and act on B321_rev
//        onEnter {
//            SP.reverse(SP_Reverse_Speed)
//            SP.f1(On)
//        }
//    }
//
//    val B321_rev = node(B321) {
//        // Error case coming back to B321 after overshooting in B330
//        onEnter {
//            SP.horn()
//            after(SP_Timer_Reverse_Horn) then {
//                SP.horn()
//            }
//        }
//    }
//
//    val B311_rev = node(B311) {
//        onEnter {
//            after(SP_Timer_Down_Slow) then {
//                SP.f1(On)
//            } and_after(SP_Timer_Down_Stop) then {
//                SP.f1(Off)
//                SP.horn()
//                SP.stop()
//            } and_after(SP_Timer_Down_Off) then {
//                SP.horn()
//                SP.f1(Off)
//                SP.f1(Off)
//                SP.light(Off)
//                SP.f5(Off)
//            } and_after(SP_Sound_Stopped) then {
//                SP.sound(Off)
//                AM.sound(On)
//                ga_event {
//                    category = "Activation"
//                    action = "Stop"
//                    label = PA_Train.name
//                    user = PA_Start_Counter.toString()
//                }
//                PA_Train = EPA_Train.Passenger
//                PA_State = EPA_State.Wait
//                route.activate(PA_Idle_Route)
//            }
//        }
//    }
//
//    sequence = listOf(B311_start, B321_fwd, B311_rev)
//    branches += listOf(B321_fwd, B330_fwd, B321_rev, B311_rev)
//}

// --- PA State: Idle

val PA_Timer_Stop_Sound_Off = 10.seconds

on { PA_State == EPA_State.Idle && PA_Toggle.active } then {
    AM.sound(On);   AM.light(On)
    SP.sound(On);   SP.light(On)
    AM.stop();      SP.stop()
    AM.repeat(2.seconds);
    SP.repeat(2.seconds)
    PA_Fn_Release_Route()
    PA_State = EPA_State.Station
}

on { PA_State == EPA_State.Idle && !PA_Toggle } then {
    AM.light(Off);  SP.light(Off)
    AM.stop();      SP.stop()
    after(PA_Timer_Stop_Sound_Off) then {
        AM.sound(Off);
        SP.sound(Off)
    }
    PA_Fn_Release_Route()
}

on { PA_Toggle } then {
    AM.repeat(2.seconds);
    SP.repeat(2.seconds)
}

on { !PA_Toggle } then {
    AM.repeat(2.seconds);
    SP.repeat(2.seconds)
    AM.f1(Off);
    SP.f1(Off)
}

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


// --- PA State: Wait (to station)

// RM DEBUG: Use 5 here instead of 60
val PA_Timer_Wait = 60.seconds  // 1 minute
on { PA_State == EPA_State.Wait } then {
    after(PA_Timer_Wait) then {
        PA_State = EPA_State.Station
    }
}


// --- PA State: Station

// Departure from Station (going up)
// REQUIRES both trains stopped.
on { PA_Train == EPA_Train.Passenger && PA_State == EPA_State.Station && AM.stopped && SP.stopped && AIU_Motion.active } then {
    PA_State = EPA_State.Shuttle
}
on { PA_Train == EPA_Train.Freight && PA_State == EPA_State.Station && AM.stopped && SP.stopped && AIU_Motion.active } then {
    PA_State = EPA_State.Shuttle
}

on { PA_State == EPA_State.Shuttle } then {
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
    reset_timers("PA", "AM", "SP")
}

on { PA_State == EPA_State.Station && PA_Toggle.active && PA_Train == EPA_Train.Passenger } then {
    exportedVars.RTAC_PSA_Text = "{c:blue}Next Train:\n\nPassenger"
}
on { PA_State == EPA_State.Wait    && PA_Toggle.active && PA_Train == EPA_Train.Passenger } then {
    exportedVars.RTAC_PSA_Text = "{c:blue}Next Train:\n\nPassenger\n\nLeaving in 1 minute"
}
on { PA_State == EPA_State.Station && PA_Toggle.active && PA_Train == EPA_Train.Freight  } then {
    exportedVars.RTAC_PSA_Text = "{c:#FF008800}Next Train:\n\nFreight"
}
on { PA_State == EPA_State.Wait    && PA_Toggle.active && PA_Train == EPA_Train.Freight  } then {
    exportedVars.RTAC_PSA_Text = "{c:#FF008800}Next Train:\n\nFreight\n\nLeaving in 1 minute"
}

// DEBUG place to force AM_up vs SP_up
on { PA_State == EPA_State.Shuttle && PA_Train == EPA_Train.Passenger } then {
    PA_Route.activate(Passenger_Route)
}
//on { PA_State == EPA_State.Shuttle && PA_Train == EPA_Train.Freight   } then {
//    PA_Route.activate(Freight_Route)
//}

on { PA_State == EPA_State.Station && !PA_Toggle } then {
    reset_timers("PA", "AM", "SP")
    PA_State = EPA_State.Idle
    PA_Route.activate(PA_Idle_Route)
}


// ---------
// Events BL
// ---------


val BL = throttle(191)

val BL_Speed = 10.speed
val BL_Speed_Station = 6.speed

enum class EBL_State { Start, Wait, Ready, Shuttle, ToParked }

var BL_State = EBL_State.Start

var BL_Start_Counter = 0

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

val BL_Route = activeRoute {
    name = "Branchline"
    toggle = BL_Toggle
    status = { BL_State.toString() }

    onError {
        // --- BL State: Error
        BL.repeat(1.seconds)
        BL.stop()
        BL.sound(Off)
        ga_event {
            category = "Automation"
            action = "Error"
            label = "Branchline"
            user = "Staff"
        }
    }
}

val BL_Idle_Route = BL_Route.idle {}

on { BL_Route.error } then {
    // Tip: can't call this from BL_Route.onError{} as neither BL_Route
    // nor BL_Idle_Route are defined inside the onError block yet.
    BL_Route.activate(BL_Idle_Route)
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

    fun doStart() {
        BL_State = EBL_State.Shuttle
        BL.horn()
        BL.f1(On)
        after(BL_Timer_Start_Delay) then {
            BL.f1(Off)
            BL.horn()
            BL.forward(BL_Speed)
        }
        json_event {
            key1 = "Depart"
            key2 = "Branchline"
        }
    }

    val BLParked_fwd = node(BLParked) {
        whileOccupied {
            // When starting from BLParked
            if (BL.stopped && BL_State == EBL_State.Ready) {
                doStart()
            }
        }
    }

    val BLStation_fwd = node(BLStation) {
        whileOccupied {
            // When starting from BLStation
            if (BL.stopped && BL_State == EBL_State.Ready) {
                doStart()
            }
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
            BL.f1(On)
            after(BL_Timer_RevStation_Stop) then {
                // Toggle Stop/Reverse/Stop to turn off the Reverse front light.
                // Next event should still be the BLReverse Stopped one.
                BL.stop()
                BL.horn()
                BL.reverse(1.speed)
                BL.stop()
            } and_after(BL_Timer_Bell_Delay) then {
                BL.f1(Off)
            } and_after(BL_Timer_RevStation_Pause) then {
                BL.f1(On)
                BL.horn()
            } and_after(5.seconds) then {
                BL.reverse(BL_Speed)
            } and_after(BL_Timer_Bell_Delay) then {
                BL.light(Off)
                BL.f1(Off)
                BL.f8(On)
                BL.f9(On)
                BL.f10(On)
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
            if (BL_State == EBL_State.Shuttle) {
                BL.f1(On)
                T324.normal()
                BL.reverse(BL_Speed_Station)
                after(BL_Timer_Station_Stop) then {
                    BL.stop()
                    BL.horn()
                } and_after(BL_Timer_Station_Rev3) then {
                    BL.horn()
                    BL.reverse(BL_Speed_Station)
                }
            }
        }
    }

    val BLParked_rev = node(BLParked) {
        onEnter {
            // We went too far, but it's not a problem / not an error.
            BL.stop()
            after(3.seconds) then {
                BL.f1(Off)
            } and_after(2.seconds) then {
                BL.f8(Off)
                BL.f9(Off)
                BL.f10(Off)
                BL_State = EBL_State.Wait
                BL_Route.activate(BL_Idle_Route)
            }
        }
    }

    onActivate {
        // It's ok to start either from BLParked or BLStation
        if (BLParked.active) {
            BL_Route.active.start_node(BLParked_fwd)
        } else if (!BLParked && BLStation.active) {
            BL_Route.active.start_node(BLStation_fwd)
        }
    }

    onRecover {
        // Try to bring it backwards to the station
        BL.reverse(BL_Speed_Station)
        BL.f1(On)
    }

    sequence = listOf(BLParked_fwd, BLStation_fwd, BLTunnel_fwd,
        BLReverse_fwd,
        BLTunnel_rev, BLStation_rev, BLParked_rev)

}

//
//val BL_StationToParked_Route = BL_Route.sequence {
//    throttle = BL
//    timeout = 60 // 1 minute
//
//    onActivate {
//        if (!BLParked) {
//            BL.reverse(BL_Speed_Station)
//        }
//    }
//
//    val BLStation_rev = node(BLStation) {
//        onEnter {
//            BL.f1(On)
//        }
//    }
//
//    val BLParked_rev = node(BLParked) {
//        onEnter {
//            BL.stop()
//            after(3.seconds) then {
//                BL.f1(Off)
//            } and_after(2.seconds) then {
//                BL.f8(Off)
//                BL.f9(Off)
//                BL.f10(Off)
//                BL_State = EBL_State.Wait
//                BL_Route.activate(BL_Idle_Route)
//            }
//        }
//    }
//
//    sequence = listOf(BLStation_rev, BLParked_rev)
//}

// --- BL State: Wait

val BL_Timer_Wait = timer(120.seconds)  // 300=5 minutes -- changed to 2 minutes

//on { BL_Route.active == BL_Idle_Route && !BL_Toggle } then {
//    BL_State = EBL_State.ToParked
//    BL_Route.activate(BL_StationToParked_Route)
//}

on { BL_State == EBL_State.Wait } then {
    reset_timers("BL") // TODO obsolete
    BL_Timer_Wait.start()
    BL.stop()
}

on { BL_Timer_Wait.active && BL_Toggle.active && AIU_Motion.active } then {
    BL_State = EBL_State.Ready
    BL_Route.activate(BL_Shuttle_Route)
}

on { BL_State == EBL_State.Start && BL_Toggle.active && AIU_Motion.active } then {
    BL_State = EBL_State.Ready
    BL_Route.activate(BL_Shuttle_Route)
}

on { BL_Toggle } then {
    BL.repeat(2.seconds)
    BL.light(Off)
}

on { !BL_Toggle } then {
    BL.repeat(0.seconds)
    BL.light(Off)
    BL.f1(Off)
    BL.f8(Off)
    BL.f9(Off)
    BL.f10(Off)
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
