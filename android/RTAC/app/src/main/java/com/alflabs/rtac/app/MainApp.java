package com.alflabs.rtac.app;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import com.alflabs.annotations.NonNull;
import com.alflabs.rtac.fragment.AutomationFragment;
import com.alflabs.rtac.fragment.IAutomationFragmentComponent;
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
