package com.impecabel.photoup;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alexbbb.uploadservice.AbstractUploadServiceReceiver;
import com.alexbbb.uploadservice.ContentType;
import com.alexbbb.uploadservice.UploadRequest;
import com.alexbbb.uploadservice.UploadService;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.gson.reflect.TypeToken;
import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;

public class MainActivity2 extends ActionBarActivity implements ObservableScrollViewCallbacks {

    private static final String TAG = "PhotoUp";

    private SharedPreferences mSharedPreferences;

    private ArrayList<UploadServer> uploadServers = new ArrayList<UploadServer>();

    private HeaderGridView gridView;
    private GridViewAdapter customGridAdapter;
    private ArrayList<GalleryItem> galleryItems = new ArrayList<GalleryItem>();
    private static final int SELECT_PICTURE = 1;
    private int imageCounter = 0;

    private View mFlexibleSpaceView;
    private View mToolbarView;
    private TextView mTitleView;
    private int mFlexibleSpaceHeight;
    private int mFlexibleSpaceShowFabOffset;
    private View mFab;
    private int mActionBarSize;
    private int mFabMargin;
    private boolean mFabIsShown;
    private int mFlexibleSpaceAndToolbarHeight;
    private Intent settingsIntent;

    private ProgressDialog barProgressDialog;

