package com.alfray.dazzserv.utils

import com.alflabs.utils.ILogger
import com.github.ajalt.clikt.core.CliktCommand

class CommandLogger(private val echoer: CliktCommand) : ILogger {
    override fun d(tag: String?, message: String?) {
        echoer.echo("$tag: $message")
    }

    override fun d(tag: String?, message: String?, tr: Throwable?) {
        echoer.echo("$tag: $message: $tr")
    }
}
