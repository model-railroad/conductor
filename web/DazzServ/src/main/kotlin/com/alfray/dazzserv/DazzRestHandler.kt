package com.alfray.dazzserv

import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.util.Callback

class DazzRestHandler : Handler.Abstract() {

    // Handler API: https://jetty.org/docs/jetty/12/programming-guide/server/http.html#handler-impl
    override fun handle(request: Request?, response: Response?, callback: Callback?): Boolean {
        val path = Request.getPathInContext(request)
        println("DazzRestHandler.handle $path, req $request, resp $response, cb $callback")
        if (request != null && response != null && callback != null) {
            if (path.startsWith("/example")) {
                // Accept to handle this request
                println("Request accepted")

                // Provide a text response
                response.status = 200
                response.headers.put(HttpHeader.CONTENT_TYPE, "text/plain; charset=UTF-8")

                Content.Sink.write(response,
                    /*last=*/ true,
                    """
                        Response to request
                        Path = $path
                    """.trimIndent(),
                    callback)

                return true
            }
        }

        println("Request rejected")
        // Refuse to handle this request
        return false
    }
}
