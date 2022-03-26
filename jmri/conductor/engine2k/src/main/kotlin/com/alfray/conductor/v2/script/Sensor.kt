package com.alfray.conductor.v2.script

interface ISensor {
    val systemName: String
}

class Sensor(override val systemName: String) : ISensor {
}
