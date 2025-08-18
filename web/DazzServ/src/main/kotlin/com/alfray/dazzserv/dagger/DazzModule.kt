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

package com.alfray.dazzserv.dagger

import com.alflabs.utils.FileOps
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.dazzserv.serv.DazzSched
import com.alfray.dazzserv.store.DataStore
import dagger.Module
import dagger.Provides
import java.text.DateFormat
import javax.inject.Named
import javax.inject.Singleton

@Module
object DazzModule {
    @Singleton
    @Provides
    fun provideAppUnderTest(): AppUnderTest {
        // This is not just a primitive boolean because "@Inject lateinit var" in kotlin requires
        // an object and not a primitive.
        return AppUnderTest(false)
    }

    @Singleton
    @Provides
    fun provideDazzSched(
        logger: ILogger,
        clock: IClock,
        fileOps: FileOps,
        store: DataStore,
        @Named("IsoDateOnly") isoDateOnlyFormat: DateFormat,
    ): DazzSched {
        // This provider becomes a mock<> in DazzTestModule.
        return DazzSched(logger, clock, fileOps, store, isoDateOnlyFormat)
    }
}

data class AppUnderTest(val isUnderTest: Boolean)

