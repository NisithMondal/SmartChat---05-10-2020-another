package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Adapters.MyPagerAdapter;
import com.nisith.smartchat.Adapters.MySearchAdapter;
import com.nisith.smartchat.DialogBox.FriendImageClickDialog;
import com.nisith.smartchat.Fragments.ChatFragment;
import com.nisith.smartchat.Fragments.FriendsFragment;
import com.nisith.smartchat.Fragments.GroupsFragment;
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.Model.FriendRequest;
import com.nisith.smartchat.Model.UserStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements MySearchAdapter.OnSearchItemsClickListener {

    private Toolbar appToolbar;
    private TextView toolbarTextView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    //Search
    private FrameLayout searchFrameLayout;
    private RecyclerView searchRecyclerView;
    private MySearchAdapter searchAdapter;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference friendRequestRootDatabaseRef, friendsRootDatabaseRef;
    private ValueEventListener unseenFriendRequestValueEventListener;
    private View badgeViewForChats, badgeViewForRequests;
    private TextView badgeHeadingTextViewForChats, badgeItemsTextViewForChats;
    private TextView badgeHeadingTextViewForRequests, badgeItemsTextViewForRequests;
    private List<String> unseenFriendRequestKeyList;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeViews();
        //Set search frame layout visibility at the time of app launch
        searchFrameLayout.setVisibility(View.GONE);
        setSupportActionBar(appToolbar);
        setTitle("");
        toolbarTextView.setText("Smart Chat");
        setUpTabLayoutWithViewPager();
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
        searchFrameLayout = findViewById(R.id.search_frame_layout);
        searchRecyclerView = findViewById(R.id.search_recycler_view);
    }

    private void setUpTabLayoutWithViewPager(){
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), PagerAdapter.POSITION_NONE);
        viewPager.setAdapter(myPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }



    private void setupSearchRecyclerViewWithAdapter(){
        searchAdapter = new MySearchAdapter(getFirebaseDefaultOptions(), this);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        searchRecyclerView.setHasFixedSize(true);
        searchRecyclerView.setAdapter(searchAdapter);
    }

    private FirebaseRecyclerOptions<Friend> getFirebaseDefaultOptions(){
        Query query = friendsRootDatabaseRef.child(currentUser.getUid());
        FirebaseRecyclerOptions<Friend> options = new FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(query, Friend.class)
                .build();
        return options;
    }




    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null){
            openLoginActivity();
            finish();
        }else {
            friendRequestRootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
            friendsRootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friends");
            if (searchAdapter == null) {
                //This is only true for first time
                setupSearchRecyclerViewWithAdapter();
            }
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
        MenuItem menuItem = menu.findItem(R.id.search_view);
        expandModeAction(menuItem);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().length() > 0){
                    //If Some characters are present in search view
                    updateMySearchAdapterQuery(newText.toLowerCase());
                }else {
                    //when no characters in search view, then show all data i.e. all rows
                    searchAdapter.updateOptions(getFirebaseDefaultOptions());
                }
                return true;
            }
        });
        return true;
    }


    private void updateMySearchAdapterQuery(String newText){
        Query query = friendsRootDatabaseRef.child(currentUser.getUid()).orderByChild("searchName").startAt(newText).endAt(newText + "\uf8ff");
        FirebaseRecyclerOptions<Friend> options = new FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(query, Friend.class)
                .build();
        searchAdapter.updateOptions(options);
    }



    private void expandModeAction(MenuItem menuItem){
        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                //called when search view is expand
                //show frame layout and hide tab layout, view pager
                searchFrameLayout.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.GONE);
                //If search view is open then only we perform searching operation. Otherwise it consume unnecessaryly memory
                searchAdapter.startListening();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                //called when search view is collapse
                //hide frame layout and show tab layout, view pager
                searchFrameLayout.setVisibility(View.GONE);
                tabLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.VISIBLE);
                //If search view is collapse i.e. close, then we stop searching operation. To release memory
                searchAdapter.stopListening();
                return true;
            }
        });
    }



    @Override
    public void onContextMenuClosed(@NonNull Menu menu) {
        super.onContextMenuClosed(menu);
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

    @Override
    public void onViewClick(View view, String key, String friendType) {
        switch (view.getId()){
            case R.id.profile_image_view:
                showImageDialog(key);
                break;

            case R.id.root_view:
                //root card view is clicked
                showOptionsDialog(view, key, friendType);

        }
    }

    private void showImageDialog(String key){
        FriendImageClickDialog dialog = new FriendImageClickDialog(key);
        dialog.show(getSupportFragmentManager(),"smart chat");
    }

    private void showOptionsDialog(View view, final String key, final String friendType){
        Log.d("ZXCVBNM", "friend Type = "+friendType);
        //Here key may be group_key or friend_key
        CharSequence[] clickOptions = {"Wants to chat", "Show Profile"};
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext())
                .setItems(clickOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            //Open chat Activity
                            openChatActivity(key);

                        }else if (which == 1){
                            //Open profile activity
                            if (friendType.equals(Constant.SINGLE_FRIEND)){
                                openFriendProfileActivity(key);
                            }else if (friendType.equals(Constant.GROUP_FRIEND)){
                                openGroupProfileActivity(key);
                            }
                        }
                    }
                });

        dialogBuilder.create().show();
    }


    private void openChatActivity(String key){
        //Open Chat Activity
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constant.KEY, key);  //key may be friend key or group key
        startActivity(intent);
    }

    private void openFriendProfileActivity(String key){
        Intent infoIntent = new Intent(getApplicationContext(), FriendsProfileActivity.class);
        infoIntent.putExtra(Constant.FRIEND_UID, key);
        startActivity(infoIntent);
    }

    private void openGroupProfileActivity(String key){
        Intent infoIntent = new Intent(getApplicationContext(), GroupProfileActivity.class);
        infoIntent.putExtra(Constant.GROUP_KEY, key);
        startActivity(infoIntent);
    }



}