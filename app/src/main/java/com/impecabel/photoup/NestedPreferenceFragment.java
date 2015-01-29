package com.impecabel.photoup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

/**
 * Created by jrodrigues on 21-01-2015.
 */
public class NestedPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "NestedPreferenceFragment";
    public static final int NESTED_SCREEN_SERVERS_KEY = 1;
    public static final int NESTED_SCREEN_ADD_SERVER_KEY = 2;
    public static final String SERVER_KEY_PREFIX = "SERVER_";

    public static final int NEW_SERVER = -1;
    public static final int INVALID_SERVER = -2;

    private static final String TAG_KEY = "NESTED_KEY";
    private static final String TAG_SERVER_ID = "SERVER_ID";
    private ArrayList<UploadServer> uploadServers = new ArrayList<UploadServer>();

    private Switch swtServerEnabled;

    public static NestedPreferenceFragment newInstance(int key, int serverId) {
        NestedPreferenceFragment fragment = new NestedPreferenceFragment();
        // supply arguments to bundle.
        Bundle args = new Bundle();
        args.putInt(TAG_KEY, key);
        args.putInt(TAG_SERVER_ID, serverId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout v = (LinearLayout) super.onCreateView(inflater, container, savedInstanceState);

        int key = getArguments().getInt(TAG_KEY);

        if (key == NESTED_SCREEN_ADD_SERVER_KEY) {
            setHasOptionsMenu(true);
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.RIGHT);

            Button saveButton = new Button(getActivity(), null, android.R.attr.borderlessButtonStyle);
            saveButton.setText(R.string.save);



            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            //params.gravity = Gravity.RIGHT;
            //params.setMargins(100, 0, 0, 500);
            saveButton.setLayoutParams(params);
            if (getCurrentServerId() != NEW_SERVER) {
                Button deleteButton = new Button(getActivity(), null, android.R.attr.borderlessButtonStyle);
                deleteButton.setText(R.string.delete);
                deleteButton.setLayoutParams(params);
                deleteButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        new MaterialDialog.Builder(getActivity())
                                .content(R.string.confirm_delete)
                                .positiveText(R.string.delete)
                                .negativeText(R.string.cancel)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        int serverID = getCurrentServerId();
                                        Log.d(TAG, "ServerID: " + serverID);
                                        uploadServers = PrefUtils.getUploadServers(getActivity());

                                        if (getCurrentServerId() != NEW_SERVER) {
                                            uploadServers.remove(serverID);

                                        }
                                        PrefUtils.setUploadServers(getActivity(), uploadServers);
                                        getFragmentManager().popBackStack();
                                    }
                                })
                                .show();


                    }
                });

                linearLayout.addView(deleteButton);
            }

            linearLayout.addView(saveButton);

            v.addView(linearLayout);
            saveButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    int serverID = getCurrentServerId();
                    Log.d(TAG, "ServerID: " + serverID);
                    uploadServers = PrefUtils.getUploadServers(getActivity());
                    UploadServer uploadServer = new UploadServer(PrefUtils.getServerURL(getActivity()),
                            PrefUtils.getHTTPMethod(getActivity()),
                            PrefUtils.getFileParameter(getActivity()),
                            "HTTP");
                    if (serverID == NEW_SERVER) {
                        uploadServers.add(uploadServer);

                    } else {
                        uploadServers.set(serverID, uploadServer);
                    }
                    PrefUtils.setUploadServers(getActivity(), uploadServers);
                    getFragmentManager().popBackStack();

                }
            });


        } else {
            setHasOptionsMenu(false);
        }


        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_edit_server, menu);
        //get the switch instance
        swtServerEnabled = (Switch)menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.switchForActionBar);

        swtServerEnabled.setChecked(isCurrentServerEnabled());
        swtServerEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                changeServerEnabled(getCurrentServerId(), isChecked);
            }

        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

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
        setPreferenceScreen(null);
        checkPreferenceResource();
        PrefUtils.initSummary(getPreferenceScreen());
        PrefUtils.registerOnSharedPreferenceChangeListener(getActivity(), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PrefUtils.unregisterOnSharedPreferenceChangeListener(getActivity(), this);
    }

    private void checkPreferenceResource() {
        int key = getArguments().getInt(TAG_KEY);

        // Load the preferences from an XML resource
        switch (key) {
            case NESTED_SCREEN_SERVERS_KEY:
                addPreferencesFromResource(R.xml.server_list_preferences);

                uploadServers = PrefUtils.getUploadServers(getActivity());

                if (uploadServers != null) {

                    for (int i = 0; i < uploadServers.size(); i++) {
                        final int serverId = i;
                        UploadServer tmpUploadServer = uploadServers.get(serverId);
                        SwitchPlusClickPreference sp = new SwitchPlusClickPreference(getActivity());
                        sp.setKey(SERVER_KEY_PREFIX + serverId);
                        sp.setTitle(tmpUploadServer.getURL());
                        sp.setSummary(tmpUploadServer.getType() + " " + tmpUploadServer.getMethod());
                        sp.setChecked(tmpUploadServer.getEnabled());
                        sp.setSwitchClickListener(new SwitchPlusClickPreference.SwitchPlusClickListener() {
                            @Override
                            public void onCheckedChanged(Switch buttonView, boolean isChecked) {
                                //Save the preference value here
                                changeServerEnabled(serverId, isChecked);

                            }

                            @Override
                            public void onClick(View view) {
                                //Launch the new preference screen or activity here
                                SettingsFragment.mCallback.onNestedPreferenceSelected(NestedPreferenceFragment.NESTED_SCREEN_ADD_SERVER_KEY, serverId);

                            }
                        });

                        ((PreferenceScreen) findPreference("server_list")).addPreference(sp);

                    }
                }

                Preference pref_add_server = new Preference(getActivity());
                pref_add_server.setKey(PrefUtils.KEY_ADD_SERVER);
                pref_add_server.setTitle(R.string.prefs_add_server);
                pref_add_server.setSummary(R.string.prefs_add_server_summary);
                pref_add_server.setIcon(R.drawable.ic_action_new_dark);
                pref_add_server.setOnPreferenceClickListener(this);
                ((PreferenceScreen) findPreference("server_list")).addPreference(pref_add_server);

                ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.settings_server_list);

                break;

            case NESTED_SCREEN_ADD_SERVER_KEY:

                int serverId = getCurrentServerId();
                uploadServers = PrefUtils.getUploadServers(getActivity());
                UploadServer selectedUploadServer = null;
                if (serverId >= 0) {
                    try {
                        selectedUploadServer = uploadServers.get(serverId);
                    } catch (Exception e) {
                        Log.w(TAG, "Error getting server!");
                    }
                }
                if (selectedUploadServer != null || serverId == NEW_SERVER) {
                    PrefUtils.loadServerInfo(getActivity(), selectedUploadServer);

                    addPreferencesFromResource(R.xml.http_server_preferences);
                    ActionBar actionBar =  ((ActionBarActivity)getActivity()).getSupportActionBar();


                    if (serverId == NEW_SERVER) {
                        actionBar.setTitle(R.string.settings_add_server);
                    } else {
                        actionBar.setTitle(R.string.settings_edit_server);
                    }




                } else {
                    Toast.makeText(getActivity(), R.string.error_loading_server, Toast.LENGTH_SHORT).show();
                    getFragmentManager().popBackStack();
                }

                break;

            default:
                break;
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        // here you should use the same keys as you used in the xml-file
        String key = preference.getKey();
        Log.d(TAG, key);
        if (key.equals(PrefUtils.KEY_ADD_SERVER)) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.prefs_add_server)
                    .items(R.array.addServerModes)
                    .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (which == 1) {
                                SettingsFragment.mCallback.onNestedPreferenceSelected(NestedPreferenceFragment.NESTED_SCREEN_ADD_SERVER_KEY, NEW_SERVER);
                            }
                        }
                    })
                    .positiveText(R.string.choose)
                    .show();

        }


        return false;

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PrefUtils.updatePrefSummary(findPreference(key));
    }

    private void changeServerEnabled(int serverId, boolean isEnabled){
        uploadServers = PrefUtils.getUploadServers(getActivity());
        UploadServer uploadServer = uploadServers.get(serverId);
        uploadServer.setEnabled(isEnabled);
        uploadServers.set(serverId, uploadServer);
        PrefUtils.setUploadServers(getActivity(), uploadServers);
    }

    private int getCurrentServerId(){
        return getArguments().getInt(TAG_SERVER_ID, -1);
    }

    private boolean isCurrentServerEnabled(){
        int serverId = getCurrentServerId();

        if (serverId != NEW_SERVER) {
            try {
                uploadServers = PrefUtils.getUploadServers(getActivity());
                UploadServer uploadServer = uploadServers.get(serverId);
                return uploadServer.getEnabled();
            } catch (Exception e) {
                Log.w(TAG, "Error getting info on server!");
            }
        }

        return true;

    }

}