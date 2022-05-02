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

package com.alflabs.conductor.v1.dagger;

import com.alflabs.conductor.dagger.CommonTestModule;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.v1.parser.ScriptParser2Test;
import com.alflabs.conductor.v1.parser.ScriptParserFullTest;
import com.alflabs.conductor.v1.simulator.SimulatorTest;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = { CommonTestModule.class })
public interface IEngine1TestComponent extends IEngine1Component {

    void inject(SimulatorTest simulatorTest);
    void inject(ScriptParser2Test scriptParser2Test);
    void inject(ScriptParserFullTest scriptParserFullTest);
    void inject(IEngine1TestComponentTest iEngine1TestComponentTest);

    @Component.Factory
    interface Factory {
        IEngine1TestComponent createTestComponent(@BindsInstance IJmriProvider jmriProvider);
    }
}
