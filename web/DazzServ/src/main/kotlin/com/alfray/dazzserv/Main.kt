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

package com.alfray.dazzserv

import com.alflabs.utils.FileOps
import com.alflabs.utils.ILogger
import com.alfray.dazzserv.dagger.DaggerIMainComponent
import com.alfray.dazzserv.dagger.IMainComponent
import com.alfray.dazzserv.serv.DazzServ
import com.alfray.dazzserv.serv.DazzServFactory
import com.alfray.dazzserv.store.DataStore
import com.alfray.dazzserv.store.DazzSched
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named

/**
 * Main entry point for DazzServ.
 */
open class Main : CliktCommand() {
    private lateinit var component: IMainComponent
    @Inject lateinit var logger: ILogger
    @Inject lateinit var fileOps: FileOps
    @Inject lateinit var dataStore: DataStore
    @Inject lateinit var dazzServFactory: DazzServFactory
    @Inject lateinit var dazzSched: DazzSched
    @Inject @Named("AppUnderTest") lateinit var appUnderTest: AtomicBoolean
    private lateinit var server: DazzServ

    // Command Line Options
    val port by option(help = "Server Port").int().default(8080)
    val host by option(help = "Server Bind IP").default("127.0.0.1")
    val storeDir by option(help = "Directory for store files").default("~/.dazz-store")

    companion object {
        const val TAG = "Main"

        @JvmStatic
        fun main(args: Array<String>) {
            Main().main(args)
        }
    }

    open fun createComponent(): IMainComponent {
        return DaggerIMainComponent.factory().createComponent(this)
    }

    override fun run() {
        // Initialize dagger stuff
        component = createComponent()
        component.inject(this)

        // Dagger objects can now be used.
        val runningTests = appUnderTest.get()
        logger.d(TAG, "Running ${if (runningTests) "test" else "prod"} version")
        logger.d(TAG, "Configured for $host port $port")

        if (!dazzSched.setAndCheckStoreDir(storeDir)) {
            // Returned false if the directory did not exist.
            // Logging will have already happened, we just need to abort if not under tests.
            if (!runningTests) {
                logger.d(TAG, "Aborting")
                return
            }
        }
        dazzSched.load()

        server = dazzServFactory.create(host, port)
        server.createServer()
        if (!runningTests) {
            dazzSched.start()
            server.runServer()
        }

        dazzSched.stop()
        logger.d(TAG, "End")
    }

}

