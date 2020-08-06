package com.nisith.smartchat.Adapters;

import android.view.View;

import com.nisith.smartchat.Fragments.ChatFragment;
import com.nisith.smartchat.Fragments.FriendRequestFragment;
import com.nisith.smartchat.Fragments.FriendsFragment;
import com.nisith.smartchat.Fragments.GroupsFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


public class MyPagerAdapter extends FragmentPagerAdapter {


    public MyPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ChatFragment();
            case 1:
                return new GroupsFragment();
            case 2:
                return new FriendsFragment();

            case 3:
                return new FriendRequestFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Friends";
            case 3:
                return "Request";
            default:
                return null;
        }
    }
}
