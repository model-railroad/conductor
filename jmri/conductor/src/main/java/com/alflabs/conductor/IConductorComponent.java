/*
 * Project: Conductor
 * Copyright (C) 2017 alf.labs gmail com,
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

package com.alflabs.conductor;

import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.v1.script.IScriptComponent;
import com.alflabs.conductor.v1.script.ScriptModule;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.IClock;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

@Singleton
@Component(modules = {ConductorModule.class})
public interface IConductorComponent {

    IClock getClock();
    KeyValueServer getKeyValueServer();
    IJmriProvider getJmriProvider();
    @Named("script") File getScriptFile();

    void inject(EntryPoint entryPoint);

    IScriptComponent newScriptComponent(ScriptModule scriptModule);

    @Component.Builder
    interface Builder {
        IConductorComponent build();
        Builder conductorModule(ConductorModule conductorModule);
        @BindsInstance Builder scriptFile(@Named("script") File scriptFile);
    }
}
