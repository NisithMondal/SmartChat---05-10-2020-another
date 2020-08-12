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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nisith.smartchat.Adapters.MyPagerAdapter;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    private TextView toolbarTextView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;



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
        }
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
                startActivity(new Intent(HomeActivity.this,FindFriendsActivity.class));
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