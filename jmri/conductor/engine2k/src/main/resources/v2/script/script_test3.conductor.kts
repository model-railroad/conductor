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

@file:Suppress("FunctionName", "LocalVariableName", "PropertyName", "ClassName", "UNUSED_PARAMETER")

import com.alfray.conductor.v2.script.dsl.*


// Variables and local declaration.

val On = true
val Off = false

// sensors

val B801         = block ("NS752") named "BLParked"     // 48:1 -- B801
val B802         = block ("NS753") named "BLParked2"    // 48:2 -- B802
val B821         = block ("NS755") named "BLAngelsCamp" // 48:4 -- B821
val B830         = block ("NS758") named "BLCanyon"     // 48:7 -- B830 + B840
val B850         = block ("NS759") named "BLTunnel"     // 48:8 -- B850
val B860         = block ("NS760") named "BLYouBet"     // 48:9 -- B860

val B310         = block ("NS768") named "B310"         // 49:1
val B311         = block ("NS769") named "B311"         // 49:2
val B320         = block ("NS770") named "B320"         // 49:3
val B321         = block ("NS771") named "B321"         // 49:4
val B330         = block ("NS773") named "B330"         // 49:6
val B340         = block ("NS774") named "B340"         // 49:7
val B360         = block ("NS775") named "B360"         // 49:8
val B370         = block ("NS776") named "B370"         // 49:9

val B504         = block ("NS784") named "B504"         // 50:1
val B503a        = block ("NS786") named "B503a"        // 50:3
val B503b        = block ("NS787") named "B503b"        // 50:4
val AIU_Motion   = sensor("NS797") named "AIU-Motion"   // 50:14

val Sat_Toggle   = sensor("NS827") named "Saturday"     // 52:12
val BL_Toggle    = sensor("NS828") named "BL-Toggle"    // 52:13
val ML_Toggle    = sensor("NS829") named "ML-Toggle"    // 52:14

val B713a        = block ("MS713a") named "B713a"       // SDB Trolley
val B713b        = block ("MS713b") named "B713b"       // SDB Trolley


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
val T140        = turnout("NT140")  // DS64 - Sultan
val T150        = turnout("NT150")  // DS64 - Mainline to Sultan
val T151        = turnout("NT151")  // DS64 - Mainline to Napa Yard
val T160        = turnout("NT160")  // DS64 - to Richmond Yard


// Maps

map {
    name = "Mainline"
    svg  = "Map Mainline 2.svg"   // relative to script
    displayOn = SvgMapTarget.Conductor
}
map {
    name = "Mainline"
    svg  = "Map Mainline 1.svg"   // relative to script
    displayOn = SvgMapTarget.RTAC
}

// JSON tracking

exportedVars.jsonUrl = "@~/bin/JMRI/rtac_json_url.txt"


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
    FR.repeat(2.seconds)
}

on { !ML_Toggle } then {
    FR.repeat(0.seconds)
    FR.bell(Off)
}


