package com.alfray.conductor.v2.script

interface IBlock {
    val systemName: String
}

class Block(override val systemName: String) : IBlock {
}
