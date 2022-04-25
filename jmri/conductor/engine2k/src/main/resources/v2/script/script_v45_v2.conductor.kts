@file:Suppress("FunctionName", "LocalVariableName", "PropertyName")

import com.alfray.conductor.v2.script.speed


// Variables and local declaration.

val On = true
val Off = false

// sensors

val BLParked     = block ("NS752")     // 48:1
val BLStation    = block ("NS753")     // 48:2
val BLTunnel     = block ("NS754")     // 48:3
val BLReverse    = block ("NS755")     // 48:4
val BLRun        = block ("NS765")     // 48:14, Disconnected BL activation button

val B310         = block ("NS768")     // 49:1
val B311         = block ("NS769")     // 49:2
val B320         = block ("NS770")     // 49:3
val B321         = block ("NS771")     // 49:4
val B330         = block ("NS773")     // 49:6
val B340         = block ("NS774")     // 49:7
val B360         = block ("NS775")     // 49:8
val B370         = block ("NS776")     // 49:9

val B503a        = block ("NS786")     // 50:3
val B503b        = block ("NS787")     // 50:4
val AIU_Motion   = sensor("NS797")     // 50:14

val BL_Toggle    = sensor("NS828")     // 52:13
val PA_Toggle    = sensor("NS829")     // 52:14


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
    svg  = "Conductor Map Mainline 1.svg"
}

// JSON tracking

JSON_URL = "@~/bin/JMRI/rtac_json_url.txt"

// GA Tracking

GA_Tracking_Id = "@~/bin/JMRI/rtac_ga_tracking_id.txt"
GA_URL = "http://consist.alfray.com/train/"

// -----------------
// Motion
// -----------------

var AIU_Motion_Counter = 0

on { AIU_Motion } then {
    RTAC_Motion = On
    AIU_Motion_Counter += 1
    ga_event {
        category = "Motion"
        action = "Start"
        label = "AIU"
        user = "AIU-Motion-Counter"
    }
}

on { !AIU_Motion } then {
    RTAC_Motion = Off
    ga_event {
        category = "Motion"
        action = "Stop"
        label = "AIU"
        user = "AIU-Motion-Counter"
    }
}

// ---------
// End of the Day
// ---------

val End_Of_Day_HHMM = 1650

on { PA_Toggle.active && Conductor_Time == End_Of_Day_HHMM } then {
    PA_Toggle.active(Off)
}

on { BL_Toggle.active && Conductor_Time == End_Of_Day_HHMM } then {
    BL_Toggle.active(Off)
}

on { PA_Toggle } then {
    RTAC_PSA_Text = "Automation Started"
}

on { !PA_Toggle && Conductor_Time == End_Of_Day_HHMM } then {
    RTAC_PSA_Text = "{c:red}Automation Turned Off\nat 4:50 PM"
}

on { !PA_Toggle && Conductor_Time != End_Of_Day_HHMM } then {
    RTAC_PSA_Text = "{c:red}Automation Stopped"
}

// ---------
// Events PA
// ---------

enum class PA_State {
    Idle, Station, Shuttle, Manual, Error, Wait,
}
enum class PA_Train { Passenger, Freight, }
val PA_Start_Counter = 0

val AM = throttle(8749)     // Full Amtrak route
val SP = throttle(1072)     // "Short Passenger" (now Freight) on limited Amtrak route

fun PA_Fn_Release_Route() {
    T311.normal()
    T320.normal()
    T321.normal()
}

val PA_Idle_Route = route.idle()

