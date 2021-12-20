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

package com.alflabs.rtac.app;

import com.alflabs.rtac.activity.IMainActivityComponent;
import com.alflabs.rtac.service.RtacService;
import com.alflabs.rtac.service.ServiceModule;
import com.alflabs.rtac.service.WakeWifiLockMixin;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = { AppContextModule.class, AppDataModule.class, ServiceModule.class} )
@SuppressWarnings("WeakerAccess")
public interface IAppComponent
    extends IMainActivityComponent.Factory {

    AppPrefsValues getAppPrefsValues();
    WakeWifiLockMixin getWakeWifiLockMixin();

    void inject(MainApp mainApp);
    void inject(BootReceiver bootReceiver);
    void inject(RtacService rtacService);
}
