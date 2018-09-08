/*
 * Project: RTAC
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

package com.alflabs.rtac.fragment;

import com.alflabs.dagger.ActivityScope;
import dagger.Subcomponent;


@ActivityScope
@Subcomponent
public interface IFragmentComponent {

    interface Factory {
        IFragmentComponent create();
    }

    void inject(StatusFragment statusFragment);
    void inject(RoutesFragment routesFragment);
    void inject(EStopFragment eStopFragment);
    void inject(DebugFragment debugFragment);
    void inject(PsaTextFragment mapFragment);
    void inject(MapFragment mapFragment);
}
