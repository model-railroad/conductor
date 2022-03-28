

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

