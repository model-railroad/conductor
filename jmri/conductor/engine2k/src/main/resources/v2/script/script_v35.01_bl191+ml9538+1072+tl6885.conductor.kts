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

@file:Suppress("FunctionName", "LocalVariableName", "PropertyName", "ClassName", "UNUSED_PARAMETER")

import java.time.Month
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
exportedVars.dazzUrl = "@~/bin/JMRI/rtac_dazz_url.txt"

// GA Tracking

analytics.configure("@~/bin/JMRI/rtac_ga_tracking_id.txt")
val GA_URL = "http://consist.alfray.com/train/"

// MQTT Broker & Ambiance Setup

mqtt.configure("~/bin/JMRI/rtac_mqtt_conf.json")

fun Ambiance_On() {
    val _ambianceMonth = now().month
    log("[Ambiance] Light effect for ${_ambianceMonth}")

    when (_ambianceMonth) {
        Month.NOVEMBER -> {
            // Halloween Ambience Light
            mqtt.publish(
                "ambiance/script/init",
                "SlowFill 0.025 #000000 1 ; Brightness 0.5 ; SlowFill 0.1 #FF1000 40 #FF4000 10"
            )
            mqtt.publish(
                "ambiance/script/event",
                "Slide -0.05 100")
        }

        Month.DECEMBER -> {
            // Xmas Ambiance Light
            mqtt.publish(
                "ambiance/script/init",
                "SlowFill 0.025 #000000 1 ; Brightness 0.5 ; SlowFill 0.1 #00FF00 10 #FF0000 10"
            )
            mqtt.publish(
                "ambiance/script/event",
                "Slide -0.1 80")
        }

        else -> {
            // Default Yellowish Warm Lights with accent on where trains are parked
            mqtt.publish(
                "ambiance/script/init",
                "SlowFill 0.025 #000000 1 ; Brightness 0.75 ; SlowFill 0.025 #080300 13 #FF4000 25 #080300 70"
            )
            mqtt.publish(
                "ambiance/script/event",
                "Slide -0.05 200")
        }
    }
}

Ambiance_On()

fun Ambiance_Off() {
    log("[Ambiance] Off")
    mqtt.publish("ambiance/script/init", "Brightness 0.5 ; SlowFill 0.25 #000000 1")
}

var _ambiance_counter = 0
fun Ambiance_Trigger() {
    log("[Ambiance] Trigger")
    mqtt.publish("ambiance/event/trigger", _ambiance_counter.toString())
    _ambiance_counter++
}


// MQTT Setup for Turnout LED Displays

mqtt.publish("turnout/t330/script/init", "Brightness 0.1 ; Fill #FFFFFF 1")


// -----------------
// DS64 Turnouts
// -----------------
//
// These turnouts need to be reset to mainline when the layout starts.

listOf(T450, T140, T150, T151, T160).forEachIndexed { i, t ->
    on((10 + i).seconds) { true } then { t.reverse() }
    on((15 + i).seconds) { true } then { t.normal()  }
}


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
    Ambiance_Off()
}

on { BL_Toggle.active && exportedVars.conductorTime == End_Of_Day_HHMM } then {
    BL_Toggle.active(Off)
}

on { ML_Toggle.active && ML_Saturday.isOff() } then {
    exportedVars.rtacPsaText = "Automation Started"
}

on { !ML_Toggle && exportedVars.conductorTime == End_Of_Day_HHMM } then {
    exportedVars.rtacPsaText = "{c:red}Automation Turned Off\\nat 4:50 PM"
}

on { !ML_Toggle && exportedVars.conductorTime != End_Of_Day_HHMM
                && ML_Saturday.isOff() } then {
    exportedVars.rtacPsaText = "{c:red}Automation Stopped"
}

on { !ML_Saturday.isOff() && exportedVars.conductorTime != End_Of_Day_HHMM } then {
    exportedVars.rtacPsaText = "{c:red}Saturday Trains Running"
}

// ---------------------
// Events & RTAC Display
// ---------------------

