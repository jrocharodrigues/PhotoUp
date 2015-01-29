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

    public static ArrayList<UploadServer> uploadServers = new ArrayList<UploadServer>();

    public static final String KEY_MANAGE_SERVERS = "manage_servers";
    public static final String KEY_ADD_SERVER = "add_server";


    /**
     * JSON  representation of an ArrayList<UploadServer> object that holds the configuration
     * of the upload servers.
     */
    public static final String PREF_UPLOAD_SERVERS = "upload_servers";

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
     * Integer preference that indicates what conference year the application is configured
     * for. Typically, if this isn't an exact match, preferences should be wiped to re-run
     * setup.
     */
    public static final String PREF_CONFERENCE_YEAR = "pref_conference_year";

    /**
     * Boolean indicating whether we should attempt to sign in on startup (default true).
     */
    public static final String PREF_USER_REFUSED_SIGN_IN = "pref_user_refused_sign_in";

    /**
     * Boolean indicating whether the debug build warning was already shown.
     */
    public static final String PREF_DEBUG_BUILD_WARNING_SHOWN = "pref_debug_build_warning_shown";

    /** Boolean indicating whether ToS has been accepted */
    public static final String PREF_TOS_ACCEPTED = "pref_tos_accepted";

    /** Boolean indicating whether ToS has been accepted */
    public static final String PREF_DECLINED_WIFI_SETUP = "pref_declined_wifi_setup";

    /** Boolean indicating whether user has answered if they are local or remote. */
    public static final String PREF_ANSWERED_LOCAL_OR_REMOTE = "pref_answered_local_or_remote";

    /** Boolean indicating whether the user dismissed the I/O extended card. */
    public static final String PREF_DISMISSED_IO_EXTENDED_CARD = "pref_dismissed_io_extended_card";

    /** Boolean indicating whether the user has enabled BLE on the Nearby screen. */
    public static final String PREF_BLE_ENABLED = "pref_ble_enabled";

    /** Long indicating when a sync was last ATTEMPTED (not necessarily succeeded) */
    public static final String PREF_LAST_SYNC_ATTEMPTED = "pref_last_sync_attempted";

    /** Long indicating when a sync last SUCCEEDED */
    public static final String PREF_LAST_SYNC_SUCCEEDED = "pref_last_sync_succeeded";

    /** Sync interval that's currently configured */
    public static final String PREF_CUR_SYNC_INTERVAL = "pref_cur_sync_interval";

    /** Sync sessions with local calendar*/
    public static final String PREF_SYNC_CALENDAR  = "pref_sync_calendar";

    /**
     * Boolean indicating whether we performed the (one-time) welcome flow.
     */
    public static final String PREF_WELCOME_DONE = "pref_welcome_done";

    /** Boolean indicating if we can collect and Analytics */
    public static final String PREF_ANALYTICS_ENABLED = "pref_analytics_enabled";

    /** Boolean indicating whether to show session reminder notifications */
    public static final String PREF_SHOW_SESSION_REMINDERS = "pref_show_session_reminders";

    /** Boolean indicating whether to show session feedback notifications */
    public static final String PREF_SHOW_SESSION_FEEDBACK_REMINDERS
            = "pref_show_session_feedback_reminders";

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
    }





    public static void markUserRefusedSignIn(final Context context) {
        markUserRefusedSignIn(context, true);
    }

    public static void markUserRefusedSignIn(final Context context, final boolean refused) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_USER_REFUSED_SIGN_IN, refused).commit();
    }

    public static boolean hasUserRefusedSignIn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_USER_REFUSED_SIGN_IN, false);
    }

    public static boolean wasDebugWarningShown(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DEBUG_BUILD_WARNING_SHOWN, false);
    }

    public static void markDebugWarningShown(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DEBUG_BUILD_WARNING_SHOWN, true).commit();
    }

    public static boolean isTosAccepted(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_TOS_ACCEPTED, false);
    }

    public static void markTosAccepted(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_TOS_ACCEPTED, true).commit();
    }

    public static boolean hasDeclinedWifiSetup(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DECLINED_WIFI_SETUP, false);
    }

    public static void markDeclinedWifiSetup(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DECLINED_WIFI_SETUP, true).commit();
    }

    public static boolean hasAnsweredLocalOrRemote(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ANSWERED_LOCAL_OR_REMOTE, false);
    }

    public static void markAnsweredLocalOrRemote(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_ANSWERED_LOCAL_OR_REMOTE, true).commit();
    }

    public static boolean hasDismissedIOExtendedCard(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DISMISSED_IO_EXTENDED_CARD, false);
    }

    public static void markDismissedIOExtendedCard(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DISMISSED_IO_EXTENDED_CARD, true).commit();
    }

    public static boolean hasEnabledBle(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_BLE_ENABLED, false);
    }

    public static void setBleStatus(final Context context, boolean status) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_BLE_ENABLED, status).commit();
    }

    public static boolean isWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    public static void markWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, true).commit();
    }

    public static long getLastSyncAttemptedTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_ATTEMPTED, 0L);
    }

    public static void markSyncAttemptedNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //sp.edit().putLong(PREF_LAST_SYNC_ATTEMPTED, UIUtils.getCurrentTime(context)).commit();
    }

    public static long getLastSyncSucceededTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_SUCCEEDED, 0L);
    }

    public static void markSyncSucceededNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        //sp.edit().putLong(PREF_LAST_SYNC_SUCCEEDED, UIUtils.getCurrentTime(context)).commit();
    }



    public static boolean isAnalyticsEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ANALYTICS_ENABLED, true);
    }

    public static boolean shouldShowSessionReminders(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SHOW_SESSION_REMINDERS, true);
    }

    public static boolean shouldShowSessionFeedbackReminders(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SHOW_SESSION_FEEDBACK_REMINDERS, true);
    }

    public static long getCurSyncInterval(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_CUR_SYNC_INTERVAL, 0L);
    }

    public static void setCurSyncInterval(final Context context, long interval) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_CUR_SYNC_INTERVAL, interval).commit();
    }

    public static boolean shouldSyncCalendar(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_SYNC_CALENDAR, false);
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
        } else {
            //it's a new server
            cleanServerSettings(context);
        }
    }
}
