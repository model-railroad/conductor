package com.alflabs.rtac.app;

import android.content.Context;
import com.alflabs.annotations.NonNull;
import com.alflabs.dagger.AppQualifier;
import com.alflabs.utils.AndroidLogger;
import com.alflabs.utils.ILogger;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
@SuppressWarnings("WeakerAccess")
public class AppContextModule {

    @NonNull
    private final Context mContext;

    public AppContextModule(@NonNull Context context) {
        mContext = context;
    }

    /**
     * Provides an Android context, specifically this one from the app component.
     * Users request it by using the @AppQualifier to distinguish it from the one provided by the activity.
     */
    @NonNull
    @Provides
    @AppQualifier
    public Context providesContext() {
        return mContext;
    }

    /**
     * Provides a singleton instance of the android logger. This method doesn't do any logic
     * to make sure it's a singleton. However in the DaggerIAppComponent, the result is wrapped
     * in a DoubleCheck that will cache and return a singleton value. Because it's a @Singleton
     * it is also app-wide and shared with all sub-components.
     */
    @NonNull
    @Provides
    @Singleton
    public ILogger providesLogger() {
        return new AndroidLogger();
    }
}
