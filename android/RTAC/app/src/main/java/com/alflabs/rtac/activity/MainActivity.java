package com.alflabs.rtac.activity;

import android.app.Activity;
import android.os.Bundle;
import com.alflabs.rtac.R;
import com.alflabs.rtac.app.MainApp;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;

public class MainActivity extends Activity {

    @Inject ILogger mLogger;

    protected IMainActivityComponent createComponent() {
        return MainApp.getAppComponent(this).create(new ActivityContextModule(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        IMainActivityComponent component = createComponent();
        component.inject(this);
    }
}
