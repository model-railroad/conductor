package com.alfray.dazzserv

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int

class DazzServ : CliktCommand() {
    val port by option(help = "Server Port").int().default(8080)

    companion object {
        @JvmStatic
        fun main(args: Array<String>) = DazzServ().main(args)
    }

    override fun run() {
        echo("DazzServ running on port $port")
    }
}
