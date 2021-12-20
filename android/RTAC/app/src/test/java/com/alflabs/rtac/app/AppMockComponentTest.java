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

import android.app.Application;
import com.alflabs.rtac.BuildConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = 19,
        manifest = "src/main/AndroidManifest.xml",
        application = AppMockComponent.class)
public class AppMockComponentTest {
    private Application mApp;

    @Before
    public void setUp() throws Exception {
        mApp = RuntimeEnvironment.application;
        assertThat(mApp).isInstanceOf(AppMockComponent.class);
    }

    @Test
    public void testOnCreate() throws Exception {
        assertThat(mApp).isNotNull();
        assertThat(mApp).isInstanceOf(MainApp.class);
    }

    @Test
    public void testAppComponent() throws Exception {
        IAppComponent component = MainApp.getAppComponent(RuntimeEnvironment.application);
        assertThat(component).isInstanceOf(IAppComponent.class);
        assertThat(component.getAppPrefsValues()).isNotNull();
        assertThat(component.getWakeWifiLockMixin()).isNotNull();
    }

    @Test
    public void testAppModule() throws Exception {
        AppContextModule module = ((AppMockComponent) mApp).getAppContextModule();
        assertThat(module).isNotNull();
        assertThat(module.providesContext()).isNotNull();
        assertThat(module.providesLogger()).isNotNull();
        assertThat(module.providesNotificationManager()).isNotNull();
        assertThat(module.providesWifiManager()).isNotNull();
    }
}
