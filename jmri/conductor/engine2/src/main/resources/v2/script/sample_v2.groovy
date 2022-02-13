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
