package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.Model.GroupProfile;
import com.nisith.smartchat.Model.UserProfile;
import com.squareup.picasso.Picasso;

public class ChatActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    private ImageView backArrowImageView;
    private RecyclerView recyclerView;
    private CircleImageView profileImageView;
    private TextView profileNameTextView, onlineStatusTextView;
    private String key; //key may be friend key or group key
    //Firebase
    private DatabaseReference userDatabaseRef, groupsDatabaseRef, currentUserFriendsDatabaseRef;
    private ValueEventListener valueEventListenerForFriend, valueEventListenerForGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initializeViews();
        setSupportActionBar(appToolbar);
        backArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitle("");
        //Firebase
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        groupsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups");
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserFriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserUid);
        fetchDataFromIntent();
    }

    private void initializeViews(){
      appToolbar = findViewById(R.id.app_toolbar);
      backArrowImageView = findViewById(R.id.back_arrow_image_view);
      profileNameTextView = findViewById(R.id.profile_name_text_view);
      onlineStatusTextView = findViewById(R.id.online_state_image_view);
      profileImageView = findViewById(R.id.profile_image_view);
      recyclerView = findViewById(R.id.recycler_view);
    }

    private void fetchDataFromIntent(){
        Intent intent = getIntent();
        if (intent != null){
            key = intent.getStringExtra(Constant.KEY);
//            userName = intent.getStringExtra(Constant.USER_NAME);
//            profileImageUrl = intent.getStringExtra(Constant.PROFILE_IMAGE_URL);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setDataOnViews();
    }

    private void setDataOnViews(){
        if (key == null){
            return;
        }

        currentUserFriendsDatabaseRef.child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Friend friend = snapshot.getValue(Friend.class);
                            if (friend != null){
                                String friendType = friend.getFriendsType();
                                if (friendType.equals(Constant.SINGLE_FRIEND)){
                                    //means one to one friendship
                                    fetchFriendsData(key);
                                }else if (friendType.equals(Constant.GROUP_FRIEND)){
                                    //means group friendship
                                    fetchGroupData(key);
                                }
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
        if (userDatabaseRef != null && valueEventListenerForFriend != null){
            //means the key is friends key not group key
            // key == friendsKey
            userDatabaseRef.child(key).removeEventListener(valueEventListenerForFriend);
        }

        if (groupsDatabaseRef != null && valueEventListenerForGroup != null){
            //means the key is group key not friend key
            //here key == groupKey
            groupsDatabaseRef.child(key).removeEventListener(valueEventListenerForGroup);
        }
    }

    private void fetchFriendsData(String friendKey){
        valueEventListenerForFriend = userDatabaseRef.child(friendKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserProfile userProfile =snapshot.getValue(UserProfile.class);
                            if (userProfile != null) {
                                String userName = userProfile.getUserName();
                                profileNameTextView.setText(userName);
                                onlineStatusTextView.setText("online");
                                String profileImageUrl = userProfile.getProfileImage();
                                if (!profileImageUrl.equalsIgnoreCase("default")) {
                                    Picasso.get().load(profileImageUrl).placeholder(R.drawable.user_icon).into(profileImageView);
                                } else {
                                    Picasso.get().load(R.drawable.user_icon).placeholder(R.drawable.user_icon).into(profileImageView);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void fetchGroupData(String groupKey){
        valueEventListenerForGroup  = groupsDatabaseRef.child(groupKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            GroupProfile groupProfile =snapshot.getValue(GroupProfile.class);
                            if (groupProfile != null) {
                                String groupName = groupProfile.getGroupName();
                                profileNameTextView.setText(groupName);
                                onlineStatusTextView.setText("online");
                                String profileImageUrl = groupProfile.getGroupProfileImage();
                                if (!profileImageUrl.equalsIgnoreCase("default")) {
                                    Picasso.get().load(profileImageUrl).placeholder(R.drawable.ic_group_icon_white).into(profileImageView);
                                } else {
                                    Picasso.get().load(R.drawable.ic_group_icon_white).placeholder(R.drawable.ic_group_icon_white).into(profileImageView);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}

