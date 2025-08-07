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

import com.alflabs.utils.ILogger
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
import org.eclipse.jetty.util.Callback

class DazzRestHandler @AssistedInject constructor(
    private val logger: ILogger,
    private val dataStore: DataStore,
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
            } else if (isGet && path.startsWith("/query/")) {
                return doGetQuery(path, request, response, callback)
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

        // Command to trigger/test this:
        // $ wget --no-verbose -O - --post-data="" http://localhost:8080/quitquitquit
        // $ curl --data "" http://localhost:8080/quitquitquit

        reply(
            response,
            callback,
            "$path acknowledged"
        )
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
            if (ok) HttpStatus.OK_200 else HttpStatus.BAD_REQUEST_400
        )
        return true
    }

    private fun doGetQuery(
        path: String,
        request: Request,
        response: Response,
        callback: Callback
    ): Boolean {
        logger.d(TAG, "doGetQuery: $path")

        val query = path.removePrefix("/query/")

        val content = dataStore.query(query)

        reply(
            response,
            callback,
            content,
            if (content.isNotBlank()) HttpStatus.OK_200 else HttpStatus.NOT_FOUND_404
        )

        return true
    }

    private fun reply(
        response: Response,
        callback: Callback,
        answer: String,
        status: Int = HttpStatus.OK_200,
    ) {
        // Provide a text response
        response.status = status
        response.headers.put(HttpHeader.CONTENT_TYPE, "text/plain; charset=UTF-8")

        Content.Sink.write(
            response,
            /*last=*/ true,
            answer,
            callback
        )
    }
}

@AssistedFactory
interface DazzRestHandlerFactory {
    fun create(quitMethod: Runnable) : DazzRestHandler
}
