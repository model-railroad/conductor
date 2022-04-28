package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.ExportedVars
import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.IActiveRouteBuilder
import com.alfray.conductor.v2.script.dsl.IAfter
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.IConductor
import com.alfray.conductor.v2.script.dsl.IGaEventBuilder
import com.alfray.conductor.v2.script.dsl.IGaPageBuilder
import com.alfray.conductor.v2.script.dsl.IJsonEventBuilder
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.IRule
import com.alfray.conductor.v2.script.dsl.ISensor
import com.alfray.conductor.v2.script.dsl.ISvgMap
import com.alfray.conductor.v2.script.dsl.ISvgMapBuilder
import com.alfray.conductor.v2.script.dsl.IThrottle
import com.alfray.conductor.v2.script.dsl.ITimer
import com.alfray.conductor.v2.script.dsl.ITurnout
import com.alfray.conductor.v2.script.impl.ActiveRoute
import com.alfray.conductor.v2.script.impl.ActiveRouteBuilder
import com.alfray.conductor.v2.script.impl.After
import com.alfray.conductor.v2.script.impl.Block
import com.alfray.conductor.v2.script.impl.GaEvent
import com.alfray.conductor.v2.script.impl.GaEventBuilder
import com.alfray.conductor.v2.script.impl.GaPage
import com.alfray.conductor.v2.script.impl.GaPageBuilder
import com.alfray.conductor.v2.script.impl.JsonEvent
import com.alfray.conductor.v2.script.impl.JsonEventBuilder
import com.alfray.conductor.v2.script.impl.Rule
import com.alfray.conductor.v2.script.impl.Sensor
import com.alfray.conductor.v2.script.impl.SvgMapBuilder
import com.alfray.conductor.v2.script.impl.Throttle
import com.alfray.conductor.v2.script.impl.Timer
import com.alfray.conductor.v2.script.impl.Turnout

private const val VERBOSE = false

internal class ConductorImpl : IConductor {

    val sensors = mutableMapOf<String, Sensor>()
    val blocks = mutableMapOf<String, Block>()
    val turnouts = mutableMapOf<String, Turnout>()
    val throttles = mutableMapOf<Int, Throttle>()
    val svgMaps = mutableMapOf<String, ISvgMap>()
    val timers = mutableListOf<Timer>()
    val rules = mutableListOf<Rule>()
    val activeRoutes = mutableListOf<IActiveRoute>()
    var lastGaPage: GaPage? = null
        private set
    var lastGaEvent: GaEvent? = null
        private set
    var lastJsonEvent: JsonEvent? = null
        private set

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

    override fun activeRoute(init: IActiveRouteBuilder.() -> Unit): IActiveRoute {
        val b = ActiveRouteBuilder()
        b.init()
        val a = ActiveRoute(b)
        activeRoutes.add(a)
        return a
    }

    override fun ga_page(init: IGaPageBuilder.() -> Unit) {
        if (VERBOSE) println("@@ ga_page = $init")
        val builder = GaPageBuilder()
        builder.init()
        val pg = builder.create()
        // TODO send page
        lastGaPage = pg
    }

    override fun ga_event(init: IGaEventBuilder.() -> Unit) {
        if (VERBOSE) println("@@ ga_event = $init")
        val builder = GaEventBuilder()
        builder.init()
        val ev = builder.create()
        // TODO send event
        lastGaEvent = ev
    }

    override fun json_event(init: IJsonEventBuilder.() -> Unit) {
        if (VERBOSE) println("@@ json_event = $init")
        val builder = JsonEventBuilder()
        builder.init()
        val ev = builder.create()
        // TODO send event
        lastJsonEvent = ev
    }

    override fun estop() {
        TODO("Not yet implemented")
    }

    override fun reset_timers(vararg prefix: String) {
        TODO("Not yet implemented")
    }
}
