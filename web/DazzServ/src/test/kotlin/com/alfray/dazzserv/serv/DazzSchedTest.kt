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

import com.alflabs.utils.FakeClock
import com.alflabs.utils.FakeFileOps
import com.alflabs.utils.StringLogger
import com.alfray.dazzserv.dagger.DaggerIMainTestComponent
import com.alfray.dazzserv.store.DataStore
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import java.io.File
import java.text.DateFormat
import javax.inject.Inject
import javax.inject.Named

class DazzSchedTest {
    @Inject lateinit var logger: StringLogger
    @Inject lateinit var clock: FakeClock
    @Inject lateinit var fileOps: FakeFileOps
    @Inject @Named("IsoDateOnly") lateinit var isoDateOnlyFormat: DateFormat
    private val mockDazzOff = mock<DazzOff>()
    private val mockStore = mock<DataStore>()
    private lateinit var dazzSched: DazzSched

    @Before
    fun setUp() {
        val component = DaggerIMainTestComponent.factory().createComponent()
        component.inject(this)

        // Create a version of DazzSched that uses a mock DataStore
        // instead of the real one. All other test components are fakes
        // (and not mocks).
        dazzSched = DazzSched(
            logger,
            clock,
            fileOps,
            mockStore,
            mockDazzOff,
            isoDateOnlyFormat)
    }

    @Test
    fun testSetAndCheckStoreDir_existingDir() {
        // Simulate a fake dir tmp/dazz/store
        val file = fileOps.toFile("tmp", "dazz", "store", "file.txt")
        createFakeFile(file)

        assertThat(dazzSched.setAndCheckStoreDir(file.parent)).isTrue()
    }

    @Test
    fun testSetAndCheckStoreDir_missingDir() {
        // Do not simulate a fake dir tmp/dazz/store
        val file = fileOps.toFile("tmp", "dazz", "store", "file.txt")
        assertThat(dazzSched.setAndCheckStoreDir(file.parent)).isFalse()
        assertThat(logger.string.replace('\\', '/')).isEqualTo(
            """
                DazzSched: ERROR: Store directory 'tmp/dazz/store' does not exist.
                      SUGGESTION: Create it first or select an existing directory with --store-dir.
                
                
            """.trimIndent())
    }

    @Test
    fun testFileForTimestamp() {
        // Simulate a fake dir tmp/dazz/store and set it as the store root
        val file = fileOps.toFile("tmp", "dazz", "store", "file.txt")
        createFakeFile(file)
        assertThat(dazzSched.setAndCheckStoreDir(file.parent)).isTrue()

        clock.setNow(24*3600*1000L)
        assertThat(dazzSched.fileForTimestamp(clock.elapsedRealtime())).isEqualTo(
            fileOps.toFile("tmp", "dazz", "store", "ds_1970-01-02.txt")
        )

        clock.add((25*3600+300)*1000L)
        assertThat(dazzSched.fileForTimestamp(clock.elapsedRealtime())).isEqualTo(
            fileOps.toFile("tmp", "dazz", "store", "ds_1970-01-03.txt")
        )

        clock.add((26*3600+123)*1000L)
        assertThat(dazzSched.fileForTimestamp(clock.elapsedRealtime())).isEqualTo(
            fileOps.toFile("tmp", "dazz", "store", "ds_1970-01-04.txt")
        )
    }

    @Test
    fun testDoSave() {
        clock.setNow(24*3600*1000L)

        // Simulate a fake dir tmp/dazz/store and set it as the store root
        val file = fileOps.toFile("tmp", "dazz", "store", "file.txt")
        createFakeFile(file)
        assertThat(dazzSched.setAndCheckStoreDir(file.parent)).isTrue()

        dazzSched.doSave()

        verify(mockStore).saveTo(eq(fileOps.toFile("tmp", "dazz", "store", "ds_1970-01-02.txt")))
        verifyNoMoreInteractions(mockStore)
    }

    @Test
    fun testLoad() {
        clock.setNow(24*3600*1000L)

        // Simulate a fake dir tmp/dazz/store and set it as the store root
        val file = fileOps.toFile("tmp", "dazz", "store", "file.txt")
        createFakeFile(file)
        assertThat(dazzSched.setAndCheckStoreDir(file.parent)).isTrue()

        // "fill" the directory with fake files to be loaded.
        // The N "most recent" will be loaded, based on their decreasing file name
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ignored_1972-12-12.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ignored_1972-11-11.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-10-10.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-09-09.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-08-08.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-08-08.txt.ignored"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-07-07.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-06-06.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-06-06.txt.ignored"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-05-05.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-04-04.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-03-13.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-02-02.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1971-01-01.txt"))
        createFakeFile(fileOps.toFile("tmp", "dazz", "store", "ds_1970-12-12.txt"))

        dazzSched.load()
        verify(mockStore).loadFrom(eq(fileOps.toFile("tmp", "dazz", "store", "ds_1971-10-10.txt")))
        verify(mockStore).loadFrom(eq(fileOps.toFile("tmp", "dazz", "store", "ds_1971-09-09.txt")))
        verify(mockStore).loadFrom(eq(fileOps.toFile("tmp", "dazz", "store", "ds_1971-08-08.txt")))
        verify(mockStore).loadFrom(eq(fileOps.toFile("tmp", "dazz", "store", "ds_1971-07-07.txt")))
        verify(mockStore).loadFrom(eq(fileOps.toFile("tmp", "dazz", "store", "ds_1971-06-06.txt")))
        verify(mockStore).loadFrom(eq(fileOps.toFile("tmp", "dazz", "store", "ds_1971-05-05.txt")))
        verify(mockStore).loadFrom(eq(fileOps.toFile("tmp", "dazz", "store", "ds_1971-04-04.txt")))
        verifyNoMoreInteractions(mockStore)
    }

    private fun createFakeFile(file: File) {
        fileOps.writeBytes(
            "content".toByteArray(Charsets.UTF_8),
            file
        )
    }
}
