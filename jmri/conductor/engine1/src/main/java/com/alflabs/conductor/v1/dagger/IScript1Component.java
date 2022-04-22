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

package com.alflabs.conductor.v1.dagger;

import com.alflabs.conductor.v1.parser.Reporter;
import com.alflabs.conductor.v1.parser.Script1Parser2;
import com.alflabs.conductor.v1.script.ExecEngine1;
import com.alflabs.conductor.v1.script.Script1;
import dagger.BindsInstance;
import dagger.Subcomponent;

@Script1Scope
@Subcomponent
public interface IScript1Component {
    Script1 getScript1();
    ExecEngine1 getExecEngine1();
    Script1Parser2 getScript1Parser2();

    @Subcomponent.Factory
    interface Factory {
        IScript1Component createComponent(@BindsInstance Reporter reporter);
    }
}
