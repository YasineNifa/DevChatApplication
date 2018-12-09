package yassine.com.devchatapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Mohammed on 27/11/2018.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter {
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 1 :
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 3 :
                GroupFragment groupFragment = new GroupFragment();
                return groupFragment;
            default :
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Requests";
            case 1:
                return "Chats";
            case 2:
                return "Friends";
            case 3 :
                return "Groups";
            default:
                return null;
        }
    }
}