val Passenger_Route = route.sequence {
    throttle = AM
    timeout = 60 // 1 minute

    val AM_Leaving_Speed    = 6.speed
    val AM_Station_Speed    = 12.speed
    val AM_Summit_Speed     = 6.speed
    val AM_Summit_Bridge_Speed = 4.speed
    val AM_Sonora_Speed     = 8.speed
    val AM_Crossover_Speed  = 4.speed
    val AM_Full_Speed       = 12.speed

    val AM_Delayed_Horn = 2
    val AM_Leaving_To_Full_Speed = 15
    val AM_Timer_B321_Up_Doppler = 27
    val AM_Timer_B330_Up_Resume = 12
    val AM_Timer_B340_Up_Horn = 5
    val AM_Timer_B370_Forward_Stop = 17  // time running at AM_Summit_Speed before stopping
    val AM_Timer_B370_Pause_Delay  = 16
    val AM_Timer_B360_Full_Reverse = 12
    val AM_Timer_B330_Down_Speed = 8
    val AM_Timer_B321_Down_Crossover = 27
    val AM_Timer_B503b_Down_Stop = 20
    val AM_Timer_Down_Station_Lights_Off = 10

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

    fun onActivate() {
        RTAC_PSA_Text = "{c:blue}Currently Running:\n\nPassenger"
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
            on { AM.stopped } then {
                AM.forward(AM_Leaving_Speed)
                AM.f1(Off)
                SP.sound(Off)
                AM.horn()
                AM_Fn_Acquire_Route()
                AM.f1(On)
                after(timer(AM_Delayed_Horn)) then {
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
            after(timer(AM_Leaving_To_Full_Speed)) then {
                AM.forward(AM_Full_Speed)
            }

            // Mid_Station doppler on the way up
            after(timer(AM_Timer_B321_Up_Doppler)) then {
                AM.f9(On)
                AM.f9(Off)
            }
        }

        whileOccupied {
            // PA_Toggle off reverts train to down but only on specific "safe" blocks
            // (e.g. not in B503a/B503b because stop distances puts us in the next block)
            on { !PA_Toggle } then {
                AM.reverse(AM_Summit_Speed)
            }
        }
    }

    val B330_fwd = node(B330) {
        onEnter {
            // Sonora speed reduction
            AM.forward(AM_Sonora_Speed)
            after(timer(AM_Timer_B330_Up_Resume)) then {
                AM.forward(AM_Full_Speed)
                AM.horn()
            }
        }

        whileOccupied {
            // PA_Toggle off reverts train to down but only on specific "safe" blocks
            // (e.g. not in B503a/B503b because stop distances puts us in the next block)
            on { !PA_Toggle } then {
                AM.reverse(AM_Summit_Speed)
            }
        }
    }

    val B340_fwd = node(B340) {
        onEnter {
            // After tunnel on the way up
            after(timer(AM_Timer_B340_Up_Horn)) then {
                AM.horn()
            }
        }

        whileOccupied {
            // PA_Toggle off reverts train to down but only on specific "safe" blocks
            // (e.g. not in B503a/B503b because stop distances puts us in the next block)
            on { !PA_Toggle } then {
                AM.reverse(AM_Summit_Speed)
            }
        }
    }

    //--- PA State: AM Summit

    val B360_fwd = node(B360) {
    }

    val B370_end = node(B370) {
        onEnter {
            after(timer(4)) then {
                // Forward
                AM.forward(AM_Summit_Speed)
                AM.f1(On)
            } and_after(timer(AM_Timer_B370_Forward_Stop)) then {
                AM.stop()
                AM.horn()
            } and_after(timer(AM_Timer_B370_Pause_Delay)) then {
                // Stopped
                AM.f1(Off)
                AM.horn()
                AM.reverse(AM_Summit_Speed)
            }
        }
    }

    val B360_rev = node(B360) {
        onEnter {
            after(timer(AM_Timer_B360_Full_Reverse)) then {
                AM.reverse(AM_Full_Speed)
            }
        }
    }

    val B340_rev = node(B340) {
    }

    val B330_rev = node(B330) {
    }

    val B321_rev = node(B321) {
    }

    val B503a_rev = node(B503a) {
    }

    val B503b_rev = node(B503b) {
    }


//
//    Enter B330 Reverse _> {
//    After AM_Timer_B330_Down_Speed _> {
//    AM Horn ;
//    AM Reverse = AM_Sonora_Speed
//}
//}
//
//    Enter B321 Reverse _> {
//    AM Reverse = AM_Full_Speed ;
//    AM F9 = 1 ; AM F9 = 0 ;
//    After AM_Timer_B321_Down_Crossover _> {
//    AM Horn ;
//    AM F1 = 1 ;
//    AM Reverse = AM_Crossover_Speed
//}
//}
//
//    Enter B503a Reverse _> {
//    After AM_Timer_B503b_Down_Stop _> {
//    AM Stop ;
//    AM Horn ;
//    AM F1 = 0;
//    AM_Timer_Down_Station_Lights_Off Start ;
//}
//}
//
//    Block B503a Stopped _> {
//    AM_Timer_Down_Station_Lights_Off _> {
//    GA_Event Category: Activation, Action: Stop, Label: PA_Train, User: PA_Start_Counter ;
//    PA_Train = Freight ;
//    PA_State = Wait ;
//    PA_Route Activate = PA_Idle_Route
//}
//}
//
//    blocks = B503b, B503a, B321, B330, B340, B360, B370, B360, B340, B330, B321, B503a, B503b
}



