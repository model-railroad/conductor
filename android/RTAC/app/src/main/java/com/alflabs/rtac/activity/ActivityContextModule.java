package com.alflabs.rtac.activity;

import android.content.Context;
import com.alflabs.annotations.NonNull;
import com.alflabs.dagger.ActivityQualifier;
import dagger.Module;
import dagger.Provides;

@Module
@SuppressWarnings("WeakerAccess")
public class ActivityContextModule {

    @NonNull
    private final Context mContext;

    public ActivityContextModule(@NonNull Context context) {
        mContext = context;
    }

    @Provides
    @ActivityQualifier
    @NonNull
    public Context providesContext() {
        return mContext;
    }
}
