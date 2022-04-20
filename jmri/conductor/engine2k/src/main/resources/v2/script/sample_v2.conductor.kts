
// Variables and local declaration.

val LocalVar1 = "This variable is never seen by the ExecEngine"
val LocalVar2 : String = "Neither is this one"
val LocalVar3 = 42 // we can't use this either

val MyStringVar = "This string is exported. Value is " + LocalVar3
var MyIntVar = 42 + LocalVar3
var MyLongVar = 43L
MyLongVar++
var MyBooleanVar = false

val On = true
val Off = false

// Sensors

val B310 = block("NS768")      // 49:1
val B311 = block("NS769")      // 49:2

val Toggle = sensor("NS829")   // 52:14

println("Toggle active = ${Toggle.active}")

// Turnouts

val T311 = turnout("NT311")
val T312 = turnout("NT312")

// Timers

val MyTimer1 = timer(5)
val MyTimer2 = timer(15)

// Throttles

val Train1 = throttle(1001)
val Train2 = throttle(2001)

// Maps

map {
    name = "Mainline"
    svg  = "Map 1.svg"
}

// Functions

fun AlignTurnouts() {
    T311.reverse()
    T312.reverse()
}

fun ResetTurnouts() {
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

// How to apply this syntax in kts:  " on { B310 && B311 } then { ... } " ?
// Possibilities: object.active OR !!object OR !(...!object).
// E.g:
on { B310.active && B311.active } then {
    Train1.stop()
    Train2.forward(10)
}

on { !(!B310 || !B311) } then {
    Train1.stop()
    Train2.forward(10)
}

on { !!B310 && !!B311 } then {
    Train1.stop()
    Train2.forward(10)
}

// FIXME continue here: route handling
val Route_Idle = route.idle()

val _leaving_speed = 5
val _mainline_speed = 10
val _reverse_speed = 8

val Route1 = route.sequence {
    throttle = Train1
    timeout = 60

    onActivate {
        Train1.light(true)
        Train1.horn()
        Train1.forward(_leaving_speed)
    }

    // Nodes must be declared before the node graph.

    val B310_fwd = node(B310) {
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

    val B311_fwd = node(B311) {
        onEnter {
            Train1.horn()
        }
    }

    val B310_rev = node(B310) {
        onEnter {
            Train1.horn()
        }
    }

//    nodes = [ [ B310_fwd, B311_fwd, B310_rev ],
//        [ B310_fwd, B310_rev ] ]

    nodes = listOf(
        listOf(B310_fwd, B311_fwd, B310_rev),
        listOf(B310_fwd, B310_rev)
    )
}

val PA_Route = activeRoute {
    routes = listOf(Route_Idle, Route1)
}
