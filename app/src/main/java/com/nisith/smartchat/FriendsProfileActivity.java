 package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Model.UserProfile;
import com.squareup.picasso.Picasso;

 public class FriendsProfileActivity extends AppCompatActivity {

     private ImageView backgroundImageView;
     private CircleImageView profileImageView;
     private TextView userNameTextView, userStatusTextView;
     //Firebase
     private DatabaseReference rootDatabaseRef;
     private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);
        initializeViews();
        Picasso.get().load(R.drawable.wallpaper3).into(backgroundImageView);
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        Intent intent = getIntent();
        String friendUid = intent.getStringExtra(Constant.FRIEND_UID);
        if (friendUid != null){
            showFriendProfile(friendUid);
        }

    }

    private void initializeViews(){
        backgroundImageView = findViewById(R.id.background_image_view);
        profileImageView = findViewById(R.id.profile_image_view);
        userNameTextView = findViewById(R.id.user_name_text_view);
        userStatusTextView = findViewById(R.id.user_status_text_view);
    }

    private void showFriendProfile(final String friendUid){
      valueEventListener = rootDatabaseRef.child(friendUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                      UserProfile friendUserProfile = snapshot.getValue(UserProfile.class);
                      if (friendUserProfile != null){
                          String friendName = friendUserProfile.getUserName();
                          String friendStatus = friendUserProfile.getUserStatus();
                          String profileImageUrl = friendUserProfile.getProfileImage();
                          userNameTextView.setText(friendName);
                          userStatusTextView.setText(friendStatus);
                          if (! profileImageUrl.equals("default")){
                              Picasso.get().load(profileImageUrl).placeholder(R.drawable.default_user_icon).into(profileImageView);
                          }
                      }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

     @Override
     protected void onStop() {
         super.onStop();
         if (rootDatabaseRef != null){
             rootDatabaseRef.removeEventListener(valueEventListener);
         }
     }
 }