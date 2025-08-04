package com.alfray.dazzserv

import com.alflabs.utils.ILogger
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.util.Callback

class DazzRestHandler(
    private val logger: ILogger,
    private val dataStore: DataStore,
    private val quitMethod: Runnable,
) : Handler.Abstract() {

    companion object {
        const val TAG = "DazzRestHandler"
    }

    // Handler API: https://jetty.org/docs/jetty/12/programming-guide/server/http.html#handler-impl
    // Note that each handler executes asynchronously in their own thread. Synchronization is
    // needed when accessing central resources.
    override fun handle(request: Request?, response: Response?, callback: Callback?): Boolean {
        val path = Request.getPathInContext(request)
        val isPost = HttpMethod.POST.`is`(request?.method)
        val isGet = HttpMethod.GET.`is`(request?.method)

        if (request != null && response != null && callback != null) {
            if (isPost && path.startsWith("/quitquitquit")) {
                return doPostQuit(path, response, callback)
            } else if (isGet && path.startsWith("/store")) {
                return doPostStore(path, request, response, callback)
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
            "$path acknowledged",
            if (ok) HttpStatus.OK_200 else HttpStatus.BAD_REQUEST_400
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
