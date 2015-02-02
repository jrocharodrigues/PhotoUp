/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.impecabel.photoup;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;


import com.alexbbb.uploadservice.NameValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;


/**
 * Utilities and constants related to app preferences.
 */
public class PrefUtils {

    private static final String TAG = "PrefUtils";

    public static final int NEW_SERVER = -1;

    public static ArrayList<UploadServer> uploadServers = new ArrayList<UploadServer>();

    /**
     * This are just keys for preferences that act like buttons
     * No getters or setters
     */
    public static final String KEY_MANAGE_SERVERS = "manage_servers";
    public static final String KEY_ADD_SERVER = "add_server";
    public static final String KEY_MANAGE_HEADERS = "manage_headers";
    public static final String KEY_ADD_HEADER = "add_header";
    public static final String KEY_MANAGE_PARAMETERS = "manage_parameters";
    public static final String KEY_ADD_PARAMETER = "add_parameter";


    /**
     * JSON  representation of an ArrayList<UploadServer> object that holds the configuration
     * of the upload servers.
     */
    public static final String PREF_UPLOAD_SERVERS = "pref_upload_servers";

    /**
     * String preference that holds the URL of the server side script that will handle
     * the multipart form upload.
     */
    public static final String PREF_SERVER_URL = "pref_server_url";

    /**
     * String preference that holds the name of the HTTP method to use
     */
    public static final String PREF_HTTP_METHOD = "pref_http_method";

    /**
     * String preference that holds Form parameter that will contain file's data.
     */
    public static final String PREF_FILE_PARAMETER = "pref_file_parameter";

    /**
     * JSON  representation of an ArrayList<NameValue> object that holds the headers
     * to be sent on the request.
     */
    public static final String PREF_HEADERS = "pref_headers";

    /**
     * JSON  representation of an ArrayList<NameValue> object that holds the parameters
     * to be sent on the request.
     */
    public static final String PREF_PARAMETERS = "pref_parameters";



    public static ArrayList<UploadServer> getUploadServers (final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson GSON = new Gson();
        Type type = new TypeToken<List<UploadServer>>() {}.getType();
        ArrayList<UploadServer> returnArray = GSON.fromJson(sp.getString(PREF_UPLOAD_SERVERS, ""), type);
        if (returnArray == null)
            return new ArrayList<UploadServer>();
        return returnArray;
    }

