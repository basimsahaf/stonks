package com.stonks.android.adapter;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.stonks.android.PortolfioListFragment;

public class HomePagerAdapter extends FragmentPagerAdapter {
    private Context context;
    int numTabs;

    public HomePagerAdapter(Context context, FragmentManager fm, int numTabs) {
        super(fm, numTabs);
        this.context = context;
        this.numTabs = numTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
            case 1:
                PortolfioListFragment portolfioListFragment = new PortolfioListFragment();
                return portolfioListFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
