package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Model.UserProfile;
import com.squareup.picasso.Picasso;

public class ProfileSettingActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    private TextView toolbarTextView;
    private ImageView profileImageView;
    private TextView userNameTextView, userStatusTextView, aboutUserTextView;
    private ProgressBar progressBar;
    private Button editProfileButton;
    private String userName;
    private String profileImageUrl;
    //Firebase
    private DatabaseReference rootDatabaseRef, aboutMeDatabaseRef;
    private ValueEventListener valueEventListener, aboutMeValueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);
        initializeViews();
        setSupportActionBar(appToolbar);
        setTitle("");
//        toolbarTextView.setTextColor(Color.BLACK);
        appToolbar.setNavigationIcon(R.drawable.ic_back_arrow_icon);
        appToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        progressBar.setVisibility(View.GONE);
        editProfileButton.setVisibility(View.INVISIBLE);
        //Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
           String currentUserId = currentUser.getUid();
            rootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
            aboutMeDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users_detail_info").child(currentUserId).child("aboutMe");
        }
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileSettingActivity.this, EditProfileActivity.class));
            }
        });
    }
    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
//        toolbarTextView = findViewById(R.id.toolbar_text_view);
        profileImageView = findViewById(R.id.profile_image_view);
        userNameTextView = findViewById(R.id.user_name_text_view);
        userStatusTextView = findViewById(R.id.user_status_text_view);
        aboutUserTextView = findViewById(R.id.user_info_text_view);
        progressBar = findViewById(R.id.progress_bar);
        editProfileButton = findViewById(R.id.edit_profile_button);
    }

    private void showUserProfile(){
        progressBar.setVisibility(View.VISIBLE);
        editProfileButton.setVisibility(View.INVISIBLE);
         valueEventListener = rootDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    progressBar.setVisibility(View.GONE);
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    if (userProfile != null){
                        editProfileButton.setVisibility(View.VISIBLE);
                        userName = userProfile.getUserName();
                        userNameTextView.setText(userName);
                        profileImageUrl = userProfile.getProfileImage();
                        userStatusTextView.setText(userProfile.getUserStatus());

                        if (! profileImageUrl.equalsIgnoreCase("default")){
                            //means profileImage value is not default.
                            Picasso.get().load(profileImageUrl).fit().placeholder(R.drawable.default_user_icon).into(profileImageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileSettingActivity.this, "Data Not Loaded", Toast.LENGTH_SHORT).show();
                }
            });
    }


    private void fetchAboutUserDetailsInfo(){
        aboutMeValueEventListener = aboutMeDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String aboutMe = snapshot.getValue().toString();
                    aboutUserTextView.setText(aboutMe);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        showUserProfile();
        fetchAboutUserDetailsInfo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (valueEventListener != null && rootDatabaseRef != null){
            //Remove value event listener
            rootDatabaseRef.removeEventListener(valueEventListener);
        }

        if (aboutMeDatabaseRef != null){
            aboutMeDatabaseRef.removeEventListener(aboutMeValueEventListener);
        }

    }



}