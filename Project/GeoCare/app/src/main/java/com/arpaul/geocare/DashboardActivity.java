package com.arpaul.geocare;

import android.content.Intent;
import android.support.v4.view.PagerTitleStrip;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.LinearLayout;

import com.arpaul.geocare.fragment.SavedGeoFenceFragment;
import com.arpaul.geocare.fragment.TrackPathFragment;

public class DashboardActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private View llDashboardActivity;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Toolbar toolbar;
    private PagerTitleStrip ptsDashboard;

    @Override
    public void initialize() {
        llDashboardActivity = baseInflater.inflate(R.layout.activity_tabbed,null);
        llBody.addView(llDashboardActivity, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        initialiseControls();

        bindControls();
    }

    private void bindControls(){
//        setTitle("You");
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if(position == 0)
                return TrackPathFragment.newInstance();
            else
                return SavedGeoFenceFragment.newInstance();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Track";
                case 1:
                    return "GeoPoints";
            }
            return null;
        }
    }

    private void initialiseControls(){
//        toolbar = (Toolbar) llDashboardActivity.findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ptsDashboard = (PagerTitleStrip) llDashboardActivity.findViewById(R.id.ptsDashboard);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) llDashboardActivity.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }
}