    public static void setUploadServers (final Context context, ArrayList<UploadServer> uploadServers) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson GSON = new Gson();
        sp.edit().putString(PREF_UPLOAD_SERVERS, GSON.toJson(uploadServers)).commit();
    }

    public static void changeServerEnabled(final Context context, int serverId, boolean isEnabled) {
        uploadServers = getUploadServers(context);
        UploadServer uploadServer = uploadServers.get(serverId);
        uploadServer.setEnabled(isEnabled);
        uploadServers.set(serverId, uploadServer);
        setUploadServers(context, uploadServers);
    }

    public static int saveServer(final Context context, int serverId){
        int returnId;
        uploadServers = PrefUtils.getUploadServers(context);
        UploadServer uploadServer = new UploadServer(getServerURL(context),
                getHTTPMethod(context),
                getFileParameter(context),
                "HTTP",
                getHeaders(context),
                getParameters(context)
        );
        Gson GSON = new Gson();
        Log.d(TAG,  GSON.toJson(uploadServer));

        if (serverId == NEW_SERVER) {
            uploadServers.add(uploadServer);
            returnId = uploadServers.size() - 1;
        } else {
            uploadServers.set(serverId, uploadServer);
            returnId = serverId;
        }
        setUploadServers(context, uploadServers);

        return returnId;
    }

    public static void setServerURL(final Context context, String serverURL) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_SERVER_URL, serverURL).commit();
    }

    public static String getServerURL(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_SERVER_URL, "");
    }

    public static void setHTTPMethod(final Context context, String HTTPMethod) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_HTTP_METHOD, HTTPMethod).commit();
    }

    public static String getHTTPMethod(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_HTTP_METHOD, "");
    }

    public static void setFileParameter(final Context context, String fileParameter) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_FILE_PARAMETER, fileParameter).commit();
    }

    public static String getFileParameter(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_FILE_PARAMETER, "");
    }

    public static void cleanServerSettings(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().remove(PREF_SERVER_URL).commit();
        sp.edit().remove(PREF_HTTP_METHOD).commit();
        sp.edit().remove(PREF_FILE_PARAMETER).commit();
        sp.edit().remove(PREF_HEADERS).commit();
        sp.edit().remove(PREF_PARAMETERS).commit();
    }

    public static ArrayList<NameValue> getServerHeaders (final Context context, int serverId) {
        ArrayList<NameValue> headers = new ArrayList<NameValue>();
        if (serverId >= 0){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Gson GSON = new Gson();
            Type type = new TypeToken<List<UploadServer>>() {}.getType();
            ArrayList<UploadServer> uploadServers = GSON.fromJson(sp.getString(PREF_UPLOAD_SERVERS, ""), type);
            if (uploadServers != null) {
                try {
                    headers = uploadServers.get(serverId).getHeaders();
                } catch (Exception e){
                    Log.w(TAG, "Error getting headers!");
                }
            }
        }
        return headers;
    }

    public static ArrayList<NameValue> getHeaders (final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson GSON = new Gson();
        Type type = new TypeToken<List<NameValue>>() {}.getType();
        ArrayList<NameValue> returnArray = GSON.fromJson(sp.getString(PREF_HEADERS, ""), type);
        if (returnArray == null)
            return new ArrayList<NameValue>();
        return returnArray;
    }

    public static void setHeaders (final Context context, ArrayList<NameValue> headers) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson GSON = new Gson();
        sp.edit().putString(PREF_HEADERS, GSON.toJson(headers)).commit();
    }

    public static ArrayList<NameValue> getServerParameters (final Context context, int serverId) {
        ArrayList<NameValue> parameters = new ArrayList<NameValue>();
        if (serverId >= 0){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Gson GSON = new Gson();
            Type type = new TypeToken<List<UploadServer>>() {}.getType();
            ArrayList<UploadServer> uploadServers = GSON.fromJson(sp.getString(PREF_UPLOAD_SERVERS, ""), type);
            if (uploadServers != null) {
                try {
                    parameters = uploadServers.get(serverId).getParameters();
                } catch (Exception e){
                    Log.w(TAG, "Error getting parameters!");
                }
            }
        }
        return parameters;
    }

    public static ArrayList<NameValue> getParameters (final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson GSON = new Gson();
        Type type = new TypeToken<List<NameValue>>() {}.getType();
        ArrayList<NameValue> returnArray = GSON.fromJson(sp.getString(PREF_PARAMETERS, ""), type);
        if (returnArray == null)
            return new ArrayList<NameValue>();
        return returnArray;
    }

    public static void setParameters (final Context context, ArrayList<NameValue> parameters) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson GSON = new Gson();
        sp.edit().putString(PREF_PARAMETERS, GSON.toJson(parameters)).commit();
    }

    public static boolean isServerEnabled(final Context context, int serverId) {

        if (serverId != PrefUtils.NEW_SERVER) {
            try {
                uploadServers = PrefUtils.getUploadServers(context);
                UploadServer uploadServer = uploadServers.get(serverId);
                return uploadServer.getEnabled();
            } catch (Exception e) {
                Log.w(TAG, "Error getting info on server!");
            }
        }

        return true;

    }

    public static void registerOnSharedPreferenceChangeListener(final Context context,
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    public static void updatePrefSummary(Preference p) {
        if (p == null)
            return;
        Log.d(TAG, p.getKey());
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            if (listPref.getEntry() != null) {
                p.setSummary(listPref.getEntry());
            }
        }
        else if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (editTextPref.getText() != null) {
                if (p.getTitle().toString().contains("assword") || p.getTitle().toString().contains("PIN")) {
                    p.setSummary("******");
                } else {
                    p.setSummary(editTextPref.getText());
                }
            }
        }
        else if (p instanceof MultiSelectListPreference) {
            // MultiSelectList Preference
            MultiSelectListPreference mlistPref = (MultiSelectListPreference) p;
            String summaryMListPref = "";
            String and = "";

            // Retrieve values
            Set<String> values = mlistPref.getValues();
            for (String value : values) {
                // For each value retrieve index
                int index = mlistPref.findIndexOfValue(value);
                // Retrieve entry from index
                CharSequence mEntry = index >= 0
                        && mlistPref.getEntries() != null ? mlistPref
                        .getEntries()[index] : null;
                if (mEntry != null) {
                    // add summary
                    summaryMListPref = summaryMListPref + and + mEntry;
                    and = ";";
                }
            }
            // set summary
            if (summaryMListPref != null) {
                mlistPref.setSummary(summaryMListPref);
            }
        }
    }

    public static void loadServerInfo(Context context, UploadServer uploadServer) {

        if (uploadServer != null) {
            setServerURL(context, uploadServer.getURL());
            setFileParameter(context, uploadServer.getFileParameterName());
            setHTTPMethod(context, uploadServer.getMethod());
            setHeaders(context, uploadServer.getHeaders());
            setParameters(context, uploadServer.getParameters());
        } else {
            //it's a new server
            cleanServerSettings(context);
        }
    }
}
