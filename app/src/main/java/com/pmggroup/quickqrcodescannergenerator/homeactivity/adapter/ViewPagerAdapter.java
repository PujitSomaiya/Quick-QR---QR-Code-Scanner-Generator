package com.pmggroup.quickqrcodescannergenerator.homeactivity.adapter;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.pmggroup.quickqrcodescannergenerator.homeactivity.view.ScanQRCodeFragment;
import com.pmggroup.quickqrcodescannergenerator.homeactivity.view.GenerateQRCodeFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private Context myContext;
    int totalTabs;

    public ViewPagerAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
    }

    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ScanQRCodeFragment();
            case 1:
                return new GenerateQRCodeFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
