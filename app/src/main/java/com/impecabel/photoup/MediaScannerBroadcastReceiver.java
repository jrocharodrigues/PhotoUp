package com.impecabel.photoup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alexbbb.uploadservice.ContentType;
import com.alexbbb.uploadservice.UploadRequest;
import com.alexbbb.uploadservice.UploadService;

import java.util.UUID;

/**
 * Created by jrocharodrigues on 08-01-2015.
 */
public class MediaScannerBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "PhotoUpBroadReceiver";

    // Image action
    // Unofficial action, works for most devices but not HTC. See: https://github.com/owncloud/android/issues/6
    private static String NEW_PHOTO_ACTION_UNOFFICIAL = "com.android.camera.NEW_PICTURE";
    // Officially supported action since SDK 14: http://developer.android.com/reference/android/hardware/Camera.html#ACTION_NEW_PICTURE
    private static String NEW_PHOTO_ACTION = "android.hardware.action.NEW_PICTURE";
    // Video action
    // Officially suppoted action since SDK 14: http://developer.android.com/reference/android/hardware/Camera.html#ACTION_NEW_VIDEO
    private static String NEW_VIDEO_ACTION = "android.hardware.action.NEW_VIDEO";

    @Override
    public void onReceive(final Context context, final Intent intent) {

        Log.d(TAG, "Received: " + intent.getAction());
        /*if (intent.getAction().equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION)) {
            handleConnectivityAction(context, intent);
        }else*/ if (intent.getAction().equals(NEW_PHOTO_ACTION_UNOFFICIAL)) {
            handleNewPictureAction(context, intent);
            Log.d(TAG, "UNOFFICIAL processed: com.android.camera.NEW_PICTURE");
        } else if (intent.getAction().equals(NEW_PHOTO_ACTION)) {
            handleNewPictureAction(context, intent);
            Log.d(TAG, "OFFICIAL processed: android.hardware.action.NEW_PICTURE");
        }/* else if (intent.getAction().equals(NEW_VIDEO_ACTION)) {
            Log_OC.d(TAG, "OFFICIAL processed: android.hardware.action.NEW_VIDEO");
            handleNewVideoAction(context, intent);
        }*/ else {
            Log.e(TAG, "Incorrect intent sent: " + intent.getAction());
        }
    }


    private void handleNewPictureAction(Context context, Intent intent) {

        Log.w(TAG, "New photo received");

        if (!instantUploadEnabled(context)) {
            Log.w(TAG, "Instant picture upload disabled, ignoring new picture");
            return;
        }

        Uri imageUri = intent.getData();
        if (imageUri == null) {
            Log.e(TAG, "Couldn't resolve file!");
            return;
        }

        String imagePath = FileUtils.getPath(context, imageUri);
        Log.d(TAG, imagePath + "");

        if (!isOnline(context) || (instantUploadViaWiFiOnly(context) && !isConnectedViaWiFi(context))) {
            Log.w(TAG, "Not online or not connected via WiFi!");
            return;
        }

        GalleryItem itemToUpload = new GalleryItem(imageUri, imagePath);

        doUpload(context, itemToUpload);


    }

    private void doUpload(Context context, GalleryItem itemToUpload){
        final String serverUrlString = Utils.serverURL;
        final String paramNameString = Utils.parameterName;

        if (itemToUpload.getPath() == null)
            return;

        final UploadRequest request = new UploadRequest(context, UUID.randomUUID().toString(), serverUrlString);

        request.setNotificationConfig(R.drawable.ic_launcher, context.getString(R.string.app_name),
                context.getString(R.string.uploading), context.getString(R.string.upload_success),
                context.getString(R.string.upload_error), false);

        request.addFileToUpload(itemToUpload.getPath(), paramNameString, itemToUpload.getFileName(false), ContentType.APPLICATION_OCTET_STREAM);

        try {
            UploadService.startUpload(request);
        } catch (Exception exc) {
            Log.w(TAG, "Malformed upload request. " + exc.getLocalizedMessage());

        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public static boolean isConnectedViaWiFi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI
                && cm.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED;
    }

    public static boolean instantUploadEnabled(Context context) {
        return PrefUtils.isInstantUploadsEnabled(context);
    }

    public static boolean instantUploadViaWiFiOnly(Context context) {
        return PrefUtils.isInstantUploadOnWifiOnlyEnabled(context);
    }

}
