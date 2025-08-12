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

import com.alflabs.dazzserv.store.DataEntry
import com.alflabs.utils.StringLogger
import com.alfray.dazzserv.dagger.DaggerIMainTestComponent
import com.alfray.dazzserv.store.DataStore
import com.google.common.truth.Truth.assertThat
import org.eclipse.jetty.http.HttpFields
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.http.HttpURI
import org.eclipse.jetty.http.HttpVersion
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Components
import org.eclipse.jetty.server.ConnectionMetaData
import org.eclipse.jetty.server.Context
import org.eclipse.jetty.server.HttpStream
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.server.Session
import org.eclipse.jetty.server.TunnelSupport
import org.eclipse.jetty.util.Attributes
import org.eclipse.jetty.util.Callback
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import javax.inject.Inject


class DazzRestHandlerTest {
    @Inject lateinit var logger: StringLogger
    @Inject lateinit var ds: DataStore
    @Inject lateinit var dazzRestHandlerFactory: DazzRestHandlerFactory
    private lateinit var handler: DazzRestHandler

    @Before
    fun setUp() {
        val component = DaggerIMainTestComponent.factory().createComponent()
        component.inject(this)
        handler = dazzRestHandlerFactory.create { }
    }

    @Test
    fun testGetExample() {
        val uri = HttpURI.Unsafe("http", "www.example.com", 8080, "/example", "v=query", "fragment")
        val request = createRequestFromUri(HttpMethod.GET, uri)

        val response = FakeResponse(request)
        val callback = mock<Callback>()

        assertThat(uri.toString()).isEqualTo("http://www.example.com:8080/example?v=query#fragment")
        assertThat(handler.handle(request, response, callback)).isFalse()
        assertThat(response.getBuffer()).isEmpty()
    }

