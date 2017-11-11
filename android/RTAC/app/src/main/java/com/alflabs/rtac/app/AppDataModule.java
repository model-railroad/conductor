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

import android.net.wifi.WifiManager;
import com.alflabs.annotations.NonNull;
import com.alflabs.rtac.nsd.DiscoveryListener;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.rtac.service.KVClientStatsListener;
import com.alflabs.utils.AndroidClock;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
@SuppressWarnings("WeakerAccess")
public class AppDataModule {

    @NonNull
    @Provides
    @Singleton
    public DataClientMixin providesDataClientMixin(
            IClock clock,
            ILogger logger,
            AppPrefsValues appPrefsValues,
            DiscoveryListener nsdListener,
            KVClientStatsListener kvClientListener,
            WifiManager wifiManager) {
        return new DataClientMixin(
                clock,
                logger,
                appPrefsValues,
                nsdListener,
                kvClientListener,
                wifiManager);
    }

    @NonNull
    @Provides
    @Singleton
    public IClock providesClock() {
        return new AndroidClock();
    }
}
