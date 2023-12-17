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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.alflabs.dagger.ActivityScope;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.app.AppPrefsValues;
import com.alflabs.rtac.app.MainApp;
import com.alflabs.rtac.fragment.MapFragment;
import com.alflabs.rtac.fragment.PsaTextFragment;
import com.alflabs.rtac.service.RtacService;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.Utils;
import com.google.common.base.Preconditions;

import javax.inject.Inject;

@ActivityScope
public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Inject ILogger mLogger;
    @Inject AppPrefsValues mAppPrefsValues;
    @Inject MotionSensorMixin mMotionSensorMixin;

    private IMainActivityComponent mComponent;
    private RtacService.LocalBinder mServiceBinder;
    private boolean mServiceBound;

    private ViewPager mPager;
    private RtacFragmentAdapter mPagerAdapter;

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
        return MainApp
                .getAppComponent(this)
                .getMainActivityComponentFactory()
                .create(new ActivityContextModule(this));
    }

    IMainActivityComponent getComponent() {
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
        setupTitle();

        getComponent().inject(this);
        mMotionSensorMixin.onCreate();

        // Modify UI insets (normally applied using android:fitsSystemWindows="true" in the view layout
        // in order to cancel the insert that the stable layout leaves in place of the bottom nav bar.
        // Combined with the API 21 translucent nav bar, it allows it to be hidden and shown on *top*
        // of the layout without resizing the layout.
        if (Utils.getApiLevel() >= 21) {
            View root = findViewById(R.id.root);
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                insets = insets.replaceSystemWindowInsets(
                        insets.getSystemWindowInsetLeft(),
                        insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(),
                        0 /* insets.getSystemWindowInsetBottom() */
                );
                return root.onApplyWindowInsets(insets);
            });
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new RtacFragmentAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(mAppPrefsValues.getConductor_StartMapView() ? 1 : 0);
    }

    @Override
    protected void onStart() {
        if (DEBUG) Log.d(TAG, "onStart");
        super.onStart();
        mMotionSensorMixin.onStart();
    }

    @Override
    protected void onResume() {
        if (DEBUG) Log.d(TAG, "onResume");
        super.onResume();
        bindServerService();
        hideNavigationBar();
        setupEstopFragment();
        mMotionSensorMixin.onResume();
    }

    @Override
    protected void onPause() {
        if (DEBUG) Log.d(TAG, "onPause");
        mMotionSensorMixin.onPause();
        if (mServiceBinder != null) {
            mServiceBinder.startNotification(this);
        }
        unbindServerService();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (DEBUG) Log.d(TAG, "onStop");
        mMotionSensorMixin.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
        mMotionSensorMixin.onDestroy();
        super.onDestroy();
    }

    private void setupEstopFragment() {
        findViewById(R.id.estop_fragment).setVisibility(
                mAppPrefsValues.getConductor_ControlEmergencyStop() ? View.VISIBLE : View.GONE);
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setupTitle() {
        setTitle(getTitle() + " v" + BuildConfig.VERSION_NAME);
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
        final int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_disconnect) {
            askDisconnect();
            return true;

        // FIXME enable SetupActivity or remove
        //  } else if (id == R.id.action_setup) {
        //            Intent i = new Intent(this, SetupActivity.class);
        //            i.putExtra(SetupActivity.EXTRA_BOOL_SHOW_CALIBRATE, true);
        //            startActivity(i);
        //            return true;
        } else if (id == R.id.action_settings) {
            Intent i = new Intent(this, PrefsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //----

    /** Enable immersion mode to hide the navigation bar */
    private void hideNavigationBar() {
        if (mAppPrefsValues.getSystem_HideNavigation()) {
            final View root = getWindow().getDecorView();
            final int visibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    (Utils.getApiLevel() < 19 ? 0 : View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            // Note: combine with API 21 style.xml to give the nav bar a translucent background
            // to allow the nav bar to show up on top of the layout without resizing it.
            // Combine with onApplyWindowInsets() above to let the layout cover the nav bar area.

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
        b.setPositiveButton("Disconnect", (dialog, which) -> {
            terminateService();
            MainActivity.this.finish();
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

    /**
     * Simplified Fragment Pager Adapter.
     * For now only deal with 2 fragments: First one is the info fragment and 2nd is the map fragment.
     * <p/>
     * Future extension: allow more than one map. Adapter should start with no maps and map fragments
     * should be added as maps are behind created dynamically. This is not the current case for simplification.
     */
    public static class RtacFragmentAdapter extends FragmentPagerAdapter {

        public RtacFragmentAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
            case 0:
                return PsaTextFragment.newInstance();
            case 1:
                return MapFragment.newInstance();
            default:
                // Should not happen
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case 0:
                return "Information";
            case 1:
                return "Map";
            default:
                // Should not happen
                return null;
            }
        }
    }

}