    @Test
    fun testPostQuitQuitQuit() {
        val request = createRequest(HttpMethod.POST, "/quitquitquit")

        val response = FakeResponse(request)
        val callback = mock<Callback>()

        var quitCalled = false
        val handlerQuit = DazzRestHandler(logger, ds, quitMethod = {
            quitCalled = true
        })

        assertThat(handlerQuit.handle(request, response, callback)).isTrue()
        assertThat(quitCalled).isTrue()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.getBuffer()).isEqualTo("/quitquitquit acknowledged")
    }

    @Test
    fun testPostStore_noPayload() {
        val request = createRequest(HttpMethod.POST, "/store")

        val response = FakeResponse(request)
        val callback = mock<Callback>()

        assertThat(handler.handle(request, response, callback)).isTrue()
        assertThat(response.status).isEqualTo(400)
        assertThat(response.getBuffer()).isEqualTo("/store rejected")
        assertThat(ds.storeToJson()).isEqualTo("{ }")
    }

    @Test
    fun testPostStore_correctPayload() {
        val jsonPayload =
            """
                {"key":"toggles/entry1","ts":"1970-01-01T00:03:54Z","st":true,"d":"( some payload )"}
            """.trimIndent()

        val request = createRequest(HttpMethod.POST, "/store", content = jsonPayload)

        val response = FakeResponse(request)
        val callback = mock<Callback>()

        assertThat(handler.handle(request, response, callback)).isTrue()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.getBuffer()).isEqualTo("/store acknowledged")
        assertThat(ds.storeToJson()).isEqualTo(
            """
                {
                  "toggles/entry1": {
                    "entries": {
                      "1970-01-01T00:03:54Z": {"key": "toggles/entry1", "ts": "1970-01-01T00:03:54Z", "st": true, "d": "( some payload )"}
                    }
                  }
                }
            """.trimIndent())
    }

    @Test
    fun testGetQuery_noPayload() {
        ds.add(DataEntry("toggles/entry1", "1970-01-04T00:06:59Z", true, "payload 1"))
        ds.add(DataEntry("toggles/entry2", "1970-01-03T00:05:48Z", true, "payload 2"))
        ds.add(DataEntry("toggles/entry1", "1970-01-01T00:04:37Z", true, "payload 3"))
        ds.add(DataEntry("toggles/entry2", "1970-01-02T00:03:26Z", true, "payload 4"))

        val request = createRequest(HttpMethod.GET, "/query/")

        val response = FakeResponse(request)
        val callback = mock<Callback>()

        assertThat(handler.handle(request, response, callback)).isTrue()
        assertThat(response.status).isEqualTo(200)
        // Note: this is about testing the REST call. We don't want to dup the DataStore test here.
        assertThat(response.getBuffer()).isNotEmpty()
    }

    @Test
    fun testGetQuery_correctPayload() {
        ds.add(DataEntry("toggles/entry1", "1970-01-04T00:06:59Z", true, "payload 1"))
        ds.add(DataEntry("toggles/entry2", "1970-01-03T00:05:48Z", true, "payload 2"))
        ds.add(DataEntry("toggles/entry1", "1970-01-01T00:04:37Z", true, "payload 3"))
        ds.add(DataEntry("toggles/entry2", "1970-01-02T00:03:26Z", true, "payload 4"))

        val request = createRequest(HttpMethod.GET, "/query/toggles/**1")

        val response = FakeResponse(request)
        val callback = mock<Callback>()

        assertThat(handler.handle(request, response, callback)).isTrue()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.getBuffer()).isEqualTo(
            """
                {
                  "toggles/entry1": {
                    "entries": {
                      "1970-01-04T00:06:59Z": {"key": "toggles/entry1", "ts": "1970-01-04T00:06:59Z", "st": true, "d": "payload 1"}, 
                      "1970-01-01T00:04:37Z": {"key": "toggles/entry1", "ts": "1970-01-01T00:04:37Z", "st": true, "d": "payload 3"}
                    }
                  }
                }
            """.trimIndent())
    }

    @Test
    fun testGetLive_noData() {
        val request = createRequest(HttpMethod.GET, "/live")

        val response = FakeResponse(request)
        val callback = mock<Callback>()

        assertThat(handler.handle(request, response, callback)).isTrue()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.getBuffer()).isEqualTo("{ }")
    }

    @Test
    fun testGetLive_correctData() {
        ds.add(DataEntry("toggles/entry1", "1970-01-04T00:06:59Z", true, "payload 1"))
        ds.add(DataEntry("toggles/entry2", "1970-01-03T00:05:48Z", true, "payload 2"))
        ds.add(DataEntry("toggles/entry1", "1970-01-01T00:04:37Z", false, "payload 3"))
        ds.add(DataEntry("toggles/entry2", "1970-01-02T00:03:26Z", false, "payload 4"))
        ds.add(DataEntry("toggles/entry1", "1970-01-05T00:06:89Z", true, "payload 5"))
        ds.add(DataEntry("toggles/entry2", "1970-01-06T00:07:89Z", true, "payload 6"))

        val request = createRequest(HttpMethod.GET, "/live")

        val response = FakeResponse(request)
        val callback = mock<Callback>()

        assertThat(handler.handle(request, response, callback)).isTrue()
        assertThat(response.status).isEqualTo(200)
        // Note: this is about testing the REST call. We don't want to dup the DataStore test here.
        assertThat(response.getBuffer()).isNotEmpty()
    }

    @Test
    fun testGetHistory_noData() {
        val request = createRequest(HttpMethod.GET, "/history")

        val response = FakeResponse(request)
        val callback = mock<Callback>()

        assertThat(handler.handle(request, response, callback)).isTrue()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.getBuffer()).isEqualTo("{ }")
    }

    @Test
    fun testGetHistory_correctData() {
        ds.add(DataEntry("toggles/entry1", "1970-01-04T00:06:59Z", true, "payload 1"))
        ds.add(DataEntry("toggles/entry2", "1970-01-03T00:05:48Z", true, "payload 2"))
        ds.add(DataEntry("toggles/entry1", "1970-01-01T00:04:37Z", false, "payload 3"))
        ds.add(DataEntry("toggles/entry2", "1970-01-02T00:03:26Z", false, "payload 4"))
        ds.add(DataEntry("toggles/entry1", "1970-01-05T00:06:89Z", true, "payload 5"))
        ds.add(DataEntry("toggles/entry2", "1970-01-06T00:07:89Z", true, "payload 6"))

        val request = createRequest(HttpMethod.GET, "/history")

        val response = FakeResponse(request)
        val callback = mock<Callback>()

        assertThat(handler.handle(request, response, callback)).isTrue()
        assertThat(response.status).isEqualTo(200)
        // Note: this is about testing the REST call. We don't want to dup the DataStore test here.
        assertThat(response.getBuffer()).isNotEmpty()
    }

    // -- test helpers --

    private fun createRequestFromUri(
        httpMethod: HttpMethod,
        uri: HttpURI.Unsafe,
        content: String? = null,
    ): Request {
        return FakeRequest(httpMethod, uri, content)
    }

    private fun createRequest(
        method: HttpMethod,
        path: String,
        query: String? = null,
        fragment: String? = null,
        content: String? = null,
    ): Request {
        val uri = HttpURI.Unsafe("http", "www.example.com", /*port=*/ 8080, path, query, fragment)
        return createRequestFromUri(method, uri, content)
    }
}