    private static final String GALLERY_ITEMS = "mediaItems";
    private static final int RESULT_SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bckp);

        UploadService.NAMESPACE = "com.impecabel.photup";

        barProgressDialog = new ProgressDialog(this);
        barProgressDialog.setTitle("Uploading Images ...");
        barProgressDialog.setMessage("Upload in progress ...");
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        mSharedPreferences = getSharedPreferences("wedPrefs", MODE_PRIVATE);

        mFlexibleSpaceView = findViewById(R.id.flexible_space);
        mTitleView = (TextView) findViewById(R.id.title);
        mTitleView.setText(getTitle());
        setTitle(null);
        mToolbarView = findViewById(R.id.toolbar);

        gridView = (HeaderGridView) findViewById(R.id.gridView);
        gridView.setScrollViewCallbacks(this);
        mActionBarSize = getActionBarSize();
        mFlexibleSpaceHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_height);
        mFlexibleSpaceAndToolbarHeight = mFlexibleSpaceHeight + mActionBarSize;
        mFlexibleSpaceShowFabOffset = mActionBarSize;


        mFlexibleSpaceView.getLayoutParams().height = mFlexibleSpaceAndToolbarHeight;

        // Set padding view for ListView. This is the flexible space.
        View paddingView = new View(this);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                mFlexibleSpaceAndToolbarHeight);
        paddingView.setLayoutParams(lp);

        // This is required to disable header's list selector effect
        paddingView.setClickable(true);

        gridView.addHeaderView(paddingView);

        mFab = findViewById(R.id.fab);
        mFabMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        ViewHelper.setScaleX(mFab, 0);
        ViewHelper.setScaleY(mFab, 0);

        ViewTreeObserver vto = mTitleView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mTitleView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mTitleView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                updateFlexibleSpaceText(gridView.getCurrentScrollY());
            }
        });

        if (savedInstanceState==null) {
            String galleryItemsGson = mSharedPreferences.getString("gallery_items", null);


            if (galleryItemsGson != null) {
                try {
                    Gson GSON = new Gson();

                    Type type = new TypeToken<List<GalleryItem>>() {}.getType();
                    galleryItems = GSON.fromJson(galleryItemsGson, type);
                    customGridAdapter = new GridViewAdapter(this,
                            R.layout.grid_item, galleryItems);
                    gridView.setAdapter(customGridAdapter);
                }catch (Exception e) {
                    Log.e(TAG, "Error loading files form preferences - " + e.getMessage() );
                    SharedPreferences.Editor edit = mSharedPreferences.edit();
                    edit.remove("gallery_items");
                    edit.commit();

                }
            }
        }else{
            Gson GSON = new Gson();
            Type type = new TypeToken<List<GalleryItem>>() {}.getType();
            galleryItems = GSON.fromJson(savedInstanceState.getString(GALLERY_ITEMS), type);
            customGridAdapter = new GridViewAdapter(this,
                    R.layout.grid_item, galleryItems);
            gridView.setAdapter(customGridAdapter);
        }

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        // gridView = (HeaderGridView) findViewById(R.id.gridView);


        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent);
                customGridAdapter = new GridViewAdapter(this,
                        R.layout.grid_item, galleryItems);
                gridView.setAdapter(customGridAdapter);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                // Handle multiple images
                // being sent
                handleSendMultipleImages(intent);
                customGridAdapter = new GridViewAdapter(this,
                        R.layout.grid_item, galleryItems);
                gridView.setAdapter(customGridAdapter);

            }
        } /*
         * else { // Handle other intents, such as being started from the home
		 * screen
		 *
		 * }
		 */

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");

                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(
                        Intent.createChooser(intent, "Select Picture"),
                        SELECT_PICTURE);

            }
        });

        uploadServers = PrefUtils.getUploadServers(this);

        settingsIntent = new Intent(this, SettingsActivity.class);
        if (uploadServers.size() == 0){
            showNoServerFoundDialog();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        Gson GSON = new Gson();
        savedInstanceState.putString("gallery_items",  GSON.toJson(galleryItems));
    }

    @Override
    protected void onResume() {
        super.onResume();
        uploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uploadReceiver.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.remove("gallery_items");
        edit.commit();
    }


    private void showNoServerFoundDialog(){
        new MaterialDialog.Builder(this)
                .title(R.string.no_server_configured)
                .content(R.string.add_new_server)
                .positiveText(R.string.add)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        settingsIntent.putExtra("SHOW_SERVER_WIZZARD", true);
                        startActivityForResult(settingsIntent, RESULT_SETTINGS);

                    }
                })
                .show();
    }


    //FILE HANDLERs

    private void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

        if (imageUri != null) {
            imageCounter++;
            String imagePath = FileUtils.getPath(this, imageUri);
            Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show();

            addGalleryItem(imageUri, imagePath);
        }

    }

    private void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent
                .getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // construct array
            Iterator<Uri> itr = imageUris.iterator();
            while (itr.hasNext()) {
                imageCounter++;
                Uri imageUri = itr.next();
                String imagePath = FileUtils.getPath(this, imageUri);
                Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show();
                addGalleryItem(imageUri, imagePath);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

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
                            imageCounter++;
                            ClipData.Item item = clipdata.getItemAt(i);
                            Uri imageUri = item.getUri();

                            //In case you need image's absolute path
                            String imagePath = FileUtils.getPath(this, imageUri);
                            Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show();
                            addGalleryItem(imageUri, imagePath);
                        }
                        customGridAdapter = new GridViewAdapter(this,
                                R.layout.grid_item, galleryItems);
                        gridView.setAdapter(customGridAdapter);

                    }
                } else {
                    Uri imageUri = data.getData();
                    imageCounter++;

                    //In case you need image's absolute path
                    String imagePath = FileUtils.getPath(this, imageUri);
                    Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show();
                    addGalleryItem(imageUri, imagePath);
                    customGridAdapter = new GridViewAdapter(this,
                            R.layout.grid_item, galleryItems);
                    gridView.setAdapter(customGridAdapter);
                }

            } else if (requestCode == RESULT_SETTINGS){
                Log.d(TAG, "settings");
            }
        }

    }

    private void addGalleryItem(Uri fileUri, String filePath) {
        galleryItems.add(new GalleryItem(fileUri, filePath));
        Gson GSON = new Gson();
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString("gallery_items",  GSON.toJson(galleryItems));
        edit.commit();
    }

    //MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, RESULT_SETTINGS);
            return true;
        } else if (id == R.id.action_upload) {
            onUploadButtonClick();
            return true;
        } else if (id == R.id.action_tabs){
            Intent i = new Intent(this, SoonToBeMainActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_tabs2){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        return true;
    }

        return super.onOptionsItemSelected(item);
    }

    private boolean hasFilesToUpload(){
        return galleryItems.size() > 0;
    }

    private void onUploadButtonClick() {
        final String serverUrlString = Utils.serverURL;
        GalleryItem itemToUpload;
        final String paramNameString = Utils.parameterName;

        if (!hasFilesToUpload())
            return;

        final UploadRequest request = new UploadRequest(this, UUID.randomUUID().toString(), serverUrlString);

        Iterator<GalleryItem> itr = galleryItems.iterator();

        while (itr.hasNext()) {
            itemToUpload = itr.next();
            request.addFileToUpload(itemToUpload.getPath(), paramNameString, itemToUpload.getFileName(false), ContentType.APPLICATION_OCTET_STREAM);
        }

        request.setNotificationConfig(R.drawable.ic_launcher, getString(R.string.app_name),
                getString(R.string.uploading), getString(R.string.upload_success),
                getString(R.string.upload_error), false);
        barProgressDialog.setProgress(0);
        barProgressDialog.setMax(100);
        barProgressDialog.setMessage("Uploading file 1/" + galleryItems.size());
        barProgressDialog.setProgressNumberFormat(null);
        barProgressDialog.show();


        try {
            UploadService.startUpload(request);
        } catch (Exception exc) {
            barProgressDialog.dismiss();
            Toast.makeText(this, "Malformed upload request. " + exc.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    //SCROLL HANDLER
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        updateFlexibleSpaceText(scrollY);
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    private void updateFlexibleSpaceText(final int scrollY) {
        ViewHelper.setTranslationY(mFlexibleSpaceView, -scrollY);
        int adjustedScrollY = scrollY;
        if (scrollY < 0) {
            adjustedScrollY = 0;
        } else if (mFlexibleSpaceHeight < scrollY) {
            adjustedScrollY = mFlexibleSpaceHeight;
        }
        float maxScale = (float) (mFlexibleSpaceHeight - mToolbarView.getHeight()) / mToolbarView.getHeight();
        float scale = maxScale * ((float) mFlexibleSpaceHeight - adjustedScrollY) / mFlexibleSpaceHeight;

        ViewHelper.setPivotX(mTitleView, 0);
        ViewHelper.setPivotY(mTitleView, 0);
        ViewHelper.setScaleX(mTitleView, 1 + scale);
        ViewHelper.setScaleY(mTitleView, 1 + scale);
        ViewHelper.setTranslationY(mTitleView, ViewHelper.getTranslationY(mFlexibleSpaceView) + mFlexibleSpaceView.getHeight() - mTitleView.getHeight() * (1 + scale));
        int maxTitleTranslationY = mToolbarView.getHeight() + mFlexibleSpaceHeight - (int) (mTitleView.getHeight() * (1 + scale));
        int titleTranslationY = (int) (maxTitleTranslationY * ((float) mFlexibleSpaceHeight - adjustedScrollY) / mFlexibleSpaceHeight);
        ViewHelper.setTranslationY(mTitleView, titleTranslationY);

        // Translate FAB
        int maxFabTranslationY = mFlexibleSpaceAndToolbarHeight - mFab.getHeight() / 2;
        int fabTranslationY = Math.max(mActionBarSize - mFab.getHeight() / 2,
                Math.min(maxFabTranslationY, -scrollY + mFlexibleSpaceAndToolbarHeight - mFab.getHeight() / 2));
        // ViewHelper.setTranslationX(mFab, mToolbarView.getWidth() - mFabMargin - mFab.getWidth());

        ViewHelper.setTranslationX(mFab, mFabMargin);
        ViewHelper.setTranslationY(mFab, fabTranslationY);

        // Show/hide FAB
        if (ViewHelper.getTranslationY(mFab) < mFlexibleSpaceShowFabOffset) {
            hideFab();
        } else {
            showFab();
        }
    }

    private int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(1).scaleY(1).setDuration(200).start();
            mFabIsShown = true;
        }
    }

    private void hideFab() {
        if (mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(0).scaleY(0).setDuration(200).start();
            mFabIsShown = false;
        }
    }

    //UPLOAD SERVICE
    private final AbstractUploadServiceReceiver uploadReceiver = new AbstractUploadServiceReceiver() {

        @Override
        public void onProgress(String uploadId, int progress, int currentItem) {
            barProgressDialog.setProgress(progress);
            barProgressDialog.setMessage("Uploading file " + currentItem + "/" + galleryItems.size());

            Log.i(TAG, "The progress of the upload with ID " + uploadId + " is: " + progress + " currentItem: " + currentItem);
        }

        @Override
        public void onError(String uploadId, Exception exception) {
            barProgressDialog.dismiss();

            String message = "Error in upload with ID: " + uploadId + ". " + exception.getLocalizedMessage();
            Log.e(TAG, message, exception);
        }

        @Override
        public void onCompleted(String uploadId, int serverResponseCode, String serverResponseMessage) {
            barProgressDialog.dismiss();

            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.remove("gallery_items");
            edit.commit();
            galleryItems = new ArrayList<GalleryItem>();
            customGridAdapter = new GridViewAdapter(getBaseContext(),
                    R.layout.grid_item, galleryItems);
            gridView.setAdapter(customGridAdapter);

            String message = "Upload with ID " + uploadId + " is completed: " + serverResponseCode + ", "
                    + serverResponseMessage;
            Log.i(TAG, message);

        }
    };


}
