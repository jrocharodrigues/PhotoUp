package com.impecabel.photoup;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.Window;

/**
 * Created by x00881 on 21-01-2015.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener  {
    /*SharedPreferences.OnSharedPreferenceChangeListener */
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
        // PrefUtils.registerOnSharedPreferenceChangeListener(getActivity(), this);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // PrefUtils.unregisterOnSharedPreferenceChangeListener(getActivity(), this);
    }

    private void setupSimplePreferencesScreen() {
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.preferences);
        // add listeners for non-default actions
        Preference preference = findPreference(PrefUtils.KEY_MANAGE_SERVERS);
        preference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // here you should use the same keys as you used in the xml-file
        if (preference.getKey().equals(PrefUtils.KEY_MANAGE_SERVERS)) {
            mCallback.onNestedPreferenceSelected(NestedPreferenceFragment.NESTED_SCREEN_SERVERS_KEY);
        }
        return false;
    }

    public interface Callback {
        public void onNestedPreferenceSelected(int key);
    }


    /*@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PrefUtils.PREF_SYNC_CALENDAR.equals(key)) {
            Intent intent;
           if (PrefUtils.shouldSyncCalendar(getActivity())) {
                // Add all calendar entries
                intent = new Intent(SessionCalendarService.ACTION_UPDATE_ALL_SESSIONS_CALENDAR);
            } else {
                // Remove all calendar entries
                intent = new Intent(SessionCalendarService.ACTION_CLEAR_ALL_SESSIONS_CALENDAR);
            }

            intent.setClass(getActivity(), SessionCalendarService.class);
            getActivity().startService(intent);
        }
    }*/
}
