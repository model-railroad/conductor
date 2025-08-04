package com.alfray.dazzserv

import com.alflabs.utils.StringLogger
import com.google.common.truth.Truth.assertThat
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.http.HttpURI
import org.eclipse.jetty.server.Context
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.util.Callback
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


class DazzRestHandlerTest {
    private val logger = StringLogger()
    private val ds = mock<DataStore>()
    private val handler = DazzRestHandler(logger, ds, quitMethod = {})

    @Test
    fun testHandleExample() {
        val uri = HttpURI.Unsafe("http", "www.example.com", -1, "/example", "v=query", "fragment")
        val mockContext = mock<Context> {
            on { contextPath } doReturn uri.path
            on { getPathInContext(any()) } doReturn uri.path
        }
        val mockRequest = mock<Request> {
            on { method } doReturn HttpMethod.GET.asString()
            on { httpURI } doReturn uri
            on { context } doReturn mockContext
        }

        val mockResponse = mock<Response>()
        val mockCallback = mock<Callback>()

        assertThat(uri.toString()).isEqualTo("http://www.example.com/example?v=query#fragment")
        assertThat(handler.handle(mockRequest, mockResponse, mockCallback)).isFalse()
    }
}

