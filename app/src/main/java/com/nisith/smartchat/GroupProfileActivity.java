package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Model.GroupProfile;
import com.squareup.picasso.Picasso;

public class GroupProfileActivity extends AppCompatActivity {
    private Toolbar appToolbar;
    private ImageView backgroundImageView;
    private CircleImageView groupProfileImageView;
    private TextView totalGroupFriendsTextView, groupNameTextView, aboutGroupTextView;
    private Button inviteFriendsButton, groupFriendsButton;
    private TextView displayMessageTextView;
    //Group key
    private String groupKey, groupName, groupImageUrl;
    //Firebase
    private DatabaseReference currentGroupDatabaseRef;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        initializeViews();
        appToolbar.setNavigationIcon(R.drawable.ic_back_arrow_icon);
        appToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        if (intent != null){
            groupKey = intent.getStringExtra(Constant.GROUP_KEY);
            currentGroupDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups").child(groupKey);
            inviteFriendsButton.setOnClickListener(new MyClickListener());
            groupFriendsButton.setOnClickListener(new MyClickListener());
        }



    }

    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        backgroundImageView = findViewById(R.id.background_image_view);
        groupProfileImageView = findViewById(R.id.group_profile_image_view);
        totalGroupFriendsTextView = findViewById(R.id.total_group_friends_text_view);
        groupNameTextView = findViewById(R.id.group_name_text_view);
        aboutGroupTextView = findViewById(R.id.about_group_text_view);
        inviteFriendsButton = findViewById(R.id.invite_friend_button);
        groupFriendsButton = findViewById(R.id.all_group_friends_button);
        displayMessageTextView = findViewById(R.id.display_message_text_view);
    }


    private class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.invite_friend_button:
                    Intent intent = new Intent(GroupProfileActivity.this, FindFriendsActivity.class);
                    //current user wants to search friends for a group
                    intent.putExtra(Constant.SEARCH_FRIENDS_TYPE, Constant.SEARCH_FRIENDS_FOR_GROUP_FRIENDSHIP);
                    intent.putExtra(Constant.GROUP_KEY, groupKey);
                    startActivity(intent);
                    break;

                case R.id.all_group_friends_button:
                    openAllGroupFriendsActivity(groupKey);

            }

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (currentGroupDatabaseRef != null){
            setGroupDataOnViews();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (currentGroupDatabaseRef != null){
            currentGroupDatabaseRef.removeEventListener(valueEventListener);
        }
    }

    private void setGroupDataOnViews(){
        inviteFriendsButton.setEnabled(false);
        groupFriendsButton.setEnabled(false);
     valueEventListener = currentGroupDatabaseRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists()){
                   GroupProfile groupProfile = snapshot.getValue(GroupProfile.class);
                   if (groupProfile != null){
                       inviteFriendsButton.setEnabled(true);
                       groupFriendsButton.setEnabled(true);
                       groupName = groupProfile.getGroupName();
                       groupNameTextView.setText(groupName);
                       totalGroupFriendsTextView.setText(String.valueOf("Total " + groupProfile.getTotalGroupFriends()) + " Friends in this group");
                       aboutGroupTextView.setText(groupProfile.getAboutGroup());
                       groupImageUrl = groupProfile.getGroupProfileImage();
                       if (!groupImageUrl.equalsIgnoreCase("default")) {
                           Picasso.get().load(groupImageUrl).placeholder(R.drawable.group_icon).into(groupProfileImageView);
                       } else {
                           Picasso.get().load(R.drawable.group_icon).placeholder(R.drawable.group_icon).into(groupProfileImageView);
                       }
                   }
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }

    private void openAllGroupFriendsActivity(String groupKey){
        Intent groupIntent = new Intent(getApplicationContext(), AllGroupFriendsActivity.class);
        groupIntent.putExtra(Constant.GROUP_KEY, groupKey);
        groupIntent.putExtra(Constant.GROUP_NAME,groupName);
        groupIntent.putExtra(Constant.GROUP_PROFILE_IMAGE_URL,groupImageUrl);
        startActivity(groupIntent);
    }


}