on { ML_State == EML_State.Ready && ML_Toggle.active && ML_Train == EML_Train.Freight  } then {
    exportedVars.rtacPsaText = "{c:#FF008800}Next Train:\\n${FR_Data.PSA_Name}"
}
on { ML_State == EML_State.Wait  && ML_Toggle.active && ML_Train == EML_Train.Freight  } then {
    exportedVars.rtacPsaText = "{c:#FF008800}Next Train:\\n${FR_Data.PSA_Name}\\nLeaving in 1 minute"
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
>> Timings for NCE Momentum #2, speeds 4/6/8/12.

Walthers Mainline UP 8312 / 8330 (LokSound Essentials):
>> Timings for NCE Momentum #3

UP 722/712
>> Timings for NCE Momentum #3
>> F5 for Beacon light; F6 for rear number plates
>> F8 is mute (0 for sound, Tsunami)

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

enum class EML_State { Ready, Wait, Run, Recover, Error }
enum class EML_Train { Freight }

var ML_State = EML_State.Ready
var ML_Train = EML_Train.Freight
var ML_Start_Counter = 0
var ML_Requires_Wait_After_Run = false

/** Indicates that the Mainline automation is not running or in active recovery.
 *  This does not check the Saturday mode. */
fun EML_State.isIdle() = ML_State != EML_State.Run
        && ML_State != EML_State.Recover

val _enable_FR = true       // For emergencies when one of the trains is not working

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

fun ML_Freight_Align_Turnouts() {
    T311.normal()
    T320.normal()
    T321.normal()
    T322.normal()
    T326.normal()
    T330.reverse()
    T504.normal()
}

fun ML_Release_Turnouts() {
    T311.normal()
    T320.normal()
    T321.normal()
}


// --- Turns mainline engine sound off when automation is turned off

on { ML_State.isIdle() && ML_Toggle.active } then {
    FR.sound(On);   FR.light(On)
    FR.stop()
    ML_Release_Turnouts()
}

val ML_Timer_Stop_Sound_Off = 10.seconds
on { ML_State.isIdle() && !ML_Toggle } then {
    FR.light(Off)
    FR.stop()
    after(ML_Timer_Stop_Sound_Off) then {
        FR.sound(Off)
    }
    ML_Release_Turnouts()
}


// --- Mainline Routes


val ML_Route = routes {
    name = "Mainline"
    toggle = ML_Toggle
    status = {
        "$ML_Train $ML_State"
    }

    onError {
        // The current route will trigger the corresponding ML_Recover_Route.
        FR.stop()
        FR.sound(Off) ; FR.light(Off) ; FR.bell(Off) ; FR_marker(Off)
        analytics.gaEvent {
            category = "Automation"
            action = "Error"
            label = "Mainline"
            user = "Staff"
        }
        exportedVars.rtacPsaText = "{b:red}{c:white}Automation ERROR"

        ML_Fn_Try_Recover_Route()
    }
}

var ML_Idle_Route: IRoute
ML_Idle_Route = ML_Route.idle {
    // The idle route is where we check whether a mainline train should start, and which one.
    name = "ML Ready"

    onActivate {
        ML_State = EML_State.Ready
        FR.stop()
        FR.light(On)
        FR.bell(Off)
    }

    onIdle {
        if (ML_Toggle.active
                && AIU_Motion.active
                && FR.stopped) {
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
        ML_State = EML_State.Wait
        ML_Requires_Wait_After_Run = false

        after (ML_Timer_Wait) then {
            ML_Idle_Route.activate()
        }
    }
}

val ML_Error_Route = ML_Route.idle {
    // The error route is used when we fail to recover from the recovery routes.
    name = "ML Error"

    onActivate {
        ML_State = EML_State.Error
        FR.stop()
    }
}

// Helper method to send start GA event for both mainline routes.
fun ML_Send_Start_GaEvent() {
    ML_Start_Counter++
    analytics.gaEvent {
        category = "Activation"
        action = "Start"
        label = ML_Train.name
        user = ML_Start_Counter.toString()
    }
    jsonEvent {
        key1 = "Depart"
        key2 = ML_Train.name
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

val FR_Data = if (FR.dccAddress == 1067) _FR_Data(
    // Customize slow-to-stop speed at Sonora for Freight 1067
    Station_Speed    = 2.speed,
    Delay_Up_Slow    = 35.seconds,
    Delay_Up_Stop    = 14.seconds,
    B321_maxSecondsOnBlock = 3*60,
) else if (FR.dccAddress == 1072) _FR_Data(
    // Customize slow-to-stop speed at Sonora for Freight 1072
    Station_Speed    = 3.speed,
    Delay_Up_Slow    = 40.seconds,
    Delay_Up_Stop    = 15.seconds,
    B321_maxSecondsOnBlock = 4*60,
) else if (FR.dccAddress == 5278) _FR_Data(
    // Polar Express 5278
    F8_is_Mute       = true, // BLI
    Forward_Speed    = 16.speed,
    Reverse_Speed    = 15.speed,
    Station_Speed    = 12.speed,
    Delay_Up_Slow    = 58.seconds,
    Delay_Up_Stop    = 24.seconds,
    Delay_Down_Slow  = 18.seconds,
    Delay_Down_Stop  = 13.seconds,
    PSA_Name = "Polar Express",
    B321_maxSecondsOnBlock = 4*60,
) else if (FR.dccAddress == 1225) _FR_Data(
    // Polar Express 1225
    F8_is_Mute       = true, // Tsunami
    Forward_Speed    = 15.speed,
    Reverse_Speed    = 12.speed,
    Station_Speed    = 10.speed,
    Saturday_Speed   = 12.speed,
    Delay_Saturday_Into_B321 = 20.seconds,
    Delay_Up_Slow    = 74.seconds,
    Delay_Up_Stop    = 24.seconds,
    Delay_Down_Slow  = 28.seconds,
    Delay_Down_Stop  = 13.seconds,
    PSA_Name = "Polar Express",
    B321_maxSecondsOnBlock = 6*60,
) else _FR_Data()



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
        ML_Train = EML_Train.Freight
        ML_State = EML_State.Run
        exportedVars.rtacPsaText = "{c:#FF008800}Currently Running:\\n${FR_Data.PSA_Name}"
        throttle.incActivationCount()
    }

    val B311_start = node(B311) {
        onEnter {
            // Delay the GA/JSON event till train actually moves -- this prevents the event
            // from being sent when the train is activated and a presence error is detected.
            ML_Send_Start_GaEvent()
            ML_Requires_Wait_After_Run = true

            FR.light(On)
            FR_marker(On)
            FR.sound(On)
            after (FR_Data.Delay_Sound_Started) then {
                FR.horn()
                FR.light(On)
                FR.bell(Off)
                FR.forward(FR_Data.Station_Speed)
                ML_Freight_Align_Turnouts()
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

    val B330_fwd = node(B330) {
        // Error case: we should never reach block B330.
        // If we do, reverse and act on B321_rev
        minSecondsOnBlock = 0
        onEnter {
            FR.reverse(FR_Data.Reverse_Speed)
            FR.bell(On)
        }
    }

    val B321_rev = node(B321) {
        // Error case coming back to B321 after overshooting in B330
        maxSecondsOnBlock = 180
        onEnter {
            FR.horn()
            after (FR_Data.Delay_Reverse_Horn) then {
                FR.horn()
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
                analytics.gaEvent {
                    category = "Activation"
                    action = "Stop"
                    label = ML_Train.name
                    user = ML_Start_Counter.toString()
                }
                ML_Wait_Route.activate()
            }
        }
    }

    sequence = listOf(B311_start, B321_fwd, B311_rev)
    branches += listOf(B321_fwd, B330_fwd, B321_rev, B311_rev)
}



fun ML_Fn_Try_Recover_Route() {
    // Utility method that tries to see if mainline trains are at the correct position
    // and evaluates whether it would be possible to recover them for typical scenarios.
    // This will either activate the normal route (if all blocks look correct), or active
    // the corresponding recovery routes, or as a last resort enter the non-recoverable
    // ML_Error_Route.

    if (!ML_Toggle) {
        ML_Idle_Route.activate()
        return
    }

    val FR_Route = Freight_Route as ISequenceRoute
    val FR_blocks = FR_Route.sequence.map { it.block }.distinct()

    // Some of these sensors do not register properly if we trains are not moving.
    //    if (PA_blocks.count { it.active } == 0) { PA.reverse(1.speed) }
    //    if (FR_blocks.count { it.active } == 0) { FR.reverse(1.speed) }
    // This won't work because the block.active state won't change for this engine iteration.
    // TBD use a timer and reschedule the test in 1 second.

    // x_start: whether there's a train in the "start" block (where it should be when starting).
    // Expected value: 1 for a train waiting at the start block.
    val FR_start = FR_Route.isStartBlockActive()
    // x_occup: Number of blocks active EXCEPT the first one (counted by x_start)
    // Expected value: 0 for a train waiting at the start block.
    val FR_occup = FR_Route.numNonAdjacentBlocksActive(includeFirstBlock = false)
    // x_total: Number of blocks active. Note that PA and FR overlap by 1 block. Includes "start" block.
    // Expected value: 1 for a train waiting at the start block.
    val FR_total = FR_Route.numNonAdjacentBlocksActive(includeFirstBlock = true)
    // occup vs total:
    // - If the train has stopped somewhere else on the track, we should have start=false,
    //   and occup = total =1.
    // - If the train has stopped on a block boundary, we could have start=false, and
    //   occup = total = 1 (because we count adjacent blocks as a single one).
    // - If there's another train occupying the track, we'll have total > 1.
    //   If the train is on its start block, then start=true, occup > 0 (how many extra blocks occupied).
    //   and total = occup + 1 (extra trains + the one on start block).

    log("[ML Recovery] FR start=$FR_start occup=$FR_occup total=$FR_total $FR_blocks")


    if (_enable_FR && (!FR_start || FR_occup == 1)) {
        // PA train is accounted for, where expected.
        // FR train is not at start but there's one FR block occupied, so let's assume it's
        // our train and try to recover that FR train.
        log("[ML Recovery] Recover Freight")
        ML_Recovery_Freight_Route.activate()

    } else if (FR_start && FR_occup == 0) {
        // FR train is accounted for, where expected.
        // PA train is either missing or on a dead block (otherwise the first recovery test
        // above would have matched).
        // In that case we can still run the FR route because it's a subset of the large route,
        // and we verified the route is not occupied.
        log("[ML Recovery] Ignore Passenger, Activate Freight")
        if (ML_Requires_Wait_After_Run) {
            ML_Train = EML_Train.Freight
            ML_Wait_Route.activate()
        } else if (_enable_FR) {
            Freight_Route.activate()
        }

    } else if (FR_total > 1) {
        // We have more than one block occupied per route. This is not recoverable
        // but that's a case special enough we can ask for the track to be cleared.
        val FR_names = FR_blocks.filter { it.active }
        log("[ML Recovery] Track occupied (Freight: $FR_names)")

        val names = FR_names.map { it.name }.distinct()
        exportedVars.rtacPsaText = "{b:blue}{c:white}Automation Warning\\nCheck Track $names"
        analytics.gaEvent {
            category = "Automation"
            action = "Warning"
            label = "Passenger"
            user = "Staff"
        }
        ML_Error_Route.activate()

    } else {
        log("[ML Recovery] Unknown situation. Cannot recover.")
        ML_Error_Route.activate()
    }

    FR.stop()
}

val ML_Recovery_Freight_Route = ML_Route.sequence {
    // Recovery route for the freight mainline train.
    name = "FR Recovery"
    throttle = FR
    minSecondsOnBlock = 0       // deactivated
    maxSecondsOnBlock = 180     // 3 minutes per block max
    maxSecondsEnterBlock = 30

    fun move() {
        FR.bell(On)
        FR.sound(On)
        FR.horn()
        FR.reverse(FR_Data.Reverse_Speed)
    }

    val B321_rev = node(B321) {
        onEnter {
            ML_Freight_Align_Turnouts()
            move()
        }
    }

    val B311_rev = node(B311) {
        onEnter {
            // Note: this is the normal start block and is never
            // a recover block. We do not set any speed on purpose
            // (except to clear B321 below).
            ML_Freight_Align_Turnouts()
            after (FR_Data.Delay_Down_Slow) then {
                FR.bell(On)
            } and_after (FR_Data.Delay_Down_Stop) then {
                FR.bell(Off)
                FR.horn()
                FR.stop()
            } and_after (5.seconds) then {
                FR.bell(Off)
                FR.sound(Off)
            } and_after (2.seconds) then {
                ML_Train = EML_Train.Freight
                ML_Wait_Route.activate()
            }
        }

        whileOccupied {
            if (B321.active) {
                FR.reverse(FR_Data.Reverse_Speed)
            } else {
                // This forces the train to stop prematurely.
                // Its position will get fixed at the next normal run.
                FR.stop()
            }
        }
    }

    onActivate {
        ML_State = EML_State.Recover
        ML_Train = EML_Train.Freight
        ML_Freight_Align_Turnouts()
        when {
            B321.active && B311.active -> route.startNode(B311_rev, trailing=B321_rev)
            B321.active -> route.startNode(B321_rev)
            B311.active -> route.startNode(B311_rev)
            else -> {
               log("ML FR Recovery: WARNING: No condition for start node.")
            }
        }
    }

    onError {
        // We cannot recover from an error during the recover route.
        FR.stop()
        ML_Error_Route.activate()
    }

    sequence = listOf(B321_rev, B311_rev)
}



// ------------------
// Automatic Turnouts
// ------------------

// If automation is off, T330 is automatically selected:
// B320 -> B330 via T330 Normal
// B321 -> B330 via T330 Reverse

on { ML_State.isIdle() && !ML_Toggle && !B330 &&  B320.active && !B321 } then { T330.normal() }
on { ML_State.isIdle() && !ML_Toggle && !B330 && !B320 &&  B321.active } then { T330.reverse() }



// ---------
