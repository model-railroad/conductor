package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.script.impl.ActiveRoute
import com.alfray.conductor.v2.script.impl.After
import com.alfray.conductor.v2.script.impl.Block
import com.alfray.conductor.v2.script.impl.Rule
import com.alfray.conductor.v2.script.impl.Sensor
import com.alfray.conductor.v2.script.impl.SvgMapBuilder
import com.alfray.conductor.v2.script.impl.Throttle
import com.alfray.conductor.v2.script.impl.Timer
import com.alfray.conductor.v2.script.impl.Turnout

private const val VERBOSE = false

class ConductorImpl : IConductor {

    val sensors = mutableMapOf<String, Sensor>()
    val blocks = mutableMapOf<String, Block>()
    val turnouts = mutableMapOf<String, Turnout>()
    val throttles = mutableMapOf<Int, Throttle>()
    val svgMaps = mutableMapOf<String, ISvgMap>()
    val timers = mutableListOf<Timer>()
    val rules = mutableListOf<Rule>()
    val activeRoutes = mutableListOf<IActiveRoute>()

    override val exportedVars = ExportedVars()

    override fun sensor(systemName: String): ISensor {
        if (VERBOSE) println("@@ sensor systemName = $systemName")
        return sensors.computeIfAbsent(systemName) { Sensor(it) }
    }

    override fun block(systemName: String): IBlock {
        if (VERBOSE) println("@@ block systemName = $systemName")
        return blocks.computeIfAbsent(systemName) { Block(it) }
    }

    override fun turnout(systemName: String): ITurnout {
        if (VERBOSE) println("@@ turnout systemName = $systemName")
        return turnouts.computeIfAbsent(systemName) { Turnout(it) }
    }

    override fun timer(delay: Delay): ITimer {
        if (VERBOSE) println("@@ timer seconds = $delay")
        val t = Timer(delay)
        timers.add(t)
        return t
    }

    override fun throttle(dccAddress: Int): IThrottle {
        if (VERBOSE) println("@@ throttle dccAddress = $dccAddress")
        return throttles.computeIfAbsent(dccAddress) { Throttle(it) }
    }

    override fun map(init: ISvgMapBuilder.() -> Unit): ISvgMap {
        if (VERBOSE) println("@@ map = $init")
        val builder = SvgMapBuilder()
        builder.init()
        val m = builder.create()
        if (svgMaps.contains(m.name)) {
            throw IllegalArgumentException ("Map name ${m.name} is already defined.")
        }
        svgMaps[m.name] = m
        return m
    }

    override fun on(condition: () -> Any): IRule {
        if (VERBOSE) println("@@ on = $condition")
        val rule = Rule(condition)
        rules.add(rule)
        return rule
    }

    override fun after(delay: Delay): IAfter {
        return After(delay)
    }

    override val route = RouteBuilder()

    override fun activeRoute(init: IActiveRouteBuilder.() -> Unit): IActiveRoute {
        if (VERBOSE) println("@@ activeRoute = $init")
        val b = ActiveRouteBuilder()
        b.init()
        val a = ActiveRoute(b)
        activeRoutes.add(a)
        return a
    }

    override fun ga_event(init: IGaEventBuilder.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun json_event(init: IJsonEventBuilder.() -> Unit) {
        TODO("Not yet implemented")
    }
}
