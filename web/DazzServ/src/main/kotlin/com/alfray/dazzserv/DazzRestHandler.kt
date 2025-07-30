package com.alfray.dazzserv

import com.alflabs.utils.ILogger
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.util.Callback

class DazzRestHandler(
    val logger: ILogger,
    val quitMethod: Runnable
) : Handler.Abstract() {

    companion object {
        const val TAG = "DazzRestHandler"
    }

    // Handler API: https://jetty.org/docs/jetty/12/programming-guide/server/http.html#handler-impl
    override fun handle(request: Request?, response: Response?, callback: Callback?): Boolean {
        val path = Request.getPathInContext(request)
        if (request != null && response != null && callback != null) {
            if (path.startsWith("/quitquitquit")) {
                logger.d(TAG, "Handling $path")
                reply200(
                    response,
                    callback,
                    """
                        $path acknowledged
                    """.trimIndent())
                quitMethod.run()
            } else if (path.startsWith("/example")) {
                logger.d(TAG, "Handling $path")
                reply200(
                    response,
                    callback,
                    """
                        Response to request
                        Path = $path
                    """.trimIndent())
                return true
            }
        }

        logger.d(TAG, "Request rejected")
        // Refuse to handle this request
        return false
    }

    private fun reply200(
        response: Response,
        callback: Callback,
        answer: String,
    ) {
        // Provide a text response
        response.status = 200
        response.headers.put(HttpHeader.CONTENT_TYPE, "text/plain; charset=UTF-8")

        Content.Sink.write(
            response,
            /*last=*/ true,
            answer,
            callback
        )
    }
}
