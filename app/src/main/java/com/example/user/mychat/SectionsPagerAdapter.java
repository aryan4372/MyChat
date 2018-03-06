package com.example.user.mychat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by USER on 22-01-2018.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Fragment getItem(int position)
    {
        switch(position)
        {
            case 0:
                RequestFragment rf=new RequestFragment();
                return rf;

            case 1:
                ChatFragment cf=new ChatFragment();
                return cf;

            case 2:
                FriendsFragment ff=new FriendsFragment();
                return ff;

            default:
                return null;
        }
    }

    public int getCount()
    {
        return 3;
    }

    public CharSequence getPageTitle(int position)
    {
        switch(position)
        {
            case 0:
                return "Requests";

            case 1:
                return "Chats";

            case 2:
                return "Friends";

            default:
                return null;
        }
    }
}