on { ML_Toggle  } then {
    Ambiance_On()
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
    dazzEvent {
        key = "toggle/passenger"
        state = true
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
    dazzEvent {
        key = "toggle/passenger"
        state = false
    }
}

on { ML_Toggle } then {
    PA.repeat(2.seconds)
    FR.repeat(2.seconds)
}

on { !ML_Toggle } then {
    PA.repeat(0.seconds)
    FR.repeat(0.seconds)
    PA.bell(Off)
    FR.bell(Off)
}


on { ML_State == EML_State.Ready && ML_Toggle.active && ML_Train == EML_Train.Passenger } then {
    exportedVars.rtacPsaText = "{c:blue}Next Train:\\n${PA_Data.PSA_Name}"
}
on { ML_State == EML_State.Wait  && ML_Toggle.active && ML_Train == EML_Train.Passenger } then {
    exportedVars.rtacPsaText = "{c:blue}Next Train:\\n${PA_Data.PSA_Name}\\nLeaving in 1 minute"
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
enum class EML_Train { Passenger, Freight, Saturday }
enum class EML_Saturday { Init, Off, Setup, On, Reset }

var ML_State = EML_State.Ready
var ML_Train = EML_Train.Passenger
var ML_Saturday = EML_Saturday.Init
var ML_Start_Counter = 0
var ML_Requires_Wait_After_Run = false

/** Indicates that the Mainline automation is not running or in active recovery.
 *  This does not check the Saturday mode. */
fun EML_State.isIdle() = ML_State != EML_State.Run
        && ML_State != EML_State.Recover

/** Indicates that the Saturday automation is off -- it's not doing a setup/reset,
 *  and it's not idle in storage. */
fun EML_Saturday.isOff() = ML_Saturday == EML_Saturday.Off

/** Indicates that the Saturday automation is idle -- it's not doing a setup/reset;
 *  the train can be in storage or at its normal position, but it's not moving. */
fun EML_Saturday.isIdle() = ML_Saturday == EML_Saturday.Off || ML_Saturday == EML_Saturday.On

val _enable_FR = true       // For emergencies when one of the trains is not working
val _enable_PA = true       // Warning: see ML_Fn_Try_Recover_Route() for special recovery cases.
val _enable_Saturday = true

// PA is UP 722 or 712 or 3609 or 8312 or 8330 or 8749
val PA = throttle(9538) {
    // Full mainline route -- Passenger.
    name = "PA"
    onBell  { on -> throttle.f1(on) }
    onSound { on -> PA_sound(on) }
}

// FR is Beeline 1067 or 1072
// FR is 1225 for Polar Express
val FR = throttle(1072) {
    // Short mainline route -- Freight.
    name = "FR"
    onBell  { on -> throttle.f1(on) }
    onSound { on -> FR_sound(on) }
}


fun PA_startup_sound() {
    // TCS WOW Diesel: F6=1 to have prime mover sound.
    if (PA.dccAddress == 9538) {
        PA.f6(On)
    }
}

fun PA_sound(on: Boolean) {
    if (PA_Data.F8_is_Mute) {
        PA.f8(!on)
    } else {
        PA.f8(on)
    }
}

fun FR_sound(on: Boolean) {
    if (FR_Data.F8_is_Mute) {
        FR.f8(!on)
    } else {
        FR.f8(on)
    }
}

fun PA_doppler(on: Boolean) {
    /* no-op on 8749, 8330, 722 */
}

fun PA_beacon(on: Boolean) {
    // For UP 712 / 722
    // PA.f5(on); PA.f6(on);
    // No-op on 8312, as one of the ditch lights is broken
}

fun FR_marker (on: Boolean) {
    /* no-op on 1067, 1072 */
}

fun ML_Passenger_Align_Turnouts() {
    T311.reverse()
    T320.normal()
    T321.normal()
    T322.normal()
    T326.normal()
    T330.reverse()
    T370.normal()
    T504.normal()
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


// --- AM Auto Lights

// RM 2024-02-13 TEMP 8312 with new LS5 has a broken Ditch light, don't turn on.
on { PA.stopped } then { PA.f5(Off) }
// on { PA.forward } then { PA.f5(On) }
// on { PA.reverse } then { PA.f5(On) }


// --- Turns mainline engine sound off when automation is turned off

on { ML_State.isIdle() && ML_Saturday.isOff() && ML_Toggle.active } then {
    PA.sound(On);   PA.light(On)
    FR.sound(On);   FR.light(On)
    PA.stop();      FR.stop()
    ML_Release_Turnouts()
}

val ML_Timer_Stop_Sound_Off = 10.seconds
on { ML_State.isIdle() && ML_Saturday.isIdle() && !ML_Toggle } then {
    PA.light(Off);  FR.light(Off)
    PA.stop();      FR.stop()
    after(ML_Timer_Stop_Sound_Off) then {
        PA.sound(Off)
        FR.sound(Off)
    }
    ML_Release_Turnouts()
}


// --- Mainline Routes


val ML_Route = routes {
    name = "Mainline"
    toggle = ML_Toggle
    status = {
        if (ML_Saturday.isOff())
            "$ML_Train $ML_State"
        else
            "$ML_Train $ML_Saturday"
    }

    onError {
        // The current route will trigger the corresponding ML_Recover_Route.
        PA.stop()
        FR.stop()
        PA.sound(Off) ; PA.light(Off) ; PA.bell(Off)
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
        PA.stop()
        PA.light(On)
        PA.bell(Off)

        FR.stop()
        FR.light(On)
        FR.bell(Off)

        if (_enable_Saturday && ML_Saturday == EML_Saturday.Init) {
            if (Sat_Toggle.active && !B311.active && B503a.active && B503b.active) {
                // This looks like Freight is in the Saturday storage location
                // with the Passenger at its default startup location.
                ML_Saturday = EML_Saturday.On
                ML_Train = EML_Train.Saturday
            } else {
                // Otherwise do not assume anything about the Saturday mode.
                ML_Saturday = EML_Saturday.Off
            }
        }
    }

    onIdle {
        if (ML_Toggle.active
                && AIU_Motion.active
                && PA.stopped
                && FR.stopped
                && ML_Saturday.isOff()) {
            if (!_enable_PA) {
                Freight_Route.activate()
            } else if (!_enable_FR) {
                Passenger_Route.activate()
            } else {
                when (ML_Train) {
                    EML_Train.Passenger -> Passenger_Route.activate()
                    EML_Train.Freight -> Freight_Route.activate()
                    EML_Train.Saturday -> { /* no-op */ }
                }
            }
        } else if (ML_Saturday != EML_Saturday.Init) {
            if (ML_Saturday.isOff() && Sat_Toggle.active && ML_State.isIdle() && !ML_Toggle) {
                SaturdaySetup_Route.activate()
            } else if (ML_Saturday == EML_Saturday.On && !Sat_Toggle.active) {
                SaturdayReset_Route.activate()
            }
        }
    }
}

val ML_Wait_Route = ML_Route.idle {
    // The wait route creates the pause between two mainline train runs.
    name = "ML Wait"

    // Wait 1 minute (or wait 2 if one of the routes is disabled)
    val ML_Timer_Wait = if (_enable_FR xor _enable_PA) 2.minutes else 1.minutes

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
        PA.stop()
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

data class _PA_Data(
    /// True: F8=1 engages mute, 0 engages sound. False: F8=1 engages sound, 0 engages mute).
    // LokSound Sound for 8330/8312 or 3609 : F8_is_Mute=false
    // Tsunami Sound for 8401, 8749, 8736 : F8_is_Mute=true
    val F8_is_Mute: Boolean = false,

    // Default values for legacy UP 8312 or 8330 ESU Essentials Decoder
    val Leaving_Speed: DccSpeed             = 20.speed,
    val Summit_Speed: DccSpeed              = 22.speed,
    val Summit_Bridge_Speed: DccSpeed       = 16.speed,
    val Sonora_Speed: DccSpeed              = 18.speed,
    val Crossover_Speed: DccSpeed           = 18.speed,
    val Full_Speed: DccSpeed                = 24.speed,
    val Recover_Speed: DccSpeed             = 16.speed,

    val Delay_Horn: Delay                   = 2.seconds,
    val Delay_Startup_Sound: Delay          = 8.seconds,
    val Delay_Leaving_To_Full_Speed: Delay  = 15.seconds,
    val Delay_B321_Up_Doppler: Delay        = 27.seconds,
    val Delay_B330_Up_Resume: Delay         = 12.seconds,
    val Delay_B340_Up_Horn: Delay           = 10.seconds,
    /// time running at AM_Summit_Speed before stopping
    val Delay_B370_Entrance: Delay          = 4.seconds,
    val Delay_B370_Forward_Stop: Delay      = 17.seconds,
    val Delay_B370_Pause_Delay: Delay       = 30.seconds,
    val Delay_B360_Full_Reverse: Delay      = 12.seconds,
    val Delay_B330_Down_Speed: Delay        = 8.seconds,
    val Delay_B321_Down_Crossover: Delay    = 35.seconds,
    val Delay_B503b_Down_Stop: Delay        = 9.seconds,
    val Delay_Down_Station_Lights_Off: Delay = 10.seconds,

    val PSA_Name: String = "Passenger",
)

val PA_Data = if (PA.dccAddress == 8401) _PA_Data(
    F8_is_Mute                = true, // Tsunami
    Leaving_Speed             = 4.speed,
    Summit_Speed              = 4.speed,
    Summit_Bridge_Speed       = 6.speed,
    Sonora_Speed              = 6.speed,
    Crossover_Speed           = 4.speed,
    Full_Speed                = 8.speed,
    Recover_Speed             = 8.speed,
    Delay_B370_Entrance       = 10.seconds,
) else if (PA.dccAddress == 3609) _PA_Data(
    F8_is_Mute                = false, // LokSound
    Leaving_Speed           = 10.speed,
    Summit_Speed            = 10.speed,
    Summit_Bridge_Speed     = 8.speed,
    Sonora_Speed            = 12.speed,
    Crossover_Speed         = 8.speed,
    Full_Speed              = 16.speed,
    Recover_Speed           = 12.speed,
    Delay_B330_Up_Resume    = 16.seconds,
    Delay_B370_Entrance     = 8.seconds,
) else if (PA.dccAddress == 8330 || PA.dccAddress == 8312) _PA_Data(
    // Values for Walthers UP 8312 or 8330 with new LS5 using S0822 sound project.
    F8_is_Mute                = false, // LokSound
    Leaving_Speed           = 8.speed,
    Summit_Speed            = 10.speed,
    Summit_Bridge_Speed     = 8.speed,
    Sonora_Speed            = 14.speed,
    Crossover_Speed         = 8.speed,
    Full_Speed              = 20.speed,
    Recover_Speed           = 12.speed,
    Delay_B370_Entrance     = 4.seconds,
) else if (PA.dccAddress == 8749 || PA.dccAddress == 8736) _PA_Data(
    // Values for Athearn UP 8749 or 8736.
    F8_is_Mute                = true, // Tsunami
    Leaving_Speed           = 6.speed,
    Summit_Speed            = 6.speed,
    Summit_Bridge_Speed     = 4.speed,
    Sonora_Speed            = 8.speed,
    Crossover_Speed         = 4.speed,
    Full_Speed              = 12.speed,
    Recover_Speed           = 8.speed,
    Delay_B370_Entrance     = 4.seconds,
    Delay_B370_Forward_Stop = 14.seconds,
    Delay_B360_Full_Reverse = 20.seconds,
    Delay_B340_Up_Horn      = 10.seconds,
    Delay_B503b_Down_Stop   = 10.seconds,
) else if (PA.dccAddress == 9538) _PA_Data(
    // Values for Bachmann UP 9538.
    F8_is_Mute                = true, // TCS WoW Diesel
    Leaving_Speed           = 10.speed,
    Summit_Speed            = 10.speed,
    Summit_Bridge_Speed     = 8.speed,
    Sonora_Speed            = 14.speed,
    Crossover_Speed         = 8.speed,
    Full_Speed              = 16.speed,
    Recover_Speed           = 14.speed,
    Delay_B370_Entrance     = 4.seconds,
    Delay_B370_Forward_Stop = 14.seconds,
    Delay_B360_Full_Reverse = 20.seconds,
    Delay_B340_Up_Horn      = 10.seconds,
    // Delay_B321_Down_Crossover = 27.seconds,
    Delay_B503b_Down_Stop   = 8.seconds,
) else _PA_Data()



val Passenger_Route = ML_Route.sequence {
    // The mainline passenger route sequence.
    name = "Passenger"
    throttle = PA
    minSecondsOnBlock = 10
    maxSecondsOnBlock = 120 // 2 minutes per block max
    maxSecondsEnterBlock = 30

    onError {
        // no-op
    }

    onActivate {
        if (!B311.active) {
            // If the Freight train is not on block B311, we don't know where it is,
            // and we may collide with it. In this case enter error mode right away.
            log("[ML Passenger] Freight missing in B311.")
            if (_enable_FR) {
                // This action is deactivated if the Freight train has been deactivated.
                ML_Fn_Try_Recover_Route()
            }
        }

        ML_Train = EML_Train.Passenger
        ML_State = EML_State.Run
        exportedVars.rtacPsaText = "{c:blue}Currently Running:\\n${PA_Data.PSA_Name}"
        throttle.incActivationCount()
        PA.light(On)
        PA.bell(Off)
        PA_startup_sound()
        PA.sound(On)
        FR.sound(Off)
    }

    val B503b_start = node(B503b) {
        // After a recovery, the train may be just at the edge of the block.
        // in which case we may be leaving the block just after the horn delay.
        minSecondsOnBlock = 2

        onEnter {
            // Delay the GA/JSON event till train actually moves -- this prevents the event
            // from being sent when the train is activated and a presence error is detected.
            ML_Send_Start_GaEvent()
            ML_Requires_Wait_After_Run = true

            FR.sound(Off)
            PA.horn()
            ML_Passenger_Align_Turnouts()
            PA.bell(On)
            PA_beacon(On)
            after (PA_Data.Delay_Startup_Sound) then {
                PA.horn()
                PA.forward(PA_Data.Leaving_Speed)
                Ambiance_Trigger()
            }
        }
    }

    val B503a_fwd = node(B503a) {
        // Time in block fluctuates around 10 seconds.
        minSecondsOnBlock = 4
        maxSecondsOnBlock = 30
        maxSecondsEnterBlock = 10
        onEnter {
            PA.bell(Off)
        }
    }

    val B504_fwd = node(B504) {
        minSecondsOnBlock = 8
        maxSecondsOnBlock = 80		// was 40; need more time with new 8312 LS5
        onEnter {
            PA.horn()
        }
    }

    val B321_fwd = node(B321) {
        onEnter {
            // Wait for train to have cleared up T320 before switching to full speed.
            // The problem is that B321 activates as train hits diverted leg of turnout.
            after (PA_Data.Delay_Leaving_To_Full_Speed) then {
                PA.forward(PA_Data.Full_Speed)
            }

            // Mid_Station doppler on the way up
            after (PA_Data.Delay_B321_Up_Doppler) then {
                PA_doppler(On)
            } and_after (1.seconds) then {
                PA_doppler(Off)
            }
        }
    }

    val B330_fwd = node(B330) {
        onEnter {
            // Sonora speed reduction
            PA.forward(PA_Data.Sonora_Speed)
            after (PA_Data.Delay_B330_Up_Resume) then {
                PA.forward(PA_Data.Full_Speed)
                PA.horn()
            }
        }
    }

    val B340_fwd = node(B340) {
        onEnter {
            // After tunnel on the way up
            after (PA_Data.Delay_B340_Up_Horn) then {
                PA.horn()
            }
        }
    }

    //--- PA State: AM Summit

    val B360_fwd = node(B360) {
    }

    val B370_end = node(B370) {
        minSecondsOnBlock = PA_Data.Delay_B370_Forward_Stop.seconds
        onEnter {
            after (PA_Data.Delay_B370_Entrance) then {
                // Forward
                PA.forward(PA_Data.Summit_Speed)
                PA.bell(On)
            } and_after (PA_Data.Delay_B370_Forward_Stop) then {
                PA.stop()
                PA.horn()
            } and_after (PA_Data.Delay_B370_Pause_Delay) then {
                // Stopped
                PA.bell(Off)
                PA.horn()
                PA.reverse(PA_Data.Summit_Speed)
            }
        }
    }

    val B360_rev = node(B360) {
        onEnter {
            after (PA_Data.Delay_B360_Full_Reverse) then {
                PA.reverse(PA_Data.Full_Speed)
            }
        }
    }

    val B340_rev = node(B340) {
        minSecondsOnBlock = 8
    }

    val B330_rev = node(B330) {
        onEnter {
            ML_Passenger_Align_Turnouts()
            after (PA_Data.Delay_B330_Down_Speed) then {
                PA.horn()
                PA.reverse(PA_Data.Sonora_Speed)
            }
        }
    }

    val B321_rev = node(B321) {
        maxSecondsOnBlock = 140
        onEnter {
            ML_Passenger_Align_Turnouts()
            PA.reverse(PA_Data.Sonora_Speed)
            // Doppler sound
            //PA_doppler(On)
            //after (1.seconds) then {
            //    PA_doppler(Off)
            //}
            after (PA_Data.Delay_B321_Down_Crossover) then {
                PA.horn()
                PA.bell(On)
                PA.reverse(PA_Data.Crossover_Speed)
            }
        }
    }

    val B504_rev = node(B504) {
        minSecondsOnBlock = 8
        maxSecondsOnBlock = 60
        onEnter {
            PA.horn()
        }
    }

    val B503a_rev = node(B503a) {
        // Time in block fluctuates around 10 seconds.
        minSecondsOnBlock = 3
        maxSecondsOnBlock = 30
        maxSecondsEnterBlock = 10
        onEnter {
            PA.horn()
        }
    }

    val B503b_rev = node(B503b) {
        onEnter {
            after (PA_Data.Delay_B503b_Down_Stop) then {
                PA.stop()
                PA.horn()
                PA.bell(Off)
                PA_beacon(Off)
            } and_after (PA_Data.Delay_Down_Station_Lights_Off) then {
                analytics.gaEvent {
                    category = "Activation"
                    action = "Stop"
                    label = ML_Train.name
                    user = ML_Start_Counter.toString()
                }
                ML_Train = EML_Train.Freight
                ML_Wait_Route.activate()
            }
        }
    }

    sequence = listOf(
        B503b_start, B503a_fwd, B504_fwd, B321_fwd, B330_fwd, B340_fwd, B360_fwd,
        B370_end,
        B360_rev, B340_rev, B330_rev, B321_rev, B504_rev, B503a_rev, B503b_rev)
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
            PA.sound(Off)
            after (FR_Data.Delay_Sound_Started) then {
                FR.horn()
                FR.light(On)
                FR.bell(Off)
                FR.forward(FR_Data.Station_Speed)
                ML_Freight_Align_Turnouts()
                Ambiance_Trigger()
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
                PA.sound(On)
                analytics.gaEvent {
                    category = "Activation"
                    action = "Stop"
                    label = ML_Train.name
                    user = ML_Start_Counter.toString()
                }
                ML_Train = EML_Train.Passenger
                ML_Wait_Route.activate()
            }
        }
    }

    sequence = listOf(B311_start, B321_fwd, B311_rev)
    branches += listOf(B321_fwd, B330_fwd, B321_rev, B311_rev)
}

val SaturdaySetup_Route = ML_Route.sequence {
    // The route sequence to move Freight train into Saturday Mode.
    name = "Saturday"
    throttle = FR
    minSecondsOnBlock = 5
    maxSecondsOnBlock = 120 // 2 minutes per block max
    maxSecondsEnterBlock = 10

    onError {
        // no-op
    }

    onActivate {
        ML_Train = EML_Train.Saturday
        ML_Saturday = EML_Saturday.Setup
    }

    val B311_start = node(B311) {
        minSecondsOnBlock = 0
        onEnter {
            FR.light(On)
            FR.sound(On)
            PA.sound(Off)
            after (FR_Data.Delay_Sound_Started) then {
                FR.horn()
                FR.light(On)
                FR.bell(On)
                FR.forward(FR_Data.Station_Speed)
                ML_Freight_Align_Turnouts()
                T311.normal()
            } and_after (FR_Data.Delay_Up_Station) then {
                FR.forward(FR_Data.Saturday_Speed)
                FR.horn()
            }
        }
    }

    val B321_fwd = node(B321) {
        maxSecondsOnBlock = FR_Data.B321_maxSecondsOnBlock
        onEnter {
            FR.forward(FR_Data.Saturday_Speed)
            after (FR_Data.Delay_Saturday_Into_B321) then {
                FR.horn()
                FR.bell(On)
                // Stop in B321. Normal case is to *not* go into B330.
                FR.stop()
            } and_after (FR_Data.Delay_Saturday_Stop) then {
                // Reverse into storage track
                T311.reverse()
                FR.horn()
                FR.reverse(FR_Data.Reverse_Speed)
            }
        }
    }

    val B504_rev = node(B504) {
        minSecondsOnBlock = 0
        maxSecondsOnBlock = 40
        onEnter {
            FR.horn()
            FR.bell(On)
            FR.reverse(FR_Data.Reverse_Speed)
        }
    }

    val B503a_rev = node(B503a) {
        minSecondsOnBlock = 0
        maxSecondsOnBlock = 0
        onEnter {
            FR.horn()
            FR.bell(On)
            FR.reverse(FR_Data.Reverse_Speed)
        }

        whileOccupied {
            // Stop as soon as B504 is no longer active
            if (!B504.active && FR.reverse) {
                FR.horn()
                FR.bell(Off)
                FR.stop()

                ML_Freight_Align_Turnouts()

                FR.light(Off)
                FR.sound(Off)
                PA.sound(Off)

                ML_Saturday = EML_Saturday.On
                ML_Idle_Route.activate()
            }
        }
    }

    sequence = listOf(B311_start, B321_fwd, B504_rev, B503a_rev)
}



val SaturdayReset_Route = ML_Route.sequence {
    // The route sequence to move Freight train into Saturday Mode.
    name = "Saturday"
    throttle = FR
    minSecondsOnBlock = 0
    maxSecondsOnBlock = 120 // 2 minutes per block max
    maxSecondsEnterBlock = 10

    onError {
        // no-op
    }

    onActivate {
        ML_Saturday = EML_Saturday.Reset
    }

    val B503a_start = node(B503a) {
        minSecondsOnBlock = 1
        onEnter {
            FR.light(On)
            FR.sound(On)
            PA.sound(Off)
            after (FR_Data.Delay_Sound_Started) then {
                FR.horn()
                FR.light(On)
                FR.bell(Off)
                FR.forward(FR_Data.Station_Speed)
            } and_after (FR_Data.Delay_Up_Station) then {
                // Align for storage track
                T311.reverse()
                FR.forward(FR_Data.Saturday_Speed)
                FR.horn()
            }
        }
    }

    val B504_fwd = node(B504) {
        minSecondsOnBlock = 5
        maxSecondsOnBlock = 40
        maxSecondsEnterBlock = 10
        onEnter {
            T311.reverse()
            FR.horn()
            FR.forward(FR_Data.Saturday_Speed)
            FR.bell(On)
        }
    }

    val B321_fwd = node(B321) {
        maxSecondsOnBlock = FR_Data.B321_maxSecondsOnBlock
        onEnter {
            FR.forward(FR_Data.Saturday_Speed)
            after (FR_Data.Delay_Saturday_Into_B321) then {
                FR.horn()
                FR.bell(On)
                // Stop in B321. Normal case is to *not* go into B330.
                FR.stop()
            } and_after (FR_Data.Delay_Saturday_Stop) then {
                // Reverse into normal track
                T311.normal()
                FR.horn()
                FR.reverse(FR_Data.Reverse_Speed)
            }
        }
    }

    val B311_rev = node(B311) {
        onEnter {
            FR.reverse(FR_Data.Reverse_Speed)
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
                PA.sound(Off)
                ML_Train = EML_Train.Passenger
                ML_Saturday = EML_Saturday.Off
                ML_Wait_Route.activate()
            }
        }
    }

    sequence = listOf(B503a_start, B504_fwd, B321_fwd, B311_rev)
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

    val PA_Route = Passenger_Route as ISequenceRoute
    val FR_Route = Freight_Route as ISequenceRoute
    val PA_blocks = PA_Route.sequence.map { it.block }.distinct()
    val FR_blocks = FR_Route.sequence.map { it.block }.distinct()

    // Some of these sensors do not register properly if we trains are not moving.
    //    if (PA_blocks.count { it.active } == 0) { PA.reverse(1.speed) }
    //    if (FR_blocks.count { it.active } == 0) { FR.reverse(1.speed) }
    // This won't work because the block.active state won't change for this engine iteration.
    // TBD use a timer and reschedule the test in 1 second.

    // x_start: whether there's a train in the "start" block (where it should be when starting).
    // Expected value: 1 for a train waiting at the start block.
    val PA_start = PA_Route.isStartBlockActive()
    val FR_start = FR_Route.isStartBlockActive()
    // x_occup: Number of blocks active EXCEPT the first one (counted by x_start)
    // Expected value: 0 for a train waiting at the start block.
    val PA_occup = PA_Route.numNonAdjacentBlocksActive(includeFirstBlock = false)
    val FR_occup = FR_Route.numNonAdjacentBlocksActive(includeFirstBlock = false)
    // x_total: Number of blocks active. Note that PA and FR overlap by 1 block. Includes "start" block.
    // Expected value: 1 for a train waiting at the start block.
    val PA_total = PA_Route.numNonAdjacentBlocksActive(includeFirstBlock = true)
    val FR_total = FR_Route.numNonAdjacentBlocksActive(includeFirstBlock = true)
    // occup vs total:
    // - If the train has stopped somewhere else on the track, we should have start=false,
    //   and occup = total =1.
    // - If the train has stopped on a block boundary, we could have start=false, and
    //   occup = total = 1 (because we count adjacent blocks as a single one).
    // - If there's another train occupying the track, we'll have total > 1.
    //   If the train is on its start block, then start=true, occup > 0 (how many extra blocks occupied).
    //   and total = occup + 1 (extra trains + the one on start block).

    log("[ML Recovery] PA start=$PA_start occup=$PA_occup total=$PA_total $PA_blocks")
    log("[ML Recovery] FR start=$FR_start occup=$FR_occup total=$FR_total $FR_blocks")


    if (_enable_FR && PA_start && (!FR_start || FR_occup == 1)) {
        // PA train is accounted for, where expected.
        // FR train is not at start but there's one FR block occupied, so let's assume it's
        // our train and try to recover that FR train.
        log("[ML Recovery] Recover Freight")
        ML_Recovery_Freight_Route.activate()

    } else if (_enable_PA && FR_start && ((!PA_start && PA_occup == 1) || (PA_start && PA_occup == 0))) {
        // FR train is accounted for, where expected.
        // PA train is not at start but there's 1~2 block occupied, so let's assume it's
        // our train and try to recover that PA train.
        log("[ML Recovery] Recover Passenger")
        ML_Recovery_Passenger_Route.activate()

    } else if (_enable_PA && !_enable_FR && !FR_start && (!PA_start && PA_occup == 1)) {
        // FR train is NOT accounted for, but it is also knowingly disabled.
        // PA train is not at start but there's 1~2 block occupied, so let's assume it's
        // our train and try to recover that PA train.
        // Normally we would not want to recover (in case the freight train is on a dead block
        // in the way), but here we explicitely turned the freight train off, so we allow this.
        log("[ML Recovery] Recover Passenger, Ignore Freight missing and disabled")
        ML_Recovery_Passenger_Route.activate()

    } else if (FR_start && !PA_start && FR_occup == 0) {
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

    } else if (PA_start && !FR_start && PA_occup == 0) {
        // Similar to the previous case with the PA train present with the FR missing.
        // If we can't find the FR train, it may be "dead" on B321 which is shared with
        // the PA train, so we're not even trying to recover from that situation.
        // TBD ==> This test can never be true because it is already matched by the very first
        // test above (PA_start && !FR_Start --> activate ML_Recovery_Freight_Route).
        log("[ML Recovery] Freight not found. Cannot recover.")
        ML_Error_Route.activate()

    } else if (PA_total > 1 || FR_total > 1) {
        // We have more than one block occupied per route. This is not recoverable
        // but that's a case special enough we can ask for the track to be cleared.
        val PA_names = PA_blocks.filter { it.active }
        val FR_names = FR_blocks.filter { it.active }
        log("[ML Recovery] Track occupied (Passenger: $PA_names, Freight: $FR_names)")

        val names = PA_names.plus(FR_names).map { it.name }.distinct()
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

    PA.stop()
    FR.stop()
}

val ML_Recovery_Passenger_Route = ML_Route.sequence {
    // Recovery route for the passenger mainline train.
    name = "Recovery PA"
    throttle = PA
    minSecondsOnBlock = 0       // deactivated
    maxSecondsOnBlock = 120     // 2 minutes per block max
    maxSecondsEnterBlock = 0	// deactivated

    // Whether to monitor B503a when entering B503b.
    var monitor_B503a = false

    fun initSound() {
        PA.bell(On)
        PA_startup_sound()
        PA.sound(On)
        PA.horn()
    }

    val B370_rev = node(B370) {
        onEnter {
            initSound()
            PA.reverse(PA_Data.Summit_Speed)
        }
    }

    val B360_rev = node(B360) {
        onEnter {
            initSound()
            PA.reverse(PA_Data.Summit_Speed)
            after (PA_Data.Delay_B360_Full_Reverse) then {
                PA.reverse(PA_Data.Recover_Speed)
            }
        }
    }

    val B340_rev = node(B340) {
        onEnter {
            initSound()
            PA.reverse(PA_Data.Recover_Speed)
        }
    }

    val B330_rev = node(B330) {
        onEnter {
            ML_Passenger_Align_Turnouts()
            initSound()
            PA.reverse(PA_Data.Sonora_Speed)
        }
    }

    val B321_rev = node(B321) {
        maxSecondsOnBlock = 180
        onEnter {
            ML_Passenger_Align_Turnouts()
            initSound()
            PA.reverse(PA_Data.Sonora_Speed)
            after (PA_Data.Delay_B321_Down_Crossover) then {
                PA.horn()
                PA.bell(On)
                PA.reverse(PA_Data.Crossover_Speed)
            }
        }
    }

    val B504_rev = node(B504) {
        onEnter {
            initSound()
            PA.reverse(PA_Data.Crossover_Speed)
            PA.horn()
        }
    }

    val B503a_rev = node(B503a) {
        onEnter {
            initSound()
            PA.reverse(PA_Data.Crossover_Speed)
        }
    }

    val B503b_rev = node(B503b) {
        onEnter {
            // Note: this is the normal start block and is never
            // a recover block. We do not set any speed on purpose
            // (except to clear B503a below).
            if (!monitor_B503a) {
                log("ML Recovery PA: Enter B503 without monitor B503a")
                after (PA_Data.Delay_B503b_Down_Stop) then {
                    PA.stop()
                    PA.horn()
                    PA.bell(Off)
                } and_after (5.seconds) then {
                    PA.sound(Off)
                } and_after (2.seconds) then {
                    ML_Train = EML_Train.Freight
                    ML_Wait_Route.activate()
                }
            } else {
                log("ML Recovery PA: Enter B503 with monitor B503a")
                PA.stop()
                PA.bell(Off)
                after (5.seconds) then {
                    PA.sound(Off)
                } and_after (2.seconds) then {
                    ML_Train = EML_Train.Freight
                    ML_Wait_Route.activate()
                }
            }
        }

        whileOccupied {
            if (monitor_B503a) {
                if (B503a.active) {
                    PA.reverse(PA_Data.Crossover_Speed)
                } else {
                    // This forces the train to stop prematurely.
                    // Its position will get fixed at the next normal run.
                    PA.stop()
                }
            }
        }
    }

    onActivate {
        ML_State = EML_State.Recover
        ML_Train = EML_Train.Passenger
        ML_Passenger_Align_Turnouts()
        log("ML Recovery PA: Select start node.")
        when {
            B370.active && B360.active ->   route.startNode(B360_rev, trailing=B370_rev)
            B370.active ->                  route.startNode(B370_rev)
            B360.active && B340.active ->   route.startNode(B340_rev, trailing=B360_rev)
            B360.active ->                  route.startNode(B360_rev)
            B340.active && B330.active ->   route.startNode(B330_rev, trailing=B340_rev)
            B340.active ->                  route.startNode(B340_rev)
            B330.active && B321.active ->   route.startNode(B321_rev, trailing=B330_rev)
            B330.active ->                  route.startNode(B330_rev)
            B321.active && B504.active ->   route.startNode(B504_rev, trailing=B321_rev)
            B321.active ->                  route.startNode(B321_rev)
            B504.active && B503a.active ->  route.startNode(B503a_rev, trailing=B504_rev)
            B504.active ->                  route.startNode(B504_rev)
            B503a.active && B503b.active -> {
                monitor_B503a = true
                route.startNode(B503b_rev, trailing=B503a_rev)
            }
            B503a.active -> {
                monitor_B503a = true
                route.startNode(B503a_rev)
            }
            B503b.active ->                 route.startNode(B503b_rev)
            else -> {
               log("ML Recovery PA: WARNING: No condition for start node.")
            }
        }
    }

    onError {
        // We cannot recover from an error during the recover route.
        PA.stop()
        ML_Error_Route.activate()
    }

    sequence = listOf(
        B370_rev, B360_rev, B340_rev, B330_rev, B321_rev, B504_rev, B503a_rev, B503b_rev)
}

val ML_Recovery_Freight_Route = ML_Route.sequence {
    // Recovery route for the freight mainline train.
    name = "Recovery FR"
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
                ML_Train = EML_Train.Passenger
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
               log("ML Recovery FR: WARNING: No condition for start node.")
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


// ----------------------
// Events Branchline (BL)
// ----------------------

/*
Conductor.py Sound: defaults to F8=1 for Mute.
Except F8=0 for Mute for 204, 206, 010; F1=1 for 537.

On the Rapido RDC SP-10 / SantaFe 191:
- F5 is a doppler horn, too long so not using it
- F6 is disabled (function used by ESU PowerPack)
- F7 for dim lights for stations
- F8 is sound (0 for mute, 1 for sound, LokSound)
- F9 for red markers on the "end" side

On the Mopac 153:
- F8 is a mute (1 for silence, 0 for sound)
- F9 is a crossing horn.

On ATSF 804, or SP 3566, or RS3 4070:
- F8 is mute (1 for silence, 0 for sound)
On RS3 4070:
- F5 is the gyro light.

Caboose UP 25520 --> DCC 2552
- Lights on for lights
- Chimney denotes front
- FWD for red rear marker, REV for red front marker.
- F3 on for green rear marker, F4 on for green front marker.
*/

val BL = throttle(191) {
    name = "BL"
    onBell  { on -> throttle.f1(on) }
    onSound { on -> BL_sound(on) }
}

fun BL_gyro(on: Boolean) {
    if (BL_Data.Has_Gyro) {
        BL.f5(on)
    }
}

fun BL_sound(on: Boolean) {
    if (BL_Data.F8_is_Mute) {
        BL.f8(!on)
    } else {
        BL.f8(on)
    }
}

// val CAB = throttle(2552) named "Cab"  // for SP&P engine 4070

enum class EBL_State { Ready, Wait, Run, Recover, Error }

var BL_State = EBL_State.Ready
var BL_Start_Counter = 0

data class _BL_Data(
    val Has_Gyro: Boolean = false,
    /// True: F8=1 engages mute, 0 engages sound. False: F8=1 engages sound, 0 engages mute).
    val F8_is_Mute: Boolean = false,

    val Speed: DccSpeed                 = 6.speed,
    val Speed_Station: DccSpeed         = 4.speed,

    val Delay_Start: Delay              =  8.seconds,
    val Delay_Bell: Delay               =  5.seconds,
    val Delay_UpToSpeed: Delay          = 10.seconds,
    val Delay_Canyon_Horn_Fwd: Delay    = 23.seconds,
    val Delay_Canyon_Horn_Rev: Delay    = 41.seconds,
    val Delay_YouBet_Stop: Delay        =  8.seconds,
    val Delay_YouBet_Pause: Delay       = 25.seconds,
    val Delay_AngelsCamp_Stop: Delay    =  6.seconds,
    val Delay_AngelsCamp_Rev: Delay     =  8.seconds,

    val MinSecondsOnBlock: Int          = 10,
    val MaxSecondsEnterBlock: Int       = 20,

    // 5 minutes wait -- change for debugging
    val Cycle_Wait: Delay               = 5.minutes,

    // whether Angels Camp is the terminus on the way back
    // (train can start from either BLParked or BLAngelsCamp)
    val AngelsCampTerminus: Boolean = false,
    // for emergencies when train is not working
    val Enable_BL: Boolean = true,
)

val BL_Data = if (BL.dccAddress == 10) _BL_Data(
    Has_Gyro = false,
    Speed                   = 8.speed,
    Speed_Station           = 4.speed,
    Delay_Start             = 10.seconds,
    Delay_Canyon_Horn_Fwd   = 22.seconds,
    Delay_Canyon_Horn_Rev   = 36.seconds,
    Delay_YouBet_Stop       = 6.seconds,
    Delay_AngelsCamp_Stop   = 8.seconds,
    MinSecondsOnBlock       = 10,
    MaxSecondsEnterBlock    = 4,
) else if (BL.dccAddress == 191) _BL_Data(
    Has_Gyro = false,
    Speed                   = 8.speed,
    Speed_Station           = 4.speed,
    Delay_Start             = 10.seconds,
    Delay_Canyon_Horn_Fwd   = 22.seconds,
    Delay_Canyon_Horn_Rev   = 36.seconds,
    Delay_YouBet_Stop       = 6.seconds,
    Delay_AngelsCamp_Stop   = 8.seconds,
    MinSecondsOnBlock       = 10,
    MaxSecondsEnterBlock    = 4,
) else _BL_Data()


fun BL_is_Idle_State() = BL_State != EBL_State.Run && BL_State != EBL_State.Recover

fun BL_Send_Start_GaEvent() {
    BL_Start_Counter++
    analytics.gaEvent {
        category = "Activation"
        action = "Start"
        label = "BL"
        user = ML_Start_Counter.toString()
    }
}

fun BL_Send_Stop_GaEvent() {
    analytics.gaEvent {
        category = "Activation"
        action = "Stop"
        label = "BL"
        user = ML_Start_Counter.toString()
    }
}

// Turns branchline engine sound off when automation is turned off

on { BL_is_Idle_State() && BL_Toggle.active } then {
    BL.repeat(2.seconds)
    BL.light(Off)
}

on { BL_is_Idle_State() && !BL_Toggle } then {
    BL.repeat(0.seconds)
    BL.light(Off)
    BL.bell(Off)
    BL.sound(Off)
    BL_gyro(Off)
}

// Send GA activation/toggle state events

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
    dazzEvent {
        key = "toggle/branchline"
        state = true
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
    dazzEvent {
        key = "toggle/branchline"
        state = false
    }
}

// --- BL Static State

// #4070 Gyro light on when moving.
// Caboose lights are controlled by the engine's direction.
on { BL.stopped } then {
    BL_gyro(Off)
    // CAB.light(On)
    // CAB.f3(Off)
    // CAB.f4(Off)
}
on { BL.forward } then {
    BL_gyro(On)
    // CAB.light(On)
    // CAB.forward(1.speed)
    // CAB.f3(Off)
    // CAB.f4(On)
}
on { BL.reverse } then {
    BL_gyro(On)
    // CAB.light(On)
    // CAB.reverse(1.speed)
    // CAB.f3(On)
    // CAB.f4(Off)
}

val BL_Route = routes {
    name = "Branchline"
    toggle = BL_Toggle
    status = { BL_State.toString() }

    onError {
        // --- BL State: Error
        // The current route will trigger the corresponding BL_Recover_Route.
        BL.stop()
        BL.sound(Off)
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
    // The idle route is where we check whether a Branchline train should start.
    name = "Ready"

    onActivate {
        BL_State = EBL_State.Ready
    }

    onIdle {
        if (BL_Data.Enable_BL && BL_Toggle.active && AIU_Motion.active) {
            BL_Shuttle_Route.activate()
        }
    }
}

val BL_Wait_Route = BL_Route.idle {
    // The wait route creates the pause between two branchline train runs.
    name = "Wait"


    onActivate {
        BL_State = EBL_State.Wait

        after (BL_Data.Cycle_Wait) then {
            BL_Idle_Route.activate()
        }
    }
}

val BL_Error_Route = BL_Route.idle {
    // The error route is used when we cannot recover from an error during the recover route.
    name = "Error"

    onActivate {
        BL_State = EBL_State.Error
        BL.stop()
    }
}

val BL_Shuttle_Route = BL_Route.sequence {
    // The normal "shuttle sequence" for the branchline train.
    name = "Shuttle"
    throttle = BL
    minSecondsOnBlock = BL_Data.MinSecondsOnBlock
    maxSecondsOnBlock = 120   // 2 minutes per block max
    maxSecondsEnterBlock = BL_Data.MaxSecondsEnterBlock

    var BLStartDone = false
    fun doStart() {
        BLStartDone = true
        BL.sound(On)
        BL.horn()
        BL.bell(On)
        after (BL_Data.Delay_Start) then {
            BL.bell(Off)
            BL.horn()
            BL.light(On)
            BL.forward(BL_Data.Speed_Station)
        } and_after (BL_Data.Delay_UpToSpeed) then {
            BL.forward(BL_Data.Speed)
        }
    }

    val B801_Parked_fwd = node(B801) {
        // Typical run time: 35 seconds.
        onEnter {
            doStart()
        }
    }

    val B821_AngelsCamp_fwd = node(B821) {
        // Typical run time: 45 seconds.
        minSecondsOnBlock = if (BLStartDone) BL_Data.MinSecondsOnBlock else 1
        onEnter {
            if (BLStartDone) {
                BL.bell(Off)
                after (BL_Data.Delay_UpToSpeed) then {
                    BL.forward(BL_Data.Speed)
                }
            } else {
                doStart()
            }
        }
    }

    val B830_Canyon_fwd = node(B830) {
        // Typical run time: 70 seconds.
        maxSecondsEnterBlock = 10
        onEnter {
            BL.bell(Off)
            BL.horn()
            BL.forward(BL_Data.Speed)

            // Horn over Canyon bridge
            after (BL_Data.Delay_Canyon_Horn_Fwd) then {
                BL.horn()
            }
        }
    }

    val B850_Tunnel_fwd = node(B850) {
        // Typical run time: 25 seconds.
        minSecondsOnBlock = 5
        maxSecondsOnBlock = 40
        maxSecondsEnterBlock = 10
        onEnter {
            BL.horn()
        }
    }

    val B860_YouBet_fwd = node(B860) {
        // Typical run time: 60 seconds.
        onEnter {
            BL.horn()
            BL.bell(On)
            after (BL_Data.Delay_YouBet_Stop) then {
                // Toggle Stop/Reverse/Stop to turn off the Reverse front light on RDC 10.
                // Next event should still be the BLYouBet Stopped one.
                BL.stop()
                BL.horn()
                BL.reverse(1.speed)
                BL.stop()
            } and_after (BL_Data.Delay_Bell) then {
                BL.bell(Off)
            } and_after (BL_Data.Delay_YouBet_Pause) then {
                BL.bell(On)
                BL.horn()
            } and_after (5.seconds) then {
                BL.reverse(BL_Data.Speed)
            } and_after (BL_Data.Delay_Bell) then {
                BL.light(On)
                BL.bell(Off)
                BL.sound(On)
                BL.horn()
            }
        }
    }

    val B850_Tunnel_rev = node(B850) {
        // Typical run time: 25 seconds.
        onEnter {
            BL.horn()
        }
    }

    val B830_Canyon_rev = node(B830) {
        // Typical run time: 75 seconds.
        onEnter {
            BL.horn()
            after (BL_Data.Delay_Canyon_Horn_Rev) then {
                BL.horn()
            }
        }
    }

    fun sequenceTurnOff() {
        after (10.seconds) then {
            BL.stop()
        } and_after (3.seconds) then {
            BL.bell(Off)
        } and_after (5.seconds) then {
            BL.light(Off)
            BL.sound(Off)
            BL_Send_Stop_GaEvent()
            BL_Wait_Route.activate()
        }
    }

    val B821_AngelsCamp_rev = node(B821) {
        // Typical run time: 65 seconds (85 if terminus).
        onEnter {
            BL.bell(On)
            T324.normal()
            BL.reverse(BL_Data.Speed_Station)
            after (BL_Data.Delay_AngelsCamp_Stop) then {
                BL.stop()
                BL.horn()
            } and_after (BL_Data.Delay_AngelsCamp_Rev) then {
                if (BL_Data.AngelsCampTerminus) {
                    sequenceTurnOff()
                } else {
                    BL.horn()
                    BL.reverse(BL_Data.Speed_Station)
                }
            }
        }
    }

    val B801_Parked_rev = node(B801) {
        // Typical run time: 20 seconds.
        onEnter {
            sequenceTurnOff()
        }
    }

    onActivate {
        BL_State = EBL_State.Run
        BLStartDone = false

        when {
            B821.active && B801.active ->   route.startNode(B821_AngelsCamp_fwd, trailing=B801_Parked_fwd)
            B821.active ->                  route.startNode(B821_AngelsCamp_fwd)
            B801.active ->                  route.startNode(B801_Parked_fwd)
        }

        BL_Send_Start_GaEvent()
        jsonEvent {
            key1 = "Depart"
            key2 = "Branchline"
        }

        throttle.incActivationCount()
    }

    onError {
        BL_Recovery_Route.activate()
    }

    sequence = listOf(B801_Parked_fwd, B821_AngelsCamp_fwd, B830_Canyon_fwd, B850_Tunnel_fwd,
        B860_YouBet_fwd,
        B850_Tunnel_rev, B830_Canyon_rev, B821_AngelsCamp_rev, B801_Parked_rev)
}

val BL_Recovery_Route = BL_Route.sequence {
    // Recovery mechanism for the branchline train.
    name = "Recovery"
    throttle = BL
    minSecondsOnBlock = 0       // deactivated
    maxSecondsOnBlock = 120     // 2 minutes per block max
    maxSecondsEnterBlock = 0

    fun move() {
        BL.bell(On)
        BL.sound(On)
        BL.horn()
        BL.reverse(BL_Data.Speed_Station)
    }

    val B860_YouBet_rev = node(B860) {
        onEnter {
            move()
        }
    }

    val B850_Tunnel_rev = node(B850) {
        onEnter {
            move()
        }
    }

    val B830_Canyon_rev = node(B830) {
        onEnter {
            move()
        }
    }

    val B821_AngelsCamp_rev = node(B821) {
        onEnter {
            move()
        }

        whileOccupied {
            if (BL_Data.AngelsCampTerminus && !B830.active) {
                BL.stop()
                BL.bell(Off)
                BL.sound(Off)
                BL_Wait_Route.activate()
            }
        }
    }

    val B801_Parked_rev = node(B801) {
        onEnter {
            move()
        }

        whileOccupied {
            if (!B821.active) {
                BL.stop()
                BL.bell(Off)
                BL.sound(Off)
                BL_Wait_Route.activate()
            }
        }
    }

    onActivate {
        BL_State = EBL_State.Recover
        when {
            B860.active && B850.active ->   route.startNode(B850_Tunnel_rev, trailing=B860_YouBet_rev)
            B860.active ->                  route.startNode(B860_YouBet_rev)
            B850.active && B830.active ->   route.startNode(B830_Canyon_rev, trailing=B850_Tunnel_rev)
            B850.active ->                  route.startNode(B850_Tunnel_rev)
            B830.active && B821.active ->   route.startNode(B821_AngelsCamp_rev, trailing=B830_Canyon_rev)
            B830.active ->                  route.startNode(B830_Canyon_rev)
            B821.active && B801.active ->   route.startNode(B801_Parked_rev, trailing=B821_AngelsCamp_rev)
            B821.active ->                  route.startNode(B821_AngelsCamp_rev)
            B801.active ->                  route.startNode(B801_Parked_rev)
            else -> {
                // None of the sensors are active. We don't know where the train is located.
                BL_Recovery2_Route.activate()
            }
        }
    }

    onError {
        // We cannot recover from an error during the recover route.
        BL.stop()
        BL_Error_Route.activate()
    }

    sequence = listOf(B860_YouBet_rev, B850_Tunnel_rev, B830_Canyon_rev, B821_AngelsCamp_rev, B801_Parked_rev)
}

val BL_Recovery2_Route = BL_Route.idle {
    // Recovery mechanism for the branchline train when the engine cannot be located in
    // any of the physical blocks.
    name = "Recovery2"

    val timeout = timer(5.minutes)

    onActivate {
        BL_State = EBL_State.Recover
        BL.bell(On)
        BL.sound(On)
        BL.horn()
        BL.reverse(BL_Data.Speed_Station)
        timeout.start()
    }

    onIdle {
        if (   ( BL_Data.AngelsCampTerminus && B821.active && !B830.active)
            || (!BL_Data.AngelsCampTerminus && B801.active && !B821.active)) {
            BL.stop()
            BL.bell(Off)
            BL.sound(Off)
            timeout.reset()
            // We have recovered
            BL_Wait_Route.activate()
        } else if (timeout.active) {
            BL.stop()
            BL.bell(Off)
            BL.sound(Off)
            timeout.reset()
            // We cannot recover
            BL_Error_Route.activate()
        }
    }
}


// --------------------
// Events Trolley Line (TL)
// --------------------

// Trolley: Orange = 6885 -- Yellow = 6119 -- Thomas = 0001
val TL = throttle(6885) {
    name = "TL"
    // Disable F1 and F5 on Thomas
    onLight { on -> throttle.f0(on)
                    /* throttle.f5(on) */ }
    onBell  { on -> /* throttle.f1(on) */ }
    onSound { on -> throttle.f8(on) }
    // no horn
}

enum class ETL_State { Ready, Wait, Run, Recover, Error }

var TL_State = ETL_State.Ready
var TL_Start_Counter = 0

// SDB sensor block timings:
// 6885: block0 = 0..200, block1: = 300..2000
// 6119: block0 = 0..400, block1: = 380..2000
// 0001: block0 = 0..400, block1: = 400..2000

data class _TL_Data(
    // Defaults for Orange Trolley 6885
    val Speed: DccSpeed = 4.speed,
    val RevSpeed: DccSpeed = 2.speed,

    val Delay_Start: Delay      = 5.seconds,
    val Delay_Start2: Delay     = 1.seconds,
    /** How long to go Forward on block B. */
    val Delay_Forward: Delay    = 43.seconds,
    /** Pause before reversing. */
    val Delay_RevPause: Delay   = 5.seconds,
    /** Stop time at the end before the kludge. */
    val Delay_EndStop: Delay    = 3.seconds,
    /** The "rev-stop-fwd-stop" kludge timing that (mostly) stops 6885 properly. */
    val Delay_EndKludge: Delay  = 3.seconds,
    /** Time before turning off all lights & sounds. */
    val Delay_Off: Delay        = 5.seconds,

    val Cycle_Wait: Delay       = 30.seconds,

    val Delay_Recovery: Delay   = 10.minutes,

    /** For emergencies when train is not working. */
    val Enable_TL: Boolean = false // Trolley automation currently disabled
)

val TL_Data = if (TL.dccAddress == 6119) _TL_Data(
    // Values for Yellow Trolley 6119
    Speed = 12.speed,
    RevSpeed = 8.speed,

    Delay_Start     = 5.seconds,
    Delay_Start2    = 1.seconds,
    Delay_Forward   = 14.seconds,
    Delay_RevPause  = 5.seconds,
    Delay_EndStop   = 1.seconds,
    Delay_EndKludge = 1.seconds,
    Delay_Off       = 5.seconds,

    Cycle_Wait      = 2.minutes,
) else if (TL.dccAddress == 1) _TL_Data(
    // Values for Thomas 1
    Speed = 12.speed,
    RevSpeed = 10.speed,

    Delay_Start     = 3.seconds,
    Delay_Start2    = 1.seconds,
    Delay_Forward   = 19.seconds,
    Delay_RevPause  = 5.seconds,
    Delay_EndStop   = 1.seconds,
    Delay_EndKludge = 1.seconds,
    Delay_Off       = 5.seconds,

    Cycle_Wait      = 5.minutes,
) else _TL_Data()

fun TL_is_Idle_State() = TL_State != ETL_State.Run && TL_State != ETL_State.Recover

fun TL_Send_Start_GaEvent() {
    TL_Start_Counter++
    analytics.gaEvent {
        category = "Activation"
        action = "Start"
        label = "TL"
        user = ML_Start_Counter.toString()
    }
}

fun TL_Send_Stop_GaEvent() {
    analytics.gaEvent {
        category = "Activation"
        action = "Stop"
        label = "TL"
        user = ML_Start_Counter.toString()
    }
}

val TL_Route = routes {
    name = "Trolley"
    toggle = BL_Toggle      // Trolley is active when Branchline is active.
    status = { TL_State.toString() }

    onError {
        // --- TL State: Error
        // The current route will trigger the corresponding TL_Recover_Route.
        TL.stop()
        TL.sound(Off)
        analytics.gaEvent {
            category = "Automation"
            action = "Error"
            label = "Trolley"
            user = "Staff"
        }
    }
}

var TL_Idle_Route: IRoute
TL_Idle_Route = TL_Route.idle {
    // The idle route is where we check whether a Branchline train should start.
    name = "Ready"

    onActivate {
        TL_State = ETL_State.Ready
        TL.light(Off)
        TL.sound(Off)
    }

    onIdle {
        if (TL_Data.Enable_TL && route.owner.toggle.active && AIU_Motion.active) {
            TL_Shuttle_Route.activate()
        }
    }
}

val TL_Wait_Route = TL_Route.idle {
    // The wait route creates the pause between two trolley runs.
    name = "Wait"

    onActivate {
        TL_State = ETL_State.Wait

        after (TL_Data.Cycle_Wait) then {
            TL_Idle_Route.activate()
        }
    }
}

val TL_Error_Route = TL_Route.idle {
    // The error route is used when we cannot recover from an error during the recover route.
    name = "Error"

    onActivate {
        TL_State = ETL_State.Error
        TL.stop()
    }
}

val TL_Shuttle_Route = TL_Route.sequence {
    // The normal "shuttle sequence" for the trolley.
    name = "Trolley"
    throttle = TL
    minSecondsOnBlock = 10
    maxSecondsOnBlock = 120 // 2 minutes per Block max
    maxSecondsEnterBlock = 20

    val B713a_fwd = node(B713a) {
        minSecondsOnBlock = 2
        onEnter {
            TL.light(On)
            TL.sound(On)
            after (TL_Data.Delay_Start) then {
                TL.reverse(1.speed)
            } and_after (TL_Data.Delay_Start2) then {
                TL.stop()
            } and_after (TL_Data.Delay_Start2) then {
                TL.forward(TL_Data.Speed)
            }
        }
    }

    val B713b_fwd = node(B713b) {
        minSecondsOnBlock = 10
        onEnter {
            TL.sound(On)
            TL.bell(On)
            after (TL_Data.Delay_Forward) then {
                TL.bell(Off)
                TL.stop()
            } and_after (TL_Data.Delay_RevPause) then {
                TL.bell(Off)
                TL.reverse(TL_Data.RevSpeed)
            }
        }
    }

    val B713a_rev = node(B713a) {
        onEnter {
            TL.bell(On)
            // Note: 6885 needed a short forward(2.speed) kludge here. Still need it?
            TL.reverse(TL_Data.RevSpeed)
            after (TL_Data.Delay_EndStop) then {
                TL.bell(Off)
                TL.stop()
                // Engine 6885 will not stop in reverse.
                // The kludge to force 6885 to stop is to change it to go forward then stop
                // after a very small delay.
                TL.forward(2.speed)
            } and_after (TL_Data.Delay_EndKludge) then {
                TL.stop()
            } and_after (TL_Data.Delay_Off) then {
                TL.sound(Off)
                TL.light(Off)
                TL_Send_Stop_GaEvent()
                TL_Wait_Route.activate()
            }
        }
    }

    onActivate {
        TL_State = ETL_State.Run

        TL_Send_Start_GaEvent()
        jsonEvent {
            key1 = "Depart"
            key2 = "Trolley"
        }

        throttle.incActivationCount()
    }

    onError {
        TL_Recovery_Route.activate()
    }

    sequence = listOf(B713a_fwd, B713b_fwd, B713a_rev)
}

val TL_Recovery_Route = TL_Route.sequence {
    // Recovery mechanism for the trolley.
    name = "Recovery"
    throttle = TL
    minSecondsOnBlock = 0       // deactivated
    maxSecondsOnBlock = 120     // 2 minutes per block max
    maxSecondsEnterBlock = 2

    val B713b_rev = node(B713b) {
        minSecondsOnBlock = 0       // deactivated
        onEnter {
            TL.sound(On)
            TL.horn()
            TL.reverse(TL_Data.RevSpeed)
        }
    }

    val B713a_rev = node(B713a) {
        maxSecondsEnterBlock = 2
        onEnter {
            TL.horn()
            // Engine 6885 will not stop in reverse.
            // Note: 6885 needed a short forward(2.speed) kludge here. Still need it?
            TL.reverse(TL_Data.RevSpeed)
            after (TL_Data.Delay_EndStop) then {
                TL.bell(Off)
                TL.stop()
                // Engine 6885 will not stop in reverse.
                // The kludge to force 6885 to stop is to change it to go forward then stop
                // after a very small delay.
                TL.forward(2.speed)
            } and_after (TL_Data.Delay_EndKludge) then {
                TL.stop()
            } and_after (TL_Data.Delay_Off) then {
                TL.sound(Off)
                TL.light(Off)
                TL_Idle_Route.activate()
            }
        }
    }

    onActivate {
        TL_State = ETL_State.Recover
        when {
            B713b.active && B713a.active -> route.startNode(B713a_rev, trailing = B713b_rev)
            B713b.active -> route.startNode(B713b_rev)
            B713a.active -> route.startNode(B713a_rev)
            else -> {
                // SDB is setup such that B713b is active if the train is not found *elsewhere*.
                // The only reason B713b may be inactive is if SDB cannot communicate with JMRI.
                log("Recovery TL: WARNING: Engine not detected.")
            }
        }
    }

    onError {
        TL.stop()

        // We cannot recover from an error during the recover route.
        // Previous behavior was to abandon by calling:
        //     TL_Error_Route.activate()
        // New behavior is to wait for a long time (e.g. 1 hour) then retry recovery.
        after (TL_Data.Delay_Recovery) then {
            TL_Idle_Route.activate()
        }
    }

    sequence = listOf(B713b_rev, B713a_rev)

}


// ------------------
// Automatic Turnouts
// ------------------

// If automation is off, T330 is automatically selected:
// B320 -> B330 via T330 Normal
// B321 -> B330 via T330 Reverse

on { ML_State.isIdle() && ML_Saturday.isIdle() && !ML_Toggle && !B330 &&  B320.active && !B321 } then { T330.normal() }
on { ML_State.isIdle() && ML_Saturday.isIdle() && !ML_Toggle && !B330 && !B320 &&  B321.active } then { T330.reverse() }


// --------------
// Distant Signal
// --------------

// The T330 LED is GREEN when the junction B320 --> B330 via T330 Normal is ready.

on {  T330.normal } then {
    mqtt.publish("turnout/t330/script/event", "Brightness 0.75; Fill #00FF00 1; Trigger")
    DistantSignal_UpdateT330AndBlocks()
}

on { !T330.normal } then {
    mqtt.publish("turnout/t330/script/event", "Brightness 0.75; Fill #FF0000 1; Trigger")
    DistantSignal_UpdateT330AndBlocks()
}

listOf(B320, B321, B330).forEach { b ->
    on {  b.active } then {
        DistantSignal_UpdateT330AndBlocks()
    }

    on { !b.active } then {
        DistantSignal_UpdateT330AndBlocks()
    }
}

fun DistantSignal_UpdateT330AndBlocks() {
    // Turnout state name must match the JSON keys used in DistantSignal "states" dictionary.
    mqtt.publish("turnout/t330/state",  if (T330.normal) "normal" else "reverse")
    // Blocks and state names must match the JSON keys used in DistantSignal "blocks" dictionary.
    mqtt.publish("block/b320/state",    if (B320.active) "active" else "inactive")
    mqtt.publish("block/b321/state",    if (B321.active) "active" else "inactive")
    mqtt.publish("block/b330/state",    if (B330.active) "active" else "inactive")
}

// ---------
