package com.alfray.dazzserv

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import org.eclipse.jetty.server.CustomRequestLog
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.Slf4jRequestLogWriter
import org.eclipse.jetty.server.handler.DefaultHandler
import org.eclipse.jetty.server.handler.GracefulHandler


class DazzServ : CliktCommand() {
    val port by option(help = "Server Port").int().default(8080)
    val host by option(help = "Server Bind IP").default("0.0.0.0")

    companion object {
        @JvmStatic
        fun main(args: Array<String>) = DazzServ().main(args)
    }

    override fun run() {
        echo("DazzServ running on port $port")

        startServer()

        echo("DazzServ end")
    }

    private fun startServer() {
        // TBD move to creator method with a plan for testing
        val server = Server()
        val connector = ServerConnector(server)
        connector.port = port
        connector.host = host
        server.addConnector(connector)

        // DefaultHandler serves a favicon or show contexts for debugging when all other
        // handlers return false.
        server.defaultHandler = DefaultHandler(/*serveFavIcon=*/ false, /*showContexts=*/ true)

        // GracefulHandler prevents new connection during shutdown, with a stop timeout
        // for existing ones.
        val gracefulHandler = GracefulHandler()
        server.handler = gracefulHandler
        server.stopTimeout = 2_000  // seconds for current handlers to terminate on shutdown

        // DazzRestHandler is our REST API handler.
        gracefulHandler.handler = DazzRestHandler()

        // Sets the RequestLog to log to an SLF4J logger named
        // "org.eclipse.jetty.server.RequestLog" at INFO level.
        // See https://jetty.org/docs/jetty/12/programming-guide/server/http.html#request-logging
        server.requestLog = CustomRequestLog(
            Slf4jRequestLogWriter(),
            CustomRequestLog.EXTENDED_NCSA_FORMAT
        )

        try {
            server.start()
            echo("Jetty Server started on port $port")
            server.join()
        } catch (e: Exception) {
            echo("Jetty Server error: $e")
            server.stop()
        } finally {
            echo("Jetty Server shutdown")
            server.destroy()
        }
    }
}
