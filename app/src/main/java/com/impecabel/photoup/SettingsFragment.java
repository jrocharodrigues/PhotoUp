package com.impecabel.photoup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;

/**
 * Created by x00881 on 21-01-2015.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener  {

    public static Callback mCallback;

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
        } else {
            throw new IllegalStateException("Owner must implement Callback interface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSimplePreferencesScreen();
        PrefUtils.registerOnSharedPreferenceChangeListener(getActivity(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PrefUtils.unregisterOnSharedPreferenceChangeListener(getActivity(), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.settings_title);
        PrefUtils.registerOnSharedPreferenceChangeListener(getActivity(), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PrefUtils.unregisterOnSharedPreferenceChangeListener(getActivity(), this);
    }

    private void setupSimplePreferencesScreen() {
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.preferences);
        // add listeners for non-default actions
        Preference preference = findPreference(PrefUtils.KEY_MANAGE_SERVERS);
        preference.setOnPreferenceClickListener(this);
        PrefUtils.initSummary(getPreferenceScreen());
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // here you should use the same keys as you used in the xml-file
        if (preference.getKey().equals(PrefUtils.KEY_MANAGE_SERVERS)) {
            mCallback.onNestedPreferenceSelected(NestedPreferenceFragment.NESTED_SCREEN_SERVERS_KEY, -1);
        }
        return false;
    }

    public interface Callback {
        public void onNestedPreferenceSelected(int key, int serverId);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PrefUtils.updatePrefSummary(findPreference(key));
    }
}
