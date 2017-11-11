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

package com.alflabs.rtac.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.app.AppPrefsValues;
import com.alflabs.rtac.app.MainApp;
import com.alflabs.rtac.service.RtacService;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.Utils;
import com.google.common.base.Preconditions;

import javax.inject.Inject;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Inject ILogger mLogger;
    @Inject AppPrefsValues mAppPrefsValues;

    private IMainActivityComponent mComponent;
    private RtacService.LocalBinder mServiceBinder;
    private boolean mServiceBound;

    //----

    private final ServiceConnection mServiceConx = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG) Log.d(TAG, "ServiceConnection::onServiceConnected: " + name);
            mServiceBinder = (RtacService.LocalBinder) service;
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG) Log.d(TAG, "ServiceConnection::onServiceDisconnected: " + name);
            mServiceBinder = null;
            mServiceBound = false;
        }
    };

    //----

    protected IMainActivityComponent createComponent() {
        if (DEBUG) Log.d(TAG, "createComponent");
        return MainApp.getAppComponent(this).create(new ActivityContextModule(this));
    }

    private IMainActivityComponent getComponent() {
        if (DEBUG) Log.d(TAG, "getComponent");
        if (mComponent == null) {
            mComponent = createComponent();
        }
        return mComponent;
    }

    public static IMainActivityComponent getMainActivityComponent(Context context) {
        Preconditions.checkArgument(context instanceof MainActivity);
        return ((MainActivity) context).getComponent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.main_activity);
        setupActionBar();

        getComponent().inject(this);
    }

    @Override
    protected void onStart() {
        if (DEBUG) Log.d(TAG, "onStart");
        super.onStart();
        // FIXME mDataServerMixin.onStart();
    }

    @Override
    protected void onResume() {
        if (DEBUG) Log.d(TAG, "onResume");
        super.onResume();
        bindServerService();
        // FIXME mDataServerMixin.onResume();
        hideNavigationBar();
    }

    @Override
    protected void onPause() {
        if (DEBUG) Log.d(TAG, "onPause");
        // FIXME mDataServerMixin.onPause();
        if (mServiceBinder != null) {
            mServiceBinder.startNotification(this);
        }
        unbindServerService();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (DEBUG) Log.d(TAG, "onStop");
        // FIXME mDataServerMixin.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
        // FIXME mDataServerMixin.onDestroy();
        super.onDestroy();
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_disconnect:
                askDisconnect();
                return true;

            case R.id.action_setup: {
    // FIXME enable SetupActivity or remove
    //            Intent i = new Intent(this, SetupActivity.class);
    //            i.putExtra(SetupActivity.EXTRA_BOOL_SHOW_CALIBRATE, true);
    //            startActivity(i);
                return true;
            }

            case R.id.action_settings: {
                Intent i = new Intent(this, PrefsActivity.class);
                startActivity(i);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    //----

    /** Enable immersion mode to hide the navigation bar */
    private void hideNavigationBar() {
        if (mAppPrefsValues.getSystem_HideNavigation()) {
            final View root = getWindow().getDecorView();
            final int visibility =
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            (Utils.getApiLevel() < 19 ? 0 : View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if (DEBUG) Log.d(TAG, "initial setSystemUiVisibility to " + visibility);
            root.setSystemUiVisibility(visibility);
            root.setOnSystemUiVisibilityChangeListener(newVisibility -> {
                if (DEBUG) Log.d(TAG, "onSystemUiVisibilityChange: visibility=" + newVisibility);
                if ((newVisibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    root.postDelayed(() -> {
                        if (DEBUG) Log.d(TAG, "onSystemUiVisibilityChange: reset visibility to " + visibility);
                        root.setSystemUiVisibility(visibility);
                    }, 3*1000 /*ms*/);
                }
            });
        }
    }

    private void askDisconnect() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Disconnect from server?");
        b.setMessage("Do you want to disconnect from the server?");
        b.setNegativeButton("Cancel", null);
        b.setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                terminateService();
                MainActivity.this.finish();
            }
        });
        b.show();
    }

    private void terminateService() {
        // FIXME mDataServerMixin.stopCnx();
        if (mServiceBinder != null) {
            mServiceBinder.quitService();
        }
    }

    private void bindServerService() {
        Log.d(TAG, "bindServerService: isBound=" + mServiceBound);
        if (!mServiceBound) {
            // Start the service as sticky then bind to it.
            // We need the start command to make the service sticky
            // and we need the binder to communicate with it.
            RtacService.startStickyService(this);

            Intent i = new Intent(this, RtacService.class);
            i.putExtra("activity_class_name", MainActivity.class.getCanonicalName());
            boolean result = bindService(i, mServiceConx, android.app.Service.BIND_AUTO_CREATE);
            Log.d(TAG, "bindService result: " + result);
        }
    }

    private void unbindServerService() {
        Log.d(TAG, "unbindServerService: isBound="+mServiceBound);
        if (mServiceBound) {
            unbindService(mServiceConx);
            mServiceBound = false;
        }
    }

    //----

}
