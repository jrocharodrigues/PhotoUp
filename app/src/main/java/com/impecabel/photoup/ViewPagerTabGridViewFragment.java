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
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.gson.Gson;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

public class ViewPagerTabGridViewFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "PhotoUp";

    private static final int SELECT_PICTURE = 1;

    private FloatingActionButton mFabAdd;
    private ArrayList<GalleryItem> mGalleryItems = new ArrayList<GalleryItem>();
    private int mImageCounter = 0;
    private GridView mPhotoGridView;
    private GridViewAdapter mCustomGridAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_gridview, container, false);

        mFabAdd = (FloatingActionButton) view.findViewById(R.id.fab);
        mFabAdd.setOnClickListener(this);

        mPhotoGridView = (GridView) view.findViewById(R.id.gridView);

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
                            mImageCounter++;
                            ClipData.Item item = clipdata.getItemAt(i);
                            Uri imageUri = item.getUri();

                            //In case you need image's absolute path
                            String imagePath = FileUtils.getPath(getActivity(), imageUri);
                            Toast.makeText(getActivity(), imagePath, Toast.LENGTH_SHORT).show();
                            addGalleryItem(imageUri, imagePath);
                        }
                        mCustomGridAdapter = new GridViewAdapter(getActivity(),
                                R.layout.grid_item, mGalleryItems);
                        mPhotoGridView.setAdapter(mCustomGridAdapter);

                    }
                } else {
                    Uri imageUri = data.getData();
                    mImageCounter++;

                    //In case you need image's absolute path
                    String imagePath = FileUtils.getPath(getActivity(), imageUri);
                    Toast.makeText(getActivity(), imagePath, Toast.LENGTH_SHORT).show();
                    addGalleryItem(imageUri, imagePath);
                    mCustomGridAdapter = new GridViewAdapter(getActivity(),
                            R.layout.grid_item, mGalleryItems);
                    mPhotoGridView.setAdapter(mCustomGridAdapter);
                }
            }
        }

    }

    private void addGalleryItem(Uri fileUri, String filePath) {
        mGalleryItems.add(new GalleryItem(fileUri, filePath));
        /*Gson GSON = new Gson();
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString("gallery_items",  GSON.toJson(galleryItems));
        edit.commit();*/
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.upload_button, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_upload) {
            Toast.makeText(getActivity(), "UPLOAD", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


}
