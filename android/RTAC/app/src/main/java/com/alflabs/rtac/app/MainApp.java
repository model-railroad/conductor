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
import android.content.Context;
import android.support.annotation.Nullable;
import com.alflabs.annotations.NonNull;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;

public class MainApp extends Application {
    private IAppComponent mAppComponent;

    @Inject ILogger mLogger;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = createDaggerAppComponent();
        mAppComponent.inject(this);
        mLogger.d("App", "onCreate");
    }

    @NonNull
    protected IAppComponent createDaggerAppComponent() {
        return DaggerIAppComponent
                .builder()
                .appContextModule(new AppContextModule(getApplicationContext()))
                .appDataModule(new AppDataModule())
                .build();
    }

    @Nullable
    public IAppComponent getAppComponent() {
        return mAppComponent;
    }

    @NonNull
    public static IAppComponent getAppComponent(Context context) {
        return ((MainApp) context.getApplicationContext()).mAppComponent;
    }
}
