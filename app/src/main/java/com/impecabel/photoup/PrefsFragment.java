package com.impecabel.photoup;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by jrocharodrigues on 12-01-2015.
 */
public class PrefsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}