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
import com.alfray.dazzserv.store.DataStore
import com.alfray.dazzserv.utils.CnxStats
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.server.internal.HttpConnection
import org.eclipse.jetty.util.Callback

class DazzRestHandler @AssistedInject constructor(
    private val logger: ILogger,
    private val dataStore: DataStore,
    private val cnxStats: CnxStats,
    @Assisted private val quitMethod: Runnable,
) : Handler.Abstract() {

    companion object {
        const val TAG = "DazzRestHandler"
    }

    // Handler API: https://jetty.org/docs/jetty/12/programming-guide/server/http.html#handler-impl
    // Note that each handler executes asynchronously in their own thread. Synchronization is
    // needed when accessing central resources.
    // Request is typically an HttpChannelState.ChannelRequest.
    // Response is typically an HttpChannelState.ChannelResponse.
    // Callback is typically an HttpChannelState.ChannelCallback.
    override fun handle(request: Request?, response: Response?, callback: Callback?): Boolean {
        val path = Request.getPathInContext(request)
        val isPost = HttpMethod.POST.`is`(request?.method)
        val isGet = HttpMethod.GET.`is`(request?.method)

        if (request != null && response != null && callback != null) {
            if (isPost && path.startsWith("/quitquitquit")) {
                return doPostQuit(path, response, callback)
            } else if (isPost && path.startsWith("/store")) {
                return doPostStore(path, request, response, callback)
            } else if (isGet && path.startsWith("/query")) {
                return doGetQuery(path, request, response, callback)
            } else if (isGet && path.startsWith("/live")) {
                return doGetLive(path, request, response, callback)
            } else if (isGet && path.startsWith("/perf")) {
                return doGetPerf(path, request, response, callback)
            } else if (isGet && path.startsWith("/statz")) {
                return doGetStats(path, request, response, callback)
            }
        }

        logger.d(TAG, "Request rejected")
        // Refuse to handle this request
        return false
    }

    private fun doPostQuit(
        path: String,
        response: Response,
        callback: Callback
    ): Boolean {
        logger.d(TAG, "doPostQuit: $path")
        setCnxStatsLabel("quit")

        // Command to trigger/test this:
        // $ wget --no-verbose -O - --post-data="" http://localhost:8080/quitquitquit
        // $ curl --data "" http://localhost:8080/quitquitquit

        reply(
            response,
            callback,
            "$path acknowledged")
        quitMethod.run()

        return true
    }

    private fun doPostStore(
        path: String,
        request: Request,
        response: Response,
        callback: Callback
    ): Boolean {
        logger.d(TAG, "doPostStore: $path")
        setCnxStatsLabel("store")

        val stream = Request.asInputStream(request)
        // Noob notes:
        // - kotlin's reader.use { } closes the stream after operation.
        // - bufferedReader defaults to UTF-8 encoding.
        val payload = stream.bufferedReader().use { reader -> reader.readText() }
        val ok = dataStore.store(payload)

        reply(
            response,
            callback,
            "$path ${if (ok) "acknowledged" else "rejected"}",
            if (ok) HttpStatus.OK_200 else HttpStatus.BAD_REQUEST_400)

        return true
    }

    private fun doGetQuery(
        path: String,
        request: Request,
        response: Response,
        callback: Callback
    ): Boolean {
        logger.d(TAG, "doGetQuery: $path")
        setCnxStatsLabel("query")

        val filter = if (path.startsWith("/query/")) {
            path.removePrefix("/query/")
        } else {
            ""
        }

        val content = dataStore.queryToJson(filter)

        reply(
            response,
            callback,
            content,
            if (content.isNotBlank()) HttpStatus.OK_200 else HttpStatus.NOT_FOUND_404,
            mimeType = "application/json")

        return true
    }

    private fun doGetLive(
        path: String,
        request: Request,
        response: Response,
        callback: Callback
    ): Boolean {
        logger.d(TAG, "doGetLive: $path")
        setCnxStatsLabel("live")

        val filter = if (path.startsWith("/live/")) {
            path.removePrefix("/live/")
        } else {
            ""
        }

        val lastTS = if (dataStore.mostRecentTS.isBlank()) ""
                     else "${dataStore.mostRecentTS};${filter.hashCode()}"
        if (checkIfNotModified(lastTS, request, response, callback)) {
            return true
        }

        val content = dataStore.liveToJson(filter)

        reply(
            response,
            callback,
            content,
            if (content.isNotBlank()) HttpStatus.OK_200 else HttpStatus.NOT_FOUND_404,
            mimeType = "application/json",
            etag = lastTS)

        return true
    }

    private fun doGetPerf(
        path: String,
        request: Request,
        response: Response,
        callback: Callback
    ): Boolean {
        logger.d(TAG, "doGetPerf: $path")
        setCnxStatsLabel("perf")

        val filter = if (path.startsWith("/perf/")) {
            path.removePrefix("/perf/")
        } else {
            ""
        }

        val lastTS = if (dataStore.mostRecentTS.isBlank()) ""
                     else "${dataStore.mostRecentTS};${filter.hashCode()}"
        if (checkIfNotModified(lastTS, request, response, callback)) {
            return true
        }

        val content = dataStore.perfToJson(filter)

        reply(
            response,
            callback,
            content,
            if (content.isNotBlank()) HttpStatus.OK_200 else HttpStatus.NOT_FOUND_404,
            mimeType = "application/json",
            etag = lastTS)

        return true
    }

    private fun doGetStats(
        path: String,
        request: Request,
        response: Response,
        callback: Callback
    ): Boolean {
        logger.d(TAG, "doGetStats: $path")
        setCnxStatsLabel("statz")

        val content = cnxStats.logToString()

        reply(
            response,
            callback,
            content)

        return true
    }

    private fun checkIfNotModified(
        etag: String,
        request: Request,
        response: Response,
        callback: Callback
    ): Boolean {
        if (etag.isBlank()) {
            return false
        }

        val noneMatch: String? = request.headers.get(HttpHeader.IF_NONE_MATCH)

        if (etag == noneMatch) {
            reply(
                response,
                callback,
                "", // no content for a 304
                HttpStatus.NOT_MODIFIED_304,
                etag = etag)
            return true
        } else {
            return false
        }
    }

    private fun reply(
        response: Response,
        callback: Callback,
        answer: String,
        status: Int = HttpStatus.OK_200,
        mimeType: String = "text/plain",
        etag: String? = null,
    ) {
        response.status = status

        if (!etag.isNullOrBlank()) {
            response.headers.put(HttpHeader.ETAG, etag)
        }

        if (answer.isNotEmpty()) {
            response.headers.put(HttpHeader.CONTENT_TYPE, "$mimeType; charset=UTF-8")
            Content.Sink.write(
                response,
                /*last=*/ true,
                answer,
                callback
            )
        } else {
            // If we don't write to the sink (and thus don't have last=true),
            // we need to clearly indicate we're done processing this request.
            callback.succeeded()
        }
    }

    private fun setCnxStatsLabel(label: String) {
        val currentCnx = HttpConnection.getCurrentConnection()
        if (currentCnx is DazzHttpConnection) {
            currentCnx.setLabel(label)
        }
    }
}

@AssistedFactory
interface DazzRestHandlerFactory {
    fun create(quitMethod: Runnable) : DazzRestHandler
}
