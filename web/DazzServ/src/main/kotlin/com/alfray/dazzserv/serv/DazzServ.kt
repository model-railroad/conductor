/*
 * Project: DazzServ
 * Copyright (C) 2025 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alfray.dazzserv.serv

import com.alflabs.utils.ILogger
import com.alfray.dazzserv.utils.CnxStats
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.eclipse.jetty.io.Connection
import org.eclipse.jetty.io.EndPoint
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.CustomRequestLog
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.Slf4jRequestLogWriter
import org.eclipse.jetty.server.handler.DefaultHandler
import org.eclipse.jetty.server.handler.GracefulHandler
import org.eclipse.jetty.server.internal.HttpConnection

class DazzServ @AssistedInject constructor(
    private val logger: ILogger,
    private val cnxStats: CnxStats,
    private val dazzRestHandlerFactory: DazzRestHandlerFactory,
    @Assisted private val host: String,
    @Assisted private val port: Int,
) {
    private lateinit var server: Server

    companion object {
        const val TAG = "DazzServ"
        const val HTTP_CNX_TIMEOUT_MS = 5_000L // 5 seconds
    }

    fun createServer() {
        server = Server()

        host.split(",").forEach {
            val connector = ServerConnector(
                server,
                /*acceptors=*/ -1,
                /*selectors=*/ -1,
                DazzHttpConnectionFactory(cnxStats))
            connector.port = port
            connector.host = it
            connector.idleTimeout = HTTP_CNX_TIMEOUT_MS
            server.addConnector(connector)
            logger.d(TAG, "Serving on http://$it:$port")
        }

        // DefaultHandler serves a favicon or show contexts for debugging when all other
        // handlers return false.
        server.defaultHandler = DefaultHandler(/*serveFavIcon=*/ false, /*showContexts=*/ false)

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
            CustomRequestLog.EXTENDED_NCSA_FORMAT + " | %D us, %I bytes in, %O bytes out"
        )
    }

    fun runServer() {
        try {
            server.start()
            logger.d(TAG, "REST Server started")
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

/**
 * A custom Jetty HttpConnectionFactory that returns an HttpConnection that logs
 * the number of bytes receives and sent through the connection.
 */
private class DazzHttpConnectionFactory(
    private val cnxStats: CnxStats,
) : HttpConnectionFactory()
{
    override fun newConnection(
        connector: Connector,
        endPoint: EndPoint
    ): Connection? {
        // The code below mimics the original implementation from
        //    "return super.newConnection(connector, endPoint)"
        val connection = DazzHttpConnection(
            cnxStats,
            httpConfiguration,
            connector,
            endPoint)
        connection.isUseInputDirectByteBuffers = isUseInputDirectByteBuffers
        connection.isUseOutputDirectByteBuffers = isUseOutputDirectByteBuffers
        return configure<HttpConnection?>(connection, connector, endPoint)

    }
}

class DazzHttpConnection(
    private val cnxStats: CnxStats,
    httpConfiguration: HttpConfiguration,
    connector: Connector,
    endPoint: EndPoint,
) : HttpConnection(httpConfiguration, connector, endPoint)
{
    private var label = ""

    override fun onFillable() {
        var bIn = bytesIn
        var bOut = bytesOut
        label = "other"

        // The call "onRequest.run();" in the super
        // implementation invokes DazzRestHandler.handle().
        super.onFillable()

        bIn = bytesIn - bIn
        bOut = bytesOut - bOut
        cnxStats.accumulate(label, bIn, bOut)
    }

    // Called by DazzRestHandler using the static HttpConnection.currentConnection.
    fun setLabel(label: String) {
        this.label = label
    }
}
