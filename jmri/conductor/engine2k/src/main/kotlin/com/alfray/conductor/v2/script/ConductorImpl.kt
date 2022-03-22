package com.alfray.conductor.v2.script

class ConductorImpl : IConductor {
    override fun sensor(systemName: String) {
        println("@@ sensor systemName = $systemName")
    }

    override fun block(systemName: String) {
        println("@@ block systemName = $systemName")
    }

    override fun turnout(systemName: String) {
        println("@@ turnout systemName = $systemName")
    }
}
