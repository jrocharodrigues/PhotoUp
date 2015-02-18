package com.impecabel.photoup;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.google.samples.apps.iosched.ui.widget.SlidingTabLayout;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * This is an example of ViewPager + SlidingTab + ListView/ScrollView.
 * This example shows how to handle scroll events for several different fragments.
 * <p/>
 * SlidingTabLayout and SlidingTabStrip are from google/iosched:
 * https://github.com/google/iosched
 */
public class SoonToBeMainActivity extends ActionBarActivity implements ObservableScrollViewCallbacks {

    private View mHeaderView;
    private View mFlexibleSpaceView;
    private View mToolbarView;
    private TextView mTitleView;
    private int mBaseTranslationY;
    private ViewPager mPager;
    private NavigationAdapter mPagerAdapter;
    private static Context mContext;

    private int mActionBarSize;
    private int mFlexibleSpaceHeight;
    private int mFlexibleSpaceShowFabOffset;
    private int mFlexibleSpaceAndToolbarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_soon_to_be_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mHeaderView = findViewById(R.id.header);
        //ViewCompat.setElevation(mHeaderView, getResources().getDimension(R.dimen.toolbar_elevation));
        mFlexibleSpaceView = findViewById(R.id.flexible_space);
        mTitleView = (TextView) findViewById(R.id.title);
        mTitleView.setText(getTitle());
        setTitle(null);
        mToolbarView = findViewById(R.id.toolbar);
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        mActionBarSize = getActionBarSize();
        mFlexibleSpaceHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_height);
        mFlexibleSpaceAndToolbarHeight = mFlexibleSpaceHeight + mActionBarSize;

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mPager);

        // When the page is selected, other fragments' scrollY should be adjusted
        // according to the toolbar status(shown/hidden)
        slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                propagateToolbarState(toolbarIsShown());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        propagateToolbarState(toolbarIsShown());

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

        ViewTreeObserver vto = mTitleView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mTitleView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mTitleView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                updateFlexibleSpaceText(0, true, true);
            }
        });