private class FakeRequest(
    private val httpMethod: HttpMethod,
    private val uri: HttpURI.Unsafe,
    private val content: String? = null,
) : Attributes.Lazy(), Request {
    private val fakeFields = HttpFields.EMPTY
    private val metadata = org.eclipse.jetty.http.MetaData.Request(
        /*beginNanoTime=*/ 0,
        httpMethod.toString(),
        uri,
        HttpVersion.HTTP_1_1,
        fakeFields
    )

    val mockContext = mock<Context> {
        on { contextPath } doReturn uri.path
        on { getPathInContext(any()) } doReturn uri.path
    }

    override fun getContext(): Context {
        return mockContext
    }

    override fun getMethod(): String {
        return metadata.method
    }

    override fun getHttpURI(): HttpURI {
        return metadata.httpURI
    }

    override fun getHeaders(): HttpFields {
        return metadata.httpFields
    }

    override fun getBeginNanoTime(): Long {
        return metadata.beginNanoTime
    }

    override fun read(): Content.Chunk {
        if (content.isNullOrEmpty()) {
            return Content.Chunk.EOF
        } else {
            val buf = ByteBuffer.wrap(content.toByteArray(StandardCharsets.UTF_8))
            return Content.Chunk.from(buf, /*last=*/ true)
        }
    }

    // --

    override fun demand(demandCallback: Runnable?) {
        TODO("Not yet implemented in fake")
    }

    override fun fail(failure: Throwable?) {
        TODO("Not yet implemented in fake")
    }

    override fun getId(): String {
        TODO("Not yet implemented in fake")
    }

    override fun getComponents(): Components {
        TODO("Not yet implemented in fake")
    }

    override fun getConnectionMetaData(): ConnectionMetaData {
        TODO("Not yet implemented in fake")
    }


    override fun getTrailers(): HttpFields {
        TODO("Not yet implemented in fake")
    }

    override fun getHeadersNanoTime(): Long {
        TODO("Not yet implemented in fake")
    }

    override fun isSecure(): Boolean {
        TODO("Not yet implemented in fake")
    }

    override fun consumeAvailable(): Boolean {
        TODO("Not yet implemented in fake")
    }

    override fun addIdleTimeoutListener(onIdleTimeout: Predicate<TimeoutException>?) {
        TODO("Not yet implemented in fake")
    }

    override fun addFailureListener(onFailure: Consumer<Throwable>?) {
        TODO("Not yet implemented in fake")
    }

    override fun getTunnelSupport(): TunnelSupport {
        TODO("Not yet implemented in fake")
    }

    override fun addHttpStreamWrapper(wrapper: Function<HttpStream, HttpStream>?) {
        TODO("Not yet implemented in fake")
    }

    override fun getSession(create: Boolean): Session {
        TODO("Not yet implemented in fake")
    }
}


@Suppress("PrivatePropertyName")
private class FakeResponse(
    private val request: Request
) : Response {
    private val httpFields = HttpFields.build()
    private var status_ : Int = -1
    private val buffer_ = StringBuilder()

    fun getBuffer() : String {
        return buffer_.toString()
    }

    override fun getRequest(): Request {
        return request
    }

    override fun getHeaders(): HttpFields.Mutable {
        return httpFields
    }

    override fun getStatus(): Int {
        return status_
    }

    override fun setStatus(code: Int) {
        status_ = code
    }

    // ---

    override fun write(last: Boolean, byteBuffer: ByteBuffer?, callback: Callback?) {
        val charBuffer = StandardCharsets.UTF_8.decode(byteBuffer!!)
        val str = charBuffer.toString()
        buffer_.append(str)
    }

    override fun getTrailersSupplier(): Supplier<HttpFields> {
        TODO("Not yet implemented in fake")
    }

    override fun setTrailersSupplier(trailers: Supplier<HttpFields>?) {
        TODO("Not yet implemented in fake")
    }

    override fun isCommitted(): Boolean {
        TODO("Not yet implemented in fake")
    }

    override fun hasLastWrite(): Boolean {
        TODO("Not yet implemented in fake")
    }

    override fun isCompletedSuccessfully(): Boolean {
        TODO("Not yet implemented in fake")
    }

    override fun reset() {
        TODO("Not yet implemented in fake")
    }

    override fun writeInterim(status: Int, headers: HttpFields?): CompletableFuture<Void> {
        TODO("Not yet implemented in fake")
    }
}
