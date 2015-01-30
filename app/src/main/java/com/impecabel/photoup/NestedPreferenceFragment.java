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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alexbbb.uploadservice.NameValue;

import java.util.ArrayList;

/**
 * Created by jrodrigues on 21-01-2015.
 */
public class NestedPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "NestedPreferenceFragment";
    public static final int NESTED_SCREEN_SERVERS_KEY = 1;
    public static final int NESTED_SCREEN_ADD_SERVER_KEY = 2;
    public static final int NESTED_SCREEN_HEADERS_KEY = 3;
    public static final int NESTED_SCREEN_PARAMETERS_KEY = 4;
    public static final String SERVER_KEY_PREFIX = "SERVER_";
    public static final String HEADER_KEY_PREFIX = "HEADER_";




    private static final String TAG_KEY = "NESTED_KEY";
    private static final String TAG_SERVER_ID = "SERVER_ID";
    private ArrayList<UploadServer> uploadServers = new ArrayList<UploadServer>();

    private EditText headerNameInput;
    private EditText headerValueInput;

    private EditText parameterNameInput;
    private EditText parameterValueInput;

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

        switch(key){
            case NESTED_SCREEN_ADD_SERVER_KEY:
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
                if (getCurrentServerId() != PrefUtils.NEW_SERVER) {
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

                                            if (getCurrentServerId() != PrefUtils.NEW_SERVER) {
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
                        PrefUtils.saveServer(getActivity(), serverID);
                        getFragmentManager().popBackStack();

                    }
                });

                break;
            //case NESTED_SCREEN_HEADERS_KEY:

            default:
                setHasOptionsMenu(false);
                break;
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_edit_server, menu);
        //get the switch instance
        swtServerEnabled = (Switch) menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.switchForActionBar);

        swtServerEnabled.setChecked(PrefUtils.isServerEnabled(getActivity(), getCurrentServerId()));
        swtServerEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                PrefUtils.changeServerEnabled(getActivity(), getCurrentServerId(), isChecked);
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

        int selectedServerId = getCurrentServerId();

        // Load the preferences from an XML resource
        switch (key) {
            case NESTED_SCREEN_SERVERS_KEY:
                addPreferencesFromResource(R.xml.server_list_preferences);
                PreferenceScreen pref_server_list = (PreferenceScreen) findPreference("server_list");

                Preference pref_add_server = new Preference(getActivity());
                pref_add_server.setKey(PrefUtils.KEY_ADD_SERVER);
                pref_add_server.setTitle(R.string.prefs_add_server);
                pref_add_server.setSummary(R.string.prefs_add_server_summary);
                pref_add_server.setIcon(R.drawable.ic_action_new_dark);
                pref_add_server.setOnPreferenceClickListener(this);
                pref_server_list.addPreference(pref_add_server);

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
                                PrefUtils.changeServerEnabled(getActivity(), serverId, isChecked);

                            }

                            @Override
                            public void onClick(View view) {
                                //Launch the new preference screen or activity here
                                SettingsFragment.mCallback.onNestedPreferenceSelected(NestedPreferenceFragment.NESTED_SCREEN_ADD_SERVER_KEY, serverId);

                            }
                        });
                        pref_server_list.addPreference(sp);
                    }
                }
                ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_server_list);

                break;

            case NESTED_SCREEN_ADD_SERVER_KEY:
                UploadServer selectedUploadServer = null;
                if (selectedServerId >= 0) {
                    try {
                        uploadServers = PrefUtils.getUploadServers(getActivity());
                        selectedUploadServer = uploadServers.get(selectedServerId);
                    } catch (Exception e) {
                        Log.w(TAG, "Error getting server!");
                    }
                }
                if (selectedUploadServer != null || selectedServerId == PrefUtils.NEW_SERVER) {
                    PrefUtils.loadServerInfo(getActivity(), selectedUploadServer);

                    addPreferencesFromResource(R.xml.http_server_preferences);
                    ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

                    if (selectedServerId == PrefUtils.NEW_SERVER) {
                        actionBar.setTitle(R.string.settings_add_server);
                    } else {
                        actionBar.setTitle(R.string.settings_edit_server);
                    }

                    Preference pHeaders = findPreference(PrefUtils.KEY_MANAGE_HEADERS);
                    pHeaders.setOnPreferenceClickListener(this);

                    Preference pParameters = findPreference(PrefUtils.KEY_MANAGE_PARAMETERS);
                    pParameters.setOnPreferenceClickListener(this);

                } else {
                    Toast.makeText(getActivity(), R.string.error_loading_server, Toast.LENGTH_SHORT).show();
                    getFragmentManager().popBackStack();
                }

                break;
            case NESTED_SCREEN_HEADERS_KEY:
                addPreferencesFromResource(R.xml.headers_parameters_preferences);
                PreferenceScreen pref_screen_headers = (PreferenceScreen) findPreference("header_parameter_list");

                Preference pref_add_header = new Preference(getActivity());
                pref_add_header.setKey(PrefUtils.KEY_ADD_HEADER);
                pref_add_header.setTitle(R.string.prefs_add_header);
                pref_add_header.setSummary(R.string.prefs_add_header_summary);
                pref_add_header.setIcon(R.drawable.ic_action_new_dark);
                pref_add_header.setOnPreferenceClickListener(this);
                pref_screen_headers.addPreference(pref_add_header);

                ArrayList<NameValue> headers = null;
                if (selectedServerId >= 0) {
                    try {
                        headers = PrefUtils.getServerHeaders(getActivity(), selectedServerId);
                    } catch (Exception e) {
                        Log.w(TAG, "Error getting server!");
                    }
                }

                if (headers != null){
                    for (int i = 0; i < headers.size(); i++) {
                        ImageOnRightPlusClickPreference pref_header = new ImageOnRightPlusClickPreference(getActivity());
                        pref_header.setKey(HEADER_KEY_PREFIX + i);
                        pref_header.setTitle(getString(R.string.name) + ": " + headers.get(i).getName());
                        pref_header.setSummary(getString(R.string.value) + ": " + headers.get(i).getValue());
                        //pref_header.setIcon(R.drawable.ic_action_new_dark);
                        pref_header.setWidgetLayoutResource(R.layout.delete_icon);
                        pref_header.setImageClickListener(new ImageOnRightPlusClickPreference.ImageOnRightPlusClickListener() {
                            @Override
                            public void onImageClick(View view) {
                                Toast.makeText(getActivity(), "image", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getActivity(), "REST", Toast.LENGTH_SHORT).show();
                            }
                        });

                        pref_screen_headers.addPreference(pref_header);
                    }
                }
                ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_header_list);
                break;
            case NESTED_SCREEN_PARAMETERS_KEY:
                addPreferencesFromResource(R.xml.headers_parameters_preferences);
                PreferenceScreen pref_screen_parameters = (PreferenceScreen) findPreference("header_parameter_list");

                Preference pref_add_parameter = new Preference(getActivity());
                pref_add_parameter.setKey(PrefUtils.KEY_ADD_PARAMETER);
                pref_add_parameter.setTitle(R.string.prefs_add_parameter);
                pref_add_parameter.setSummary(R.string.prefs_add_parameter_summary);
                pref_add_parameter.setIcon(R.drawable.ic_action_new_dark);
                pref_add_parameter.setOnPreferenceClickListener(this);
                pref_screen_parameters.addPreference(pref_add_parameter);

                ArrayList<NameValue> parameters = null;
                if (selectedServerId >= 0) {
                    try {
                        parameters = PrefUtils.getServerParameters(getActivity(), selectedServerId);
                    } catch (Exception e) {
                        Log.w(TAG, "Error getting server!");
                    }
                }
                if (parameters != null){
                    for (int i = 0; i < parameters.size(); i++) {
                        Preference pref_parameter = new Preference(getActivity());
                        pref_parameter.setKey(HEADER_KEY_PREFIX + i);
                        pref_parameter.setTitle(getString(R.string.name) + ": " + parameters.get(i).getName());
                        pref_parameter.setSummary(getString(R.string.value) + ": " + parameters.get(i).getValue());
                        //TODO OnClick handler
                        //pref_add_header.setOnPreferenceClickListener(this);
                        pref_screen_parameters.addPreference(pref_parameter);
                    }
                }
                ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_parameter_list);
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

        switch (key) {
            case PrefUtils.KEY_ADD_SERVER:
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.prefs_add_server)
                        .items(R.array.addServerModes)
                        .itemsCallbackSingleChoice(1, new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (which == 1) {
                                    SettingsFragment.mCallback.onNestedPreferenceSelected(NESTED_SCREEN_ADD_SERVER_KEY, PrefUtils.NEW_SERVER);
                                }
                            }
                        })
                        .positiveText(R.string.choose)
                        .show();
                break;
            case PrefUtils.KEY_MANAGE_HEADERS:
                SettingsFragment.mCallback.onNestedPreferenceSelected(NESTED_SCREEN_HEADERS_KEY, getCurrentServerId());
                break;
            case PrefUtils.KEY_MANAGE_PARAMETERS:
                SettingsFragment.mCallback.onNestedPreferenceSelected(NESTED_SCREEN_PARAMETERS_KEY, getCurrentServerId());
                break;
            case PrefUtils.KEY_ADD_HEADER:

                MaterialDialog dialogAddHeader = new MaterialDialog.Builder(getActivity())
                        .title(R.string.prefs_add_header)
                        .customView(R.layout.preference_header_param_dialog_view, true)
                        .positiveText(R.string.add)
                        .negativeText(android.R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {

                                ArrayList<NameValue> headers = PrefUtils.getServerHeaders(getActivity(), getCurrentServerId());
                                if (headers == null)
                                    headers = new ArrayList<NameValue>();
                                headers.add(new NameValue(headerNameInput.getText().toString(), headerValueInput.getText().toString()));
                                PrefUtils.setHeaders(getActivity(), headers);
                                PrefUtils.saveServer(getActivity(), getCurrentServerId());

                                //reload the screen to add the new item
                                setPreferenceScreen(null);
                                checkPreferenceResource();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                            }
                        }).build();

                headerNameInput = (EditText) dialogAddHeader.getCustomView().findViewById(R.id.name);
                headerValueInput = (EditText) dialogAddHeader.getCustomView().findViewById(R.id.value);

                dialogAddHeader.show();
                Log.d(TAG, getCurrentServerId()+"");

                break;
            case PrefUtils.KEY_ADD_PARAMETER:

                MaterialDialog dialogAddParameter = new MaterialDialog.Builder(getActivity())
                        .title(R.string.prefs_add_parameter)
                        .customView(R.layout.preference_header_param_dialog_view, true)
                        .positiveText(R.string.add)
                        .negativeText(android.R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                ArrayList<NameValue> parameters = PrefUtils.getServerParameters(getActivity(), getCurrentServerId());
                                if (parameters == null)
                                    parameters = new ArrayList<NameValue>();

                                parameters.add(new NameValue( parameterNameInput.getText().toString(), parameterValueInput.getText().toString()));
                                PrefUtils.setParameters(getActivity(), parameters);
                                PrefUtils.saveServer(getActivity(), getCurrentServerId());

                                //reload the screen to add the new item
                                setPreferenceScreen(null);
                                checkPreferenceResource();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                            }
                        }).build();

                parameterNameInput = (EditText) dialogAddParameter.getCustomView().findViewById(R.id.name);
                parameterValueInput = (EditText) dialogAddParameter.getCustomView().findViewById(R.id.value);

                dialogAddParameter.show();

                break;
            default:
                break;
        }
        return false;

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PrefUtils.updatePrefSummary(findPreference(key));
    }

    private int getCurrentServerId() {
        return getArguments().getInt(TAG_SERVER_ID, -1);
    }



}