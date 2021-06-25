package com.csci571.koy.hw9.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koyst on 4/16/2018.
 */

/**
 * Code Snippets Borrowed From: Ravi Tamada
 * https://www.androidhive.info/2015/09/android-material-design-working-with-tabs/
 *
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public SectionsPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }


}
