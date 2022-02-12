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

// Maps

map {
    name = "Mainline"
    svg  = "Map 1.svg"
}

// Rules: Conditions -> Actions

on { false } then { MyIntVar = 0 }
on { true } then { MyIntVar = 1 }

on { Toggle } then {
    MyBooleanVar = true
    MyLongVar += 1
}

on { !Toggle } then {
    MyBooleanVar = true
    MyLongVar += 1
}
