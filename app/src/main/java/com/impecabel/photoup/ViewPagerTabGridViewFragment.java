/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.impecabel.photoup;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.alexbbb.uploadservice.ContentType;
import com.alexbbb.uploadservice.UploadRequest;
import com.alexbbb.uploadservice.UploadService;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class ViewPagerTabGridViewFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "PhotoUpGrid";
    private static final int SELECT_PICTURE = 1;

    public static final String ARG_INTENT_ACTION = "ARG_INTENT_ACTION";

    private Context mContext;
    private FloatingActionButton mFabAdd;
    private ArrayList<GalleryItem> mGalleryItems = new ArrayList<GalleryItem>();
    private GridView mPhotoGridView;
    private GridViewAdapter mCustomGridAdapter;

    private ProgressDialogListener mCallback;
    public interface ProgressDialogListener {
        public void showProgressDialog(int nPhotosToUpload);
        public void dismissProgressDialog();
        public void showNoServerFoundDialog(final Context context);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_gridview, container, false);
        mContext = getActivity();

        mFabAdd = (FloatingActionButton) view.findViewById(R.id.fab);
        mFabAdd.setOnClickListener(this);

        mPhotoGridView = (GridView) view.findViewById(R.id.gridView);
        mCustomGridAdapter = new GridViewAdapter(mContext,
                R.layout.grid_item, mGalleryItems);
        mPhotoGridView.setAdapter(mCustomGridAdapter);

        Bundle args = getArguments();
        if (args != null) {
            handleReceivedPhotos(args);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mFabAdd)){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    SELECT_PICTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //TODO check if there is a better way to handle this
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == SELECT_PICTURE) {

                // selectedImagePath = getPath(selectedImageUri);
                //Bitmap bitmap;
                if (Build.VERSION.SDK_INT >= 18 && null == data.getData()) {
                    // retrieve a collection of selected images
                    ClipData clipdata = data.getClipData();
                    // iterate over these images

                    if (clipdata != null) {
                        // construct array

                        for (int i = 0; i < clipdata.getItemCount(); i++) {
                            ClipData.Item item = clipdata.getItemAt(i);
                            Uri imageUri = item.getUri();

                            //In case you need image's absolute path
                            String imagePath = FileUtils.getPath(mContext, imageUri);
                            Toast.makeText(mContext, imagePath, Toast.LENGTH_SHORT).show();
                            addGalleryItem(imageUri, imagePath);
                        }

                    }
                } else {
                    Uri imageUri = data.getData();
                    //In case you need image's absolute path
                    String imagePath = FileUtils.getPath(mContext, imageUri);
                    Toast.makeText(mContext, imagePath, Toast.LENGTH_SHORT).show();
                    addGalleryItem(imageUri, imagePath);

                }
                mCustomGridAdapter.notifyDataSetChanged();
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.upload_button, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_upload) {
            onUploadButtonClick();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ProgressDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    private boolean hasFilesToUpload(){
        return mGalleryItems.size() > 0;
    }

    private void onUploadButtonClick() {
        //final String serverUrlString = Utils.serverURL;
        //final String paramNameString = Utils.parameterName;

        if (!hasFilesToUpload()) return;

        ArrayList<UploadServer> mUploadServers = PrefUtils.getUploadServers(mContext);

        if (mUploadServers.size() > 0) {

            for(UploadServer uploadServer : mUploadServers) {
                if (uploadServer.isEnabled()) {//maybe implement getEnabledServers
                    UploadRequest request = new UploadRequest(mContext, UUID.randomUUID().toString(), uploadServer.getURL());
                    request.setMethod(uploadServer.getMethod());

                    for (GalleryItem itemToUpload : mGalleryItems) {
                        request.addFileToUpload(itemToUpload.getPath(), uploadServer.getFileParameterName(),
                                itemToUpload.getFileName(false), ContentType.APPLICATION_OCTET_STREAM);
                    }

                    request.setNotificationConfig(R.drawable.ic_launcher, getString(R.string.app_name),
                            getString(R.string.uploading) + " to " + uploadServer.getURL(), getString(R.string.upload_success),
                            getString(R.string.upload_error), false);

                    mCallback.showProgressDialog(mGalleryItems.size());

                    try {
                        UploadService.startUpload(request);
                        Log.d(TAG, uploadServer.getURL() + "-" + uploadServer.getFileParameterName());
                    } catch (Exception exc) {
                        mCallback.dismissProgressDialog();
                        Toast.makeText(mContext, "Malformed upload request. " + exc.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            }
        } else {
            mCallback.showNoServerFoundDialog(mContext);
        }
    }

    //SCROLL HANDLER


    private void addGalleryItem(Uri fileUri, String filePath) {
        mGalleryItems.add(new GalleryItem(fileUri, filePath));
        /*Gson GSON = new Gson();
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString("gallery_items",  GSON.toJson(galleryItems));
        edit.commit();*/
    }

    private void handleReceivedPhotos(Bundle args){
        //TODO check why the intent doesn't fire if app already opened
        String intentAction = args.getString(ARG_INTENT_ACTION);
        Log.d(TAG, "Intent Action:" + intentAction);


        if (Intent.ACTION_SEND.equals(intentAction)){
              Uri imageUri = args.getParcelable(Intent.EXTRA_STREAM);

              if (imageUri != null) {
                  String imagePath = FileUtils.getPath(mContext, imageUri);
                  Toast.makeText(mContext, imagePath, Toast.LENGTH_SHORT).show();

                  addGalleryItem(imageUri, imagePath);
              }

        } else if (Intent.ACTION_SEND_MULTIPLE.equals(intentAction))  {
            ArrayList<Uri> imageUris = args
                    .getParcelableArrayList(Intent.EXTRA_STREAM);

            for (Uri imageUri : imageUris){
                String imagePath = FileUtils.getPath(mContext, imageUri);
                Toast.makeText(mContext, imagePath, Toast.LENGTH_SHORT).show();
                addGalleryItem(imageUri, imagePath);
            }

        }
        mCustomGridAdapter.notifyDataSetChanged();
    }

    public void clearImageGrid(){
        Log.d(TAG, "clear: " + mGalleryItems.size());
        mGalleryItems.clear();
        mCustomGridAdapter.notifyDataSetChanged();
    }

    public void removeItem(int id){
        Log.d(TAG, "remove: " + id);
        mGalleryItems.remove(id);
        mCustomGridAdapter.notifyDataSetChanged();
    }

}
