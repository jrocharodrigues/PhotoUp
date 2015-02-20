package com.impecabel.photoup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alexbbb.uploadservice.AbstractUploadServiceReceiver;
import com.alexbbb.uploadservice.UploadService;
import com.google.samples.apps.iosched.ui.widget.SlidingTabLayout;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

/**
 * This is an example of ViewPager + SlidingTab + ListView/ScrollView.
 * This example shows how to handle scroll events for several different fragments.
 * <p/>
 * SlidingTabLayout and SlidingTabStrip are from google/iosched:
 * https://github.com/google/iosched
 */
public class MainActivity extends ActionBarActivity implements ViewPagerTabGridViewFragment.ProgressDialogListener {

    private static final String TAG = "PhotoUpG";

    private View mHeaderView;
    private ViewPager mPager;
    private NavigationAdapter mPagerAdapter;
    private ProgressDialog mUploadProgressDialog;
    private ArrayList<UploadServer> mUploadServers = new ArrayList<>();
    private int mTotalPhotosToUpload = 0;

    private static String sIntentAction;
    private static Bundle sIntentData;

    //UPLOAD SERVICE
    private final AbstractUploadServiceReceiver mUploadReceiver = new AbstractUploadServiceReceiver() {
        private int mCurrentItem = 1;
        @Override
        public void onProgress(String uploadId, int progress, int currentItem) {
            mUploadProgressDialog.setProgress(progress);
            mUploadProgressDialog.setMessage("Uploading file " + currentItem + "/" + mTotalPhotosToUpload);

            Log.i(TAG, "The progress of the upload with ID " + uploadId + " is: " + progress + " currentItem: " + currentItem);
            if (currentItem > mCurrentItem){
                ViewPagerTabGridViewFragment imageGridFragment =
                        (ViewPagerTabGridViewFragment) mPagerAdapter.getRegisteredFragment(0);
                imageGridFragment.removeItem(0);
                mCurrentItem = currentItem;

            }
        }

        @Override
        public void onError(String uploadId, Exception exception) {
            mUploadProgressDialog.dismiss();

            String message = "Error in upload with ID: " + uploadId + ". " + exception.getLocalizedMessage();
            Log.e(TAG, message, exception);
        }

        @Override
        public void onCompleted(String uploadId, int serverResponseCode, String serverResponseMessage) {
            mUploadProgressDialog.dismiss();

            /*SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.remove("gallery_items");
            edit.commit();
            galleryItems = new ArrayList<GalleryItem>();
            customGridAdapter = new GridViewAdapter(getBaseContext(),
                    R.layout.grid_item, galleryItems);
            gridView.setAdapter(customGridAdapter);*/

            ViewPagerTabGridViewFragment imageGridFragment =
                    (ViewPagerTabGridViewFragment) mPagerAdapter.getRegisteredFragment(0);
            imageGridFragment.clearImageGrid();
            String message = "Upload with ID " + uploadId + " is completed: " + serverResponseCode + ", "
                    + serverResponseMessage;
            Log.i(TAG, message);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UploadService.NAMESPACE = "com.impecabel.photup";

        mUploadProgressDialog = new ProgressDialog(this);
        mUploadProgressDialog.setTitle("Uploading Images ...");
        mUploadProgressDialog.setMessage("Upload in progress ...");
        mUploadProgressDialog.setProgressStyle(mUploadProgressDialog.STYLE_HORIZONTAL);
        mUploadProgressDialog.setProgress(0);
        mUploadProgressDialog.setMax(100);
        mUploadProgressDialog.setProgressNumberFormat(null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mHeaderView = findViewById(R.id.header);
        ViewCompat.setElevation(mHeaderView, getResources().getDimension(R.dimen.toolbar_elevation));
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mPager);

        Intent intent = getIntent();
        sIntentAction = intent.getAction();
        String type = intent.getType();

        if ((Intent.ACTION_SEND.equals(sIntentAction)
                || Intent.ACTION_SEND_MULTIPLE.equals(sIntentAction)) && type != null) {
            if (type.startsWith("image/")) {
                sIntentData = intent.getExtras();

            }
        }

        mUploadServers = PrefUtils.getUploadServers(this);

        if (mUploadServers.size() == 0){
            showNoServerFoundDialog(this);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mUploadReceiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUploadReceiver.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, 1);
        } else if (id == R.id.action_tabs) {
            Intent i = new Intent(this, SoonToBeMainActivity.class);
            startActivity(i);
        } else if (id == R.id.action_tabs2) {
            Intent i = new Intent(this, MainActivity2.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showProgressDialog(int totalPhotosToUpload) {
        mTotalPhotosToUpload = totalPhotosToUpload;
        mUploadProgressDialog.setMessage("Uploading file 1/" + mTotalPhotosToUpload);
        mUploadProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        mUploadProgressDialog.dismiss();
    }

    private void showNoServerFoundDialog(final Context context){
        new MaterialDialog.Builder(this)
                .title(R.string.no_server_configured)
                .content(R.string.add_new_server)
                .positiveText(R.string.add)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent i = new Intent(context, SettingsActivity.class);
                        i.putExtra("SHOW_SERVER_WIZARD", true);
                        startActivity(i);

                    }
                })
                .show();
    }



    public static class NavigationAdapter extends SmartFragmentStatePagerAdapter {
        private static final String[] TITLES = new String[]{"Upload", "History"};

        private int mScrollY;

        public void setScrollY(int scrollY) {
            mScrollY = scrollY;
        }

        public NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            // Initialize fragments.
            // Please be sure to pass scroll position to each fragments using setArguments.
            Fragment f;
            switch (position) {
                case 0: {

                    f = new ViewPagerTabGridViewFragment();
                    if (sIntentData != null) {
                        Bundle args = sIntentData;
                        args.putString(ViewPagerTabGridViewFragment.ARG_INTENT_ACTION, sIntentAction);
                        f.setArguments(sIntentData);
                    }
                    break;
                }
                case 1: {
                    f = new ViewPagerTabListViewFragment();
                    if (0 < mScrollY) {
                        Bundle args = new Bundle();
                        args.putInt(ViewPagerTabListViewFragment.ARG_INITIAL_POSITION, 1);
                        f.setArguments(args);
                    }
                    break;
                }
                case 2:
                default: {
                    f = new ViewPagerTabRecyclerViewFragment();
                    if (0 < mScrollY) {
                        Bundle args = new Bundle();
                        args.putInt(ViewPagerTabRecyclerViewFragment.ARG_INITIAL_POSITION, 1);
                        f.setArguments(args);
                    }
                    break;
                }
            }
            return f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }


    }

}