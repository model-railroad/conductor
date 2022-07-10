/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
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

package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.dagger.CommonTestModule
import com.alfray.conductor.v2.simulator.dagger.ISimul2kComponent
import com.alfray.conductor.v2.simulator.dagger.Simul2kModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [CommonTestModule::class, Simul2kModule::class])
interface ISimul2kTestComponent : ISimul2kComponent {
    fun inject(test: Simul2kJmriProviderTest)

    @Component.Factory
    interface Factory {
        fun createComponent(): ISimul2kTestComponent
    }
}
