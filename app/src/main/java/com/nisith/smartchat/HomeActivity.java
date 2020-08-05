package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeViews();
        setSupportActionBar(appToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Smart Chat");
        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

    }


    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
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
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.Create_group:
                Toast.makeText(this, "Create Group", Toast.LENGTH_SHORT).show();
                break;

            case R.id.user_profile_setting:
                Toast.makeText(this, "User Profile Setting", Toast.LENGTH_SHORT).show();
                break;

            case R.id.privacy_policy:
                Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show();
                break;

            case R.id.log_out:
                firebaseAuth.signOut();
                openLoginActivity();
                finishAffinity();
                break;
        }
        return true;
    }


    private void openLoginActivity(){
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
    }

}