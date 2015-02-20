package com.impecabel.photoup;

/**
 * Created by x00881 on 21-01-2015.
 */

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;

/**
 * Activity for customizing app settings.
 */
public class SettingsActivity extends ActionBarActivity implements SettingsFragment.Callback{
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitle(R.string.settings_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }

        Intent intent = getIntent();
        if (intent.getBooleanExtra("SHOW_SERVER_WIZARD", false)){
            Log.d(TAG, "BIMBAAA");
            this.onNestedPreferenceSelected(NestedPreferenceFragment.NESTED_SCREEN_SERVERS_KEY, -1);
        }
    }


    @Override
    public void onBackPressed() {
        // this if statement is necessary to navigate through nested and main fragments
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onNestedPreferenceSelected(int key, int serverId) {
        getFragmentManager().beginTransaction().replace(R.id.container, NestedPreferenceFragment.newInstance(key, serverId), TAG).addToBackStack(TAG).commit();
    }

}
