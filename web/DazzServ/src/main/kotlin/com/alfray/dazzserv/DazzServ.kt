package com.alfray.dazzserv

import com.alflabs.utils.ILogger
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

private const val LOGGER_NAME = "com.alfray.DazzServer"

/**
 * Main entry point for DazzServ.
 *
 * @param autoStartServer Set to false during tests to avoid running actual web server.
 */
class DazzServ(val autoStartServer: Boolean = true) : CliktCommand() {
    lateinit var server: Server
    val port by option(help = "Server Port").int().default(8080)
    val host by option(help = "Server Bind IP").default("127.0.0.1")

    companion object {
        const val TAG = "DazzServ"

        @JvmStatic
        fun main(args: Array<String>) = DazzServ().main(args)
    }

    val logger: ILogger = object : ILogger {
        override fun d(tag: String?, message: String?) {
            echo("$tag: $message")
        }

        override fun d(tag: String?, message: String?, tr: Throwable?) {
            echo("$tag: $message: $tr")
        }
    }

    override fun run() {
        logger.d(TAG, "Configured for $host port $port")

        createServer()
        if (autoStartServer) {
            runServer()
        }

        logger.d(TAG, "End")
    }

    private fun createServer() {
        server = Server()

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
        gracefulHandler.handler = DazzRestHandler(
            logger,
            quitMethod = {
                quitServer(server)
            }
        )

        // Sets the RequestLog to log to an SLF4J logger named
        // "org.eclipse.jetty.server.RequestLog" at INFO level.
        // See https://jetty.org/docs/jetty/12/programming-guide/server/http.html#request-logging
        server.requestLog = CustomRequestLog(
            Slf4jRequestLogWriter(),
            CustomRequestLog.EXTENDED_NCSA_FORMAT
        )
    }

    private fun runServer() {
        try {
            server.start()
            logger.d(TAG, "REST Server started on http://$host:$port")
            server.join()
        } catch (e: Exception) {
            logger.d(TAG, "REST Server error: $e")
            server.stop()
        } finally {
            logger.d(TAG, "REST Server shutdown")
            server.destroy()
        }
    }

    private fun quitServer(server: Server) {
        logger.d(TAG, "REST Server quit requested")
        server.stop()
    }
}
