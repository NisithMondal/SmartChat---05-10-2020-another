package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.util.Objects;

public class ProfileSettingActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    private CircleImageView profileImageView;
    private TextView userNameTextView, userStatusTextView;
    private ProgressBar progressBar;
    private Button editProfileButton;
    //Firebase
    private DatabaseReference rootDatabaseRef;
    private ValueEventListener valueEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);
        initializeViews();
        setSupportActionBar(appToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("User profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar.setVisibility(View.GONE);
        editProfileButton.setVisibility(View.INVISIBLE);
        //Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
           String currentUserId = currentUser.getUid();
            rootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
            showUserProfile();
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
        profileImageView = findViewById(R.id.profile_image_view);
        userNameTextView = findViewById(R.id.user_name_text_view);
        userStatusTextView = findViewById(R.id.user_status_text_view);
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
                        userNameTextView.setText(userProfile.getUserName());
                        userStatusTextView.setText("Status: " + userProfile.getUserStatus());
                        String imageUrl = userProfile.getProfileImage();
                        if (! imageUrl.equalsIgnoreCase("default")){
                            //means profileImage value is not default.
                            Picasso.get().load(imageUrl).placeholder(R.drawable.default_user_icon).into(profileImageView);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null && rootDatabaseRef != null){
            //Remove value event listener
            rootDatabaseRef.removeEventListener(valueEventListener);
        }
    }
}