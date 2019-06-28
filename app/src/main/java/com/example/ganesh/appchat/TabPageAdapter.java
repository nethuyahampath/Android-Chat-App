package com.example.ganesh.appchat;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Ganesh on 9/21/2018.
 */

public class TabPageAdapter extends FragmentStatePagerAdapter {

    String[] tabArray=new String[]{"Chats","Requests","Friends"};
    Integer tabNumber=3;

    public TabPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabArray[position];
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                ChatsFragment chatsFragment=new ChatsFragment();
                return chatsFragment;
            case 1:
                RequestsFragment requestsFragment=new RequestsFragment();
                return requestsFragment;
            case 2:
                FriendsFragment friendsFragment=new FriendsFragment();
                return friendsFragment;

        }

        return null;

    }

    @Override
    public int getCount() {
        return tabNumber;
    }
}
