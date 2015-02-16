package com.impecabel.photoup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alexbbb.uploadservice.NameValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jrodrigues on 21-01-2015.
 */
public class NestedPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "NestedPrefFragment";
    public static final int NESTED_SCREEN_SERVERS_KEY = 1;
    public static final int NESTED_SCREEN_ADD_SERVER_KEY = 2;
    public static final int NESTED_SCREEN_HEADERS_KEY = 3;
    public static final int NESTED_SCREEN_PARAMETERS_KEY = 4;
    public static final int NEW = -1;
    public static final String SERVER_KEY_PREFIX = "SERVER_";
    public static final String HEADER_KEY_PREFIX = "HEADER_";
    public static final String PARAMETER_KEY_PREFIX = "PARAMETER_";




    private static final String TAG_KEY = "NESTED_KEY";
    private static final String TAG_SERVER_ID = "SERVER_ID";
    private ArrayList<UploadServer> uploadServers = new ArrayList<UploadServer>();
    private UploadServer selectedUploadServer;

    private boolean editingServer = false;

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

                saveButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {

                        int serverID = getCurrentServerId();
                        Log.d(TAG, "ServerID: " + serverID);
                        PrefUtils.saveServer(getActivity(), serverID);
                        getFragmentManager().popBackStack();

                    }
                });

                if (getCurrentServerId() != PrefUtils.NEW_SERVER) {
                    Button deleteButton = new Button(getActivity(), null, android.R.attr.borderlessButtonStyle);
                    deleteButton.setText(R.string.delete);
                    deleteButton.setLayoutParams(params);
                    deleteButton.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            new MaterialDialog.Builder(getActivity())
                                    .content(R.string.confirm_delete_server)
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create_qr) {
            try {
                int serverID = getCurrentServerId();
                Log.d(TAG, "Generate QR ServerID: " + serverID);
                serverID = PrefUtils.saveServer(getActivity(), serverID);
                uploadServers = PrefUtils.getUploadServers(getActivity());
                Bitmap mQRBitmap = QRCodeHelper.createBitmapQR(new Gson().toJson(uploadServers.get(serverID)));

                Intent intent = new Intent(getActivity(), ShowQRActivity.class);
                intent.putExtra("QRBitmapImage", mQRBitmap);
                startActivity(intent);

               /* Uri serverQRUri = QRCodeHelper.createQRCode(getActivity(), new Gson().toJson(uploadServers.get(serverID)));
                if (serverQRUri != null) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, serverQRUri);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, "Share QR using"));
                } else {
                    throw new Exception("Invalid URI");
                }*/
            } catch (Exception e){
                Log.e(TAG, "Error generating QR");
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                editingServer = false;
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
                Intent intent = getActivity().getIntent();
                if (intent.getBooleanExtra("SHOW_SERVER_WIZZARD", false) ){
                    intent.removeExtra("SHOW_SERVER_WIZZARD");
                    this.onPreferenceClick(pref_add_server);
                }
                break;

            case NESTED_SCREEN_ADD_SERVER_KEY:
                if (editingServer == false) {
                    selectedUploadServer = null;
                    if (selectedServerId > NEW) {
                        try {
                            uploadServers = PrefUtils.getUploadServers(getActivity());
                            selectedUploadServer = uploadServers.get(selectedServerId);
                        } catch (Exception e) {
                            Log.w(TAG, "Error getting server!");
                        }
                    }
                }

                if (selectedUploadServer != null || selectedServerId == PrefUtils.NEW_SERVER) {
                    if (editingServer == false) {
                        PrefUtils.loadServerInfo(getActivity(), selectedUploadServer);
                    }
                    editingServer = true;
                    addPreferencesFromResource(R.xml.http_server_preferences);
                    ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

                    int nHeaders = 0;
                    int nParameters = 0;

                    if (selectedServerId == PrefUtils.NEW_SERVER) {
                        actionBar.setTitle(R.string.settings_add_server);
                    } else {
                        actionBar.setTitle(R.string.settings_edit_server);
                        nHeaders = PrefUtils.getHeaders(getActivity()).size();
                        nParameters = PrefUtils.getParameters(getActivity()).size();
                    }

                    Preference pHeaders = findPreference(PrefUtils.KEY_MANAGE_HEADERS);

                    if (nHeaders > 0) {
                        pHeaders.setSummary(nHeaders + " " + ((nHeaders > 1) ? getString(R.string.headers) : getString(R.string.header)));
                    }
                    pHeaders.setOnPreferenceClickListener(this);

                    Preference pParameters = findPreference(PrefUtils.KEY_MANAGE_PARAMETERS);

                    if (nParameters > 0) {
                        pParameters.setSummary(nParameters + " " + ((nParameters > 1) ? getString(R.string.parameters) : getString(R.string.parameter)));
                    }
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
                if (selectedServerId > NEW) {
                    try {
                        headers = PrefUtils.getServerHeaders(getActivity(), selectedServerId);
                    } catch (Exception e) {
                        Log.w(TAG, "Error getting server!");
                    }
                } else {
                    headers = PrefUtils.getHeaders(getActivity());
                }

                if (headers != null){
                    for (int i = 0; i < headers.size(); i++) {
                        final int headerId = i;
                        final NameValue header = headers.get(i);
                        ImageButtonPlusClickPreference pref_header = new ImageButtonPlusClickPreference(getActivity());
                        pref_header.setKey(HEADER_KEY_PREFIX + i);
                        pref_header.setTitle(getString(R.string.name) + ": " + header.getName());
                        pref_header.setSummary(getString(R.string.value) + ": " + header.getValue());
                        pref_header.setWidgetLayoutResource(R.layout.delete_icon);
                        pref_header.setImageButtonClickListener(new ImageButtonPlusClickPreference.ImageButtonPlusClickListener() {
                            @Override
                            public void onImageButtonClick(View view) {
                                new MaterialDialog.Builder(getActivity())
                                        .content(R.string.confirm_delete_header)
                                        .positiveText(R.string.delete)
                                        .negativeText(R.string.cancel)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                ArrayList<NameValue> headers = PrefUtils.getServerHeaders(getActivity(), getCurrentServerId());
                                                if (headers != null) {
                                                    headers.remove(headerId);
                                                    handleSaveHeaders(headers);
                                                }
                                            }
                                        })
                                        .show();
                            }

                            @Override
                            public void onClick(View view) {
                                showAddEditHeaderDialog(headerId, header);
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
                } else {
                    parameters = PrefUtils.getParameters(getActivity());
                }
                if (parameters != null){
                    for (int i = 0; i < parameters.size(); i++) {
                        final int parameterId = i;
                        final NameValue parameter = parameters.get(i);
                        ImageButtonPlusClickPreference pref_parameter = new ImageButtonPlusClickPreference(getActivity());
                        pref_parameter.setKey(PARAMETER_KEY_PREFIX + i);
                        pref_parameter.setTitle(getString(R.string.name) + ": " + parameter.getName());
                        pref_parameter.setSummary(getString(R.string.value) + ": " + parameter.getValue());
                        pref_parameter.setWidgetLayoutResource(R.layout.delete_icon);
                        pref_parameter.setImageButtonClickListener(new ImageButtonPlusClickPreference.ImageButtonPlusClickListener() {
                            @Override
                            public void onImageButtonClick(View view) {
                                new MaterialDialog.Builder(getActivity())
                                        .content(R.string.confirm_delete_parameter)
                                        .positiveText(R.string.delete)
                                        .negativeText(R.string.cancel)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                ArrayList<NameValue> parameters = PrefUtils.getServerParameters(getActivity(), getCurrentServerId());
                                                if (parameters != null) {
                                                    parameters.remove(parameterId);
                                                    handleSaveParameters(parameters);
                                                }
                                            }
                                        })
                                        .show();
                            }

                            @Override
                            public void onClick(View view) {
                                showAddEditParameterDialog(parameterId, parameter);
                            }
                        });

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
                                if (which == 0) {
                                    readServerFromQR();
                                }if (which == 1) {
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

                showAddEditHeaderDialog(NEW, null);

                break;
            case PrefUtils.KEY_ADD_PARAMETER:

                showAddEditParameterDialog(NEW, null);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String serverData = scanResult.getContents();
            Log.d(TAG, "QR Server Data: " + serverData);
            if (serverData != null && serverData != "") {
                try {
                    Gson GSON = new Gson();
                    Type type = new TypeToken<UploadServer>() {
                    }.getType();
                    UploadServer qrUploadServer = GSON.fromJson(serverData, type);
                    PrefUtils.loadServerInfo(getActivity(), qrUploadServer);
                    int serverId = PrefUtils.saveServer(getActivity(), PrefUtils.NEW_SERVER);
                    SettingsFragment.mCallback.onNestedPreferenceSelected(NESTED_SCREEN_ADD_SERVER_KEY, serverId);

                } catch (Exception e) {
                    Toast.makeText(getActivity(), getString(R.string.error_reading_qr), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_reading_qr), Toast.LENGTH_SHORT).show();
        }
    }

    private void readServerFromQR(){

        IntentIntegrator integrator = IntentIntegrator.forFragment(this);
        //integrator.setCaptureLayout(R.layout.custom_capture_layout);
        integrator.setResultDisplayDuration(0);
        integrator.setPrompt("Scan a QR Code");
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);

        integrator.initiateScan();

    }

    private int getCurrentServerId() {
        return getArguments().getInt(TAG_SERVER_ID, -1);
    }

    private void showAddEditHeaderDialog(final int headerId, NameValue header){
        MaterialDialog dialogAddEditHeader = new MaterialDialog.Builder(getActivity())
                .title((headerId == NEW) ? R.string.prefs_add_header : R.string.prefs_edit_header)
                .customView(R.layout.preference_header_param_dialog_view, true)
                .positiveText((headerId == NEW) ? R.string.add : R.string.save)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        ArrayList<NameValue> headers = PrefUtils.getServerHeaders(getActivity(), getCurrentServerId());
                        if (headers == null)
                            headers = new ArrayList<NameValue>();
                        NameValue newHeader = new NameValue(headerNameInput.getText().toString(), headerValueInput.getText().toString());
                        if (headerId == NEW) {
                            headers.add(newHeader);
                        } else {
                            headers.set(headerId, newHeader);
                        }
                        handleSaveHeaders(headers);
                    }

                }).build();


        headerNameInput = (EditText) dialogAddEditHeader.getCustomView().findViewById(R.id.name);
        headerValueInput = (EditText) dialogAddEditHeader.getCustomView().findViewById(R.id.value);

        if (headerId != NEW && header != null){
            //edition time
            headerNameInput.setText(header.getName());
            headerValueInput.setText(header.getValue());
        }

        dialogAddEditHeader.show();
    }

    private void handleSaveHeaders(ArrayList<NameValue> headers){
        PrefUtils.setHeaders(getActivity(), headers);
        if (getCurrentServerId() != NEW) {
            PrefUtils.saveServer(getActivity(), getCurrentServerId());
        }
        //reload the screen to add the new item
        setPreferenceScreen(null);
        checkPreferenceResource();
    }

    private void handleSaveParameters(ArrayList<NameValue> parameters){
        PrefUtils.setParameters(getActivity(), parameters);
        if (getCurrentServerId() != NEW){
            PrefUtils.saveServer(getActivity(), getCurrentServerId());
        }

        //reload the screen to add the new item
        setPreferenceScreen(null);
        checkPreferenceResource();
    }

    private void showAddEditParameterDialog(final int parameterId, NameValue parameter){
        MaterialDialog dialogAddEditParameter = new MaterialDialog.Builder(getActivity())
                .title((parameterId == NEW) ? R.string.prefs_add_parameter : R.string.prefs_edit_parameter)
                .customView(R.layout.preference_header_param_dialog_view, true)
                .positiveText((parameterId == NEW) ? R.string.add : R.string.save)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        ArrayList<NameValue> parameters = PrefUtils.getServerParameters(getActivity(), getCurrentServerId());
                        if (parameters == null)
                            parameters = new ArrayList<NameValue>();
                        NameValue newParameter = new NameValue(parameterNameInput.getText().toString(), parameterValueInput.getText().toString());
                        if (parameterId == NEW) {
                            parameters.add(newParameter);
                        } else {
                            parameters.set(parameterId, newParameter);
                        }
                        handleSaveParameters(parameters);
                    }

                }).build();


        parameterNameInput = (EditText) dialogAddEditParameter.getCustomView().findViewById(R.id.name);
        parameterValueInput = (EditText) dialogAddEditParameter.getCustomView().findViewById(R.id.value);

        if (parameterId != NEW && parameter != null){
            //edition time
            parameterNameInput.setText(parameter.getName());
            parameterValueInput.setText(parameter.getValue());
        }

        dialogAddEditParameter.show();
    }
}