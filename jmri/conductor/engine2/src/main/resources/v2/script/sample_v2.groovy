//file:noinspection GrMethodMayBeStatic
package v2.script


import com.alflabs.conductor.v2.script.RootScript
import groovy.transform.BaseScript

@BaseScript RootScript baseScript


// Variables
// Variables that must be seen/exported by the ExecEngine cannot be declared with a def or
// type keyword. They must be specified without "def", directly. Type is inferred.
// Local variables can be used but they are "invisible" to the script engine.

def LocalVar1 = "This variable is never seen by the ExecEngine"
String LocalVar2 = "Neither is this one"
int LocalVar3 = 42 // we can't use this either

MyStringVar = "This string is exported. Value is " + LocalVar3
MyIntVar = 42 + LocalVar3
MyLongVar = 43L
MyLongVar++
MyBooleanVar = false

def On = true
def Off = false

// Sensors

B310        = block  "NS768"      // 49:1
B311        = block  "NS769"      // 49:2

Toggle      = sensor "NS829"      // 52:14


// Turnouts

T311        = turnout "NT311"
T312        = turnout "NT312"

// Timers

MyTimer1    = timer 5
MyTimer2    = timer 15

// Throttles

Train1      = throttle 1001
Train2      = throttle 2001

// Maps

map {
    name = "Mainline"
    svg  = "Map 1.svg"
}

// Functions

def AlignTurnouts() {
    // Syntax warning: 'T311.reverse' is a property getter, which does
    // not exist, whereas 'T311.reverse()' is a function call.
    T311.reverse()
    T312.reverse()
}

def ResetTurnouts() {
    T311.normal()
    T312.normal()
}

// Rules: Conditions -> Actions

on { false } then { MyIntVar = 0 }
on { true } then { MyIntVar = 1 }

on { Toggle } then {
    MyBooleanVar = true
    MyLongVar += 1
    MyTimer1.start()
    AlignTurnouts()
}

on { !Toggle } then {
    MyBooleanVar = true
    MyLongVar += 1
    MyTimer1.reset()
    ResetTurnouts()
}

on { MyTimer1 } then {
    Train1.horn()
    Train1.light(On)
    Train1.forward(5)
    Train2.stop()
}

on { B310 } then {
    Train1.stop()
    Train1.horn()
    Train1.light(Off)
    Train2.reverse(10)
}

on { B311 } then {
    Train1.stop()
    Train2.stop()
}

Route_Idle = route idle()

def _leaving_speed = 5
def _mainline_speed = 10
def _reverse_speed = 8

Route1 = route sequence {
    throttle = Train1
    timeout = 60

    onActivate {
        Train1.light(true)
        Train1.horn()
        Train1.forward(_leaving_speed)
    }

    // Nodes must be declared before the node graph.
    // Nodes can be global or local variables.

    def B310_fwd = node(B310) {
        onEnter {
            Train1.forward(_leaving_speed)
        }
        whileOccupied {
            Train1.light(true)
        }
        onTrailing {
            Train1.forward(_mainline_speed)
        }
        onEmpty {
            Train1.light(false)
        }
    }

    B311_fwd = node(B311) {
        onEnter {
            Train1.horn()
        }
    }

    def B310_rev = node(B310) {
        onEnter {
            Train1.horn()
        }
    }

    nodes = [ [ B310_fwd, B311_fwd, B310_rev ],
              [ B310_fwd, B310_rev ] ]
}

PA_Route = activeRoute {
    routes = [ Route_Idle, Route1 ]
}
