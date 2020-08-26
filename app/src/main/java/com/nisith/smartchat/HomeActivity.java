package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Adapters.MyPagerAdapter;
import com.nisith.smartchat.Fragments.ChatFragment;
import com.nisith.smartchat.Fragments.FriendsFragment;
import com.nisith.smartchat.Fragments.GroupsFragment;
import com.nisith.smartchat.Model.FriendRequest;
import com.nisith.smartchat.Model.UserStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    public interface OnSearchTextChangeListener {
        void onSearchTextChange(String newText, int selectedTabIndex);
    }

    private Toolbar appToolbar;
    private TextView toolbarTextView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference friendRequestRootDatabaseRef;
    private ValueEventListener unseenFriendRequestValueEventListener;
    private View badgeViewForChats, badgeViewForRequests;
    private TextView badgeHeadingTextViewForChats, badgeItemsTextViewForChats;
    private TextView badgeHeadingTextViewForRequests, badgeItemsTextViewForRequests;
    private List<String> unseenFriendRequestKeyList;
    private OnSearchTextChangeListener searchTextChangeListener;
    private FriendsFragment friendsFragment;
    private ChatFragment chatsFragment;
    private GroupsFragment groupsFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeViews();
        setSupportActionBar(appToolbar);
        setTitle("");
        toolbarTextView.setText("Smart Chat");
        setUpTabLayoutWithViewPager();
        //////////////////////////////////////
        searchTextChangeListener = friendsFragment;
        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        unseenFriendRequestKeyList = new ArrayList<>();
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 3){
                    /* Means 'REQUESTS' tab is selected. To hide unseen requests count badge from tab layout,
                      we simply update 'read' field in database to 'true' */
                    if (friendRequestRootDatabaseRef != null && unseenFriendRequestKeyList.size()>0) {
                        Map<String, Object> updateMap = new HashMap<>();
                        String currentUserId = currentUser.getUid();
                        for (String key : unseenFriendRequestKeyList) {
                            updateMap.put(currentUserId + "/" + key + "/" + "read", true);
                        }
                        friendRequestRootDatabaseRef.updateChildren(updateMap);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        toolbarTextView = findViewById(R.id.toolbar_text_view);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
    }

    private void setUpTabLayoutWithViewPager(){
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), PagerAdapter.POSITION_NONE);
        viewPager.setAdapter(myPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        initializeFragmentObjects(myPagerAdapter);
    }

    private void initializeFragmentObjects(MyPagerAdapter myPagerAdapter){
        chatsFragment = (ChatFragment) myPagerAdapter.getItem(0);
        groupsFragment = (GroupsFragment) myPagerAdapter.getItem(1);
        friendsFragment = (FriendsFragment) myPagerAdapter.getItem(2);
    }





    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null){
            openLoginActivity();
            finish();
        }else {
            friendRequestRootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
            //Current user is already logged in
            updateUserStatus(true);
            //Count total unseen friend requests received by the current user and set it in 'requests' tab
            getTotalUnSeenFriendRequest();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentUser != null){
            //Current user is already logged in
            updateUserStatus(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (friendRequestRootDatabaseRef != null && unseenFriendRequestValueEventListener != null){
            friendRequestRootDatabaseRef.child(currentUser.getUid()).removeEventListener(unseenFriendRequestValueEventListener);
        }
    }

    public void setTotalUnreadChatsInTabLayout(String totalUnreadChats, int visibility){
        //Set badge view for 'CHATS' tab
        if (badgeViewForChats == null){
            createBadgeViewForChatsTab();
        }
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null){
            tab.setCustomView(badgeViewForChats);
            badgeHeadingTextViewForChats.setText("CHATS");
            badgeItemsTextViewForChats.setText(totalUnreadChats);
            badgeItemsTextViewForChats.setVisibility(visibility);
        }

    }

    private void createBadgeViewForChatsTab(){
            LayoutInflater layoutInflater = getLayoutInflater();
            badgeViewForChats = layoutInflater.inflate(R.layout.badge_view, null);
            badgeItemsTextViewForChats = badgeViewForChats.findViewById(R.id.badge_items_text_view);
            badgeHeadingTextViewForChats = badgeViewForChats.findViewById(R.id.badge_heading_text_view);
    }

    public int getSelectedTabPosition(){
        //Return the current selected tab position
        int position = 0;
        if (tabLayout != null){
            position = tabLayout.getSelectedTabPosition();
        }
        return position;
    }


    private void updateUserStatus(boolean isOnline){
        Map<String, Object> userStatusMap = new HashMap<>();
        UserStatus userStatus = new UserStatus(isOnline, System.currentTimeMillis());
        DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        userStatusMap.put("users_detail_info" + "/" + currentUser.getUid() + "/" + "userStatus", userStatus);
        //update user's states
        rootDatabaseRef.updateChildren(userStatusMap);
    }


    private void getTotalUnSeenFriendRequest(){
        unseenFriendRequestValueEventListener = friendRequestRootDatabaseRef.child(currentUser.getUid()).orderByChild("read").equalTo(false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int totalUnseenFriendRequest = 0;
                        unseenFriendRequestKeyList.clear();
                        //If total children is '0', then snapshot.exists() will 'false'...
                        if (snapshot.exists()) {
                            for (DataSnapshot mySnapshot : snapshot.getChildren()){
                                FriendRequest friendRequest = mySnapshot.getValue(FriendRequest.class);
                                if (friendRequest != null){
                                    unseenFriendRequestKeyList.add(mySnapshot.getKey());
                                    if (friendRequest.getRequestType().equals(Constant.RECEIVE_REQUEST)){
                                        totalUnseenFriendRequest++;
                                    }
                                }
                            }
                        }
                        //set total unread friend requests count into ''REQUESTS' tab
                        if (totalUnseenFriendRequest != 0) {
                            setTotalUnseenFriendRequestNumberInTabLayout(String.valueOf(totalUnseenFriendRequest), View.VISIBLE);
                        }else {
                            setTotalUnseenFriendRequestNumberInTabLayout(String.valueOf(totalUnseenFriendRequest), View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void setTotalUnseenFriendRequestNumberInTabLayout(String totalUnseenRequests, int visibility){
        //set badge in 'REQUESTS' tab
        if (badgeViewForRequests == null){
            createBadgeViewForRequestsTab();
        }
        TabLayout.Tab tab = tabLayout.getTabAt(3);
        if (tab != null){
            tab.setCustomView(badgeViewForRequests);
            badgeHeadingTextViewForRequests.setText("REQUESTS");
            badgeItemsTextViewForRequests.setText(totalUnseenRequests);
            badgeItemsTextViewForRequests.setVisibility(visibility);
        }
    }


    private void createBadgeViewForRequestsTab(){
        LayoutInflater layoutInflater = getLayoutInflater();
        badgeViewForRequests = layoutInflater.inflate(R.layout.badge_view, null);
        badgeItemsTextViewForRequests = badgeViewForRequests.findViewById(R.id.badge_items_text_view);
        badgeHeadingTextViewForRequests = badgeViewForRequests.findViewById(R.id.badge_heading_text_view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_menu,menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search_view).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                    switch (getSelectedTabPosition()){
                        case 0:
                            //Chats Fragment is selected
                            searchTextChangeListener = chatsFragment;
                            searchTextChangeListener.onSearchTextChange(newText, 0);
                            break;
                        case 1:
                            //Groups Fragment is selected
                            searchTextChangeListener = groupsFragment;
                            searchTextChangeListener.onSearchTextChange(newText, 1);
                            break;
                        case 2:
                            //Friends Fragment is selected
                            searchTextChangeListener = friendsFragment;
                            searchTextChangeListener.onSearchTextChange(newText, 2);
                            break;
                    }
                return true;
            }
        });
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.find_friends:
                Intent intent = new Intent(HomeActivity.this, FindFriendsActivity.class);
                //current user wants to search friends for one to one chat
                intent.putExtra(Constant.SEARCH_FRIENDS_TYPE, Constant.SEARCH_FRIENDS_FOR_ONE_TO_ONE_FRIENDSHIP);
                startActivity(intent);
                break;

            case R.id.Create_group:
                startActivity(new Intent(HomeActivity.this, CreateGroupActivity.class));
                break;

            case R.id.my_profile:
                startActivity(new Intent(HomeActivity.this,ProfileSettingActivity.class));
                break;

            case R.id.privacy_policy:
                Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show();
                break;

            case R.id.log_out:
                firebaseAuth.signOut();
                openLoginActivity();
                finishAffinity();
        }
        return true;
    }


    private void openLoginActivity(){
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
    }

}