package com.alfray.dazzserv

import com.alflabs.utils.ILogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.eclipse.jetty.server.CustomRequestLog
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.Slf4jRequestLogWriter
import org.eclipse.jetty.server.handler.DefaultHandler
import org.eclipse.jetty.server.handler.GracefulHandler

// -- obsolete -- private const val LOGGER_NAME = "com.alfray.DazzServer"

class DazzServ @AssistedInject constructor(
    private val logger: ILogger,
    private val dataStore: DataStore,
    private val dazzRestHandlerFactory: DazzRestHandlerFactory,
    @Assisted private val host: String,
    @Assisted private val port: Int,
) {
    private lateinit var server: Server

    companion object {
        const val TAG = "DazzServ"
    }

    fun createServer() {
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
        gracefulHandler.handler = dazzRestHandlerFactory.create {
            quitServer(server)
        }


        // Sets the RequestLog to log to an SLF4J logger named
        // "org.eclipse.jetty.server.RequestLog" at INFO level.
        // See https://jetty.org/docs/jetty/12/programming-guide/server/http.html#request-logging
        server.requestLog = CustomRequestLog(
            Slf4jRequestLogWriter(),
            CustomRequestLog.EXTENDED_NCSA_FORMAT
        )
    }

    fun runServer() {
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

@AssistedFactory
interface DazzServFactory {
    fun create(
        host: String,
        port: Int,
    ) : DazzServ
}
