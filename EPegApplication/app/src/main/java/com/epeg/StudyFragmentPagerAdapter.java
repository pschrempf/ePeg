package com.epeg;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class StudyFragmentPagerAdapter extends FragmentStatePagerAdapter {

    public static final String TAG = StudyFragmentPagerAdapter.class.getName();

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public StudyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String fragmentTitle){
        mFragmentList.add(fragment);
        mFragmentTitleList.add(fragmentTitle);
    }

    @Override
    public Fragment getItem(int position) {
        Log.i(TAG, "Current item: " + mFragmentTitleList.get(position));
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
}
