package com.alfray.dazzserv

import com.alflabs.utils.FileOps
import com.alflabs.utils.ILogger
import com.alfray.dazzserv.dagger.DaggerIMainComponent
import com.alfray.dazzserv.dagger.IMainComponent
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
import javax.inject.Inject

private const val LOGGER_NAME = "com.alfray.DazzServer"

/**
 * Main entry point for DazzServ.
 *
 * @param autoStartServer Set to false during tests to avoid running actual web server.
 */
class Main(
    val autoStartServer: Boolean = true,
) : CliktCommand() {
    private lateinit var component: IMainComponent
    @Inject lateinit var logger: ILogger
    @Inject lateinit var fileOps: FileOps
    @Inject lateinit var dataStore: DataStore
    @Inject lateinit var dazzServFactory: DazzServFactory
    lateinit var server: DazzServ



    // Command Line Options
    val port by option(help = "Server Port").int().default(8080)
    val host by option(help = "Server Bind IP").default("127.0.0.1")

    companion object {
        const val TAG = "Main"

        @JvmStatic
        fun main(args: Array<String>) = Main().main(args)
    }


    override fun run() {
        // Initialize dagger stuff
        component = DaggerIMainComponent.factory().createComponent(this)
        component.inject(this)

        // Dagger objects can now be used.
        logger.d(TAG, "Configured for $host port $port")

        server = dazzServFactory.create(host, port)
        server.createServer()
        if (autoStartServer) {
            server.runServer()
        }

        logger.d(TAG, "End")
    }
}

