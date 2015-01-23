package com.impecabel.photoup;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by x00881 on 21-01-2015.
 */
public class NestedPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    public static final int NESTED_SCREEN_SERVERS_KEY = 1;
    public static final int NESTED_SCREEN_ADD_SERVER_KEY = 2;

    private static final String TAG_KEY = "NESTED_KEY";


    public static NestedPreferenceFragment newInstance(int key) {
        NestedPreferenceFragment fragment = new NestedPreferenceFragment();
        // supply arguments to bundle.
        Bundle args = new Bundle();
        args.putInt(TAG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        checkPreferenceResource();
    }

    private void checkPreferenceResource() {
        int key = getArguments().getInt(TAG_KEY);
        // Load the preferences from an XML resource
        switch (key) {
            case NESTED_SCREEN_SERVERS_KEY:
                addPreferencesFromResource(R.xml.server_list_preferences);
                Preference preference = findPreference(PrefUtils.KEY_ADD_SERVER);
                preference.setOnPreferenceClickListener(this);
                break;

               case NESTED_SCREEN_ADD_SERVER_KEY:
                    addPreferencesFromResource(R.xml.http_server_preferences);
                    break;

            default:
                break;
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // here you should use the same keys as you used in the xml-file
        if (preference.getKey().equals(PrefUtils.KEY_ADD_SERVER)) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.prefs_add_server)
                    .items(R.array.addServerModes)
                    .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                           if (which == 1) {
                               SettingsFragment.mCallback.onNestedPreferenceSelected(NestedPreferenceFragment.NESTED_SCREEN_ADD_SERVER_KEY);
                           }
                        }
                    })
                    .positiveText(R.string.choose)
                    .show();

        }
        return false;

    }

}

