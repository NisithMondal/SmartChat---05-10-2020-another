package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nisith.smartchat.Adapters.MyPagerAdapter;
import com.nisith.smartchat.Model.UserStatus;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    private TextView toolbarTextView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    //////////////////////////////////////
    TextView totalUnreadMessageTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeViews();
        setSupportActionBar(appToolbar);
        setTitle("");
        toolbarTextView.setText("Smart Chat");
        setUpTabLayoutWithViewPager();
        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();


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
    }





    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null){
            openLoginActivity();
            finish();
        }else {
            //Current user is already logged in
            updateUserStatus(true);
            Log.d("ASDFG", " Home onStart is called");
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

    public void setTabLayoutUnreadMessageCount(String totalUnreadMessages){
        if (totalUnreadMessageTextView == null){
            setBadgeView();
        }
        totalUnreadMessageTextView.setText(totalUnreadMessages);
    }

    private void setBadgeView(){
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.badge_view, null);
            totalUnreadMessageTextView = view.findViewById(R.id.total_unread_message_text_view);
            TextView tabHeadingTextView = view.findViewById(R.id.tab_heading_text_view);
            tab.setCustomView(view);
        }
    }


    private void updateUserStatus(boolean isOnline){
        Map<String, Object> userStatusMap = new HashMap<>();
        UserStatus userStatus = new UserStatus(isOnline, System.currentTimeMillis());
        DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        userStatusMap.put("users_detail_info" + "/" + currentUser.getUid() + "/" + "userStatus", userStatus);
        //update user's states
        rootDatabaseRef.updateChildren(userStatusMap);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_menu,menu);
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