package com.alfray.conductor.v2.script

interface ITurnout {
    val systemName: String
}

class Turnout(override val systemName: String) : ITurnout {
}
