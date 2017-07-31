package com.alflabs.rtac.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
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
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.main_activity);
        setupActionBar();

        IMainActivityComponent component = createComponent();
        component.inject(this);
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
                // TODO -- askDisconnect();
                return true;

            case R.id.action_setup: {
    // FIXME enable or remove
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
}
