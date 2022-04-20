package com.alfray.conductor.v2.script

interface ISvgMap {
    val name: String
    val svg: String
}

interface ISvgMapBuilder {
    var name: String
    var svg: String
}