//        gridView.addHeaderView(paddingView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

   /* @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        if (dragging) {
            int toolbarHeight = mToolbarView.getHeight();
            float currentHeaderTranslationY = ViewHelper.getTranslationY(mHeaderView);
            if (firstScroll) {
                if (-toolbarHeight < currentHeaderTranslationY) {
                    mBaseTranslationY = scrollY;
                }
            }
            float headerTranslationY = ScrollUtils.getFloat(-(scrollY - mBaseTranslationY), -toolbarHeight, 0);
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewHelper.setTranslationY(mHeaderView, headerTranslationY);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, 1);
            return true;
        }/* else if (id == R.id.action_upload) {
            onUploadButtonClick();
            return true;
        }*/ else if (id == R.id.action_tabs) {
            Intent i = new Intent(this, SoonToBeMainActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_tabs2) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        updateFlexibleSpaceText(scrollY, firstScroll, dragging);
    }
    private void updateFlexibleSpaceText(final int scrollY, boolean firstScroll, boolean dragging) {
        //if (dragging) {
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

           /* int toolbarHeight = mToolbarView.getHeight();
            float currentHeaderTranslationY = ViewHelper.getTranslationY(mHeaderView);
            if (firstScroll) {
                if (-toolbarHeight < currentHeaderTranslationY) {
                    mBaseTranslationY = scrollY;
                }
            }
            float headerTranslationY = ScrollUtils.getFloat(-(scrollY - mBaseTranslationY), -toolbarHeight, 0);
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewHelper.setTranslationY(mHeaderView, headerTranslationY);*/

            int maxFabTranslationY = mFlexibleSpaceAndToolbarHeight;
            int fabTranslationY = Math.max(mActionBarSize,
                Math.min(maxFabTranslationY, -scrollY + mFlexibleSpaceAndToolbarHeight));
        // ViewHelper.setTranslationX(mFab, mToolbarView.getWidth() - mFabMargin - mFab.getWidth());

            ViewHelper.setTranslationX(mHeaderView, 0);
            ViewHelper.setTranslationY(mHeaderView, fabTranslationY);



           /* // Translate FAB
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
            }*/
        //}
    }


    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        mBaseTranslationY = 0;

        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return;
        }
        View view = fragment.getView();
        if (view == null) {
            return;
        }

        // ObservableXxxViews have same API
        // but currently they don't have any common interfaces.
      //  adjustToolbar(scrollState, view);
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

    private void adjustToolbar(ScrollState scrollState, View view) {
        int toolbarHeight = mToolbarView.getHeight();
        final Scrollable scrollView = (Scrollable) view.findViewById(R.id.scroll);
        if (scrollView == null) {
            return;
        }
        int scrollY = scrollView.getCurrentScrollY();
        if (scrollState == ScrollState.DOWN) {
            showToolbar();
        } else if (scrollState == ScrollState.UP) {
            if (toolbarHeight <= scrollY) {
                hideToolbar();
            } else {
                showToolbar();
            }
        } else {
            // Even if onScrollChanged occurs without scrollY changing, toolbar should be adjusted
            if (toolbarIsShown() || toolbarIsHidden()) {
                // Toolbar is completely moved, so just keep its state
                // and propagate it to other pages
                propagateToolbarState(toolbarIsShown());
            } else {
                // Toolbar is moving but doesn't know which to move:
                // you can change this to hideToolbar()
                showToolbar();
            }
        }
    }

    private Fragment getCurrentFragment() {
        return mPagerAdapter.getItemAt(mPager.getCurrentItem());
    }

    private void propagateToolbarState(boolean isShown) {
        if (isShown)
        return;
        int toolbarHeight = mToolbarView.getHeight();

        // Set scrollY for the fragments that are not created yet
        mPagerAdapter.setScrollY(isShown ? 0 : toolbarHeight);

        // Set scrollY for the active fragments
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            // Skip current item
            if (i == mPager.getCurrentItem()) {
                continue;
            }

            // Skip destroyed or not created item
            Fragment f = mPagerAdapter.getItemAt(i);
            if (f == null) {
                continue;
            }

            View view = f.getView();
            if (view == null) {
                continue;
            }
            propagateToolbarState(isShown, view, toolbarHeight);
        }
    }

    private void propagateToolbarState(boolean isShown, View view, int toolbarHeight) {
        Scrollable scrollView = (Scrollable) view.findViewById(R.id.scroll);
        if (scrollView == null) {
            return;
        }
        if (isShown) {
            // Scroll up
            if (0 < scrollView.getCurrentScrollY()) {
                scrollView.scrollVerticallyTo(0);
            }
        } else {
            // Scroll down (to hide padding)
            if (scrollView.getCurrentScrollY() < toolbarHeight) {
                scrollView.scrollVerticallyTo(toolbarHeight);
            }
        }
    }

    private boolean toolbarIsShown() {
        //return ViewHelper.getTranslationY(mHeaderView) == 0;
        return true;
    }

    private boolean toolbarIsHidden() {
        //return ViewHelper.getTranslationY(mHeaderView) == -mToolbarView.getHeight();

        return false;
    }

    private void showToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        if (headerTranslationY != 0) {
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewPropertyAnimator.animate(mHeaderView).translationY(0).setDuration(200).start();
        }
        propagateToolbarState(true);
    }

    private void hideToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        int toolbarHeight = mToolbarView.getHeight();
        if (headerTranslationY != -toolbarHeight) {
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewPropertyAnimator.animate(mHeaderView).translationY(-toolbarHeight).setDuration(200).start();
        }
        propagateToolbarState(false);
    }

    public static Context getContext(){
        return mContext;
    }

    /**
     * This adapter provides two types of fragments as an example.
     * {@linkplain #createItem(int)} should be modified if you use this example for your app.
     */
    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {



        private static final String[] TITLES =
                getContext().getResources().getStringArray(R.array.tabLabels);

                ///new String[]{"Upload", "History"};

        private int mScrollY;

        public NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setScrollY(int scrollY) {
            mScrollY = scrollY;
        }

        @Override
        protected Fragment createItem(int position) {
            // Initialize fragments.
            // Please be sure to pass scroll position to each fragments using setArguments.
            Fragment f;
            switch (position) {
                case 0: {
                    f = new ViewPagerTabScrollViewFragment();
                    if (0 <= mScrollY) {
                        Bundle args = new Bundle();
                        args.putInt(ViewPagerTabScrollViewFragment.ARG_SCROLL_Y, mScrollY);
                        f.setArguments(args);
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
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }
}
