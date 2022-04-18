package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.script.impl.SvgMap

interface ISvgMap {
    val name: String
    val svg: String
}

interface ISvgMapBuilder {
    var name: String
    var svg: String
}
