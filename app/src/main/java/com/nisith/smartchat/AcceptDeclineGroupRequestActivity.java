package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.Model.FriendRequest;
import com.nisith.smartchat.Model.GroupProfile;
import com.nisith.smartchat.Model.UserProfile;
import com.nisith.smartchat.Notification.MyNotification;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class AcceptDeclineGroupRequestActivity extends AppCompatActivity {
    private Toolbar appToolbar;
//    private TextView toolbarTextView;
    private ImageView groupProfileImageView;
    private CircleImageView friendProfileImageView;
    private TextView friendNameTextView, aboutGroupTextView, displayMessageTextView, groupNameTextView;
    private Button joinGroupButton, rejectGroupButton;
    //Firebase
    private DatabaseReference rootDatabaseRef, friendUserDatabaseRef, friendsDatabaseRef, friendRequestDatabaseRef, currentGroupDatabaseRef, groupFriendsDatabaseRef;
    private ValueEventListener valueEventListener;
    private String currentUserId, friendUid, requestSenderUid, requestReceiverUid, groupKey;
    private String groupName, friendName, userProfileImageUrl, currentUserName, currentUserImageUrl;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_decline_group_request);
        initializeViews();
        setSupportActionBar(appToolbar);
        setTitle("");
//        toolbarTextView.setText("Group Request");
//        toolbarTextView.setTextColor(Color.BLACK);
        appToolbar.setNavigationIcon(R.drawable.ic_back_arrow_icon);
        appToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        friendUid = intent.getStringExtra(Constant.FRIEND_UID);
        groupKey = intent.getStringExtra(Constant.GROUP_KEY);
        if (friendUid != null && groupKey != null){
            rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
            friendUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendUid);
            friendRequestDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
            friendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friends");
            currentGroupDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups").child(groupKey);
            groupFriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("group_friends").child(groupKey);
            requestSenderUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //means Current User Id
            currentUserId = requestSenderUid;
            requestReceiverUid = friendUid; //means Friend User Id
            getCurrentUserInfo();
        }
        joinGroupButton.setOnClickListener(new MyButtonClickListener());
        rejectGroupButton.setOnClickListener(new MyButtonClickListener());
    }


    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
//        toolbarTextView = appToolbar.findViewById(R.id.toolbar_text_view);
        groupProfileImageView = findViewById(R.id.group_profile_image_view);
        friendProfileImageView = findViewById(R.id.friend_profile_image_view);
        groupNameTextView = findViewById(R.id.group_name_text_view);
        friendNameTextView = findViewById(R.id.friend_name_text_view);
        aboutGroupTextView = findViewById(R.id.group_info_text_view);
        joinGroupButton = findViewById(R.id.join_group_button);
        rejectGroupButton = findViewById(R.id.reject_group_button);
        displayMessageTextView = findViewById(R.id.display_message_text_view);
    }



    private void getCurrentUserInfo(){
        rootDatabaseRef.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    if (userProfile != null){
                        currentUserName = userProfile.getUserName();
                        currentUserImageUrl = userProfile.getProfileImage();
                    }
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
        if (friendUserDatabaseRef != null){
            showFriendProfile();
            fetchDataForGroup();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (groupFriendsDatabaseRef != null){
            groupFriendsDatabaseRef.removeEventListener(valueEventListener);
        }

    }

    private void fetchDataForGroup(){
        if (currentGroupDatabaseRef != null){
            currentGroupDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        GroupProfile groupProfile = snapshot.getValue(GroupProfile.class);
                        if (groupProfile != null){
                            groupName = groupProfile.getGroupName();
                            String aboutGroup = groupProfile.getAboutGroup();
                            groupNameTextView.setText(groupName);
                            aboutGroupTextView.setText(aboutGroup);
                            String groupProfileImageUrl = groupProfile.getGroupProfileImage();
                            if (! groupProfileImageUrl.equals("default")){
                                Picasso.get().load(groupProfileImageUrl).placeholder(R.drawable.group_icon).into(groupProfileImageView);
                            }else {
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

    }







    private void showFriendProfile(){
        friendUserDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile friendUserProfile = snapshot.getValue(UserProfile.class);
                if (friendUserProfile != null){
                    friendName = friendUserProfile.getUserName();
                    userProfileImageUrl = friendUserProfile.getProfileImage();
                    friendNameTextView.setText(friendName);
                    if (! userProfileImageUrl.equals("default")){
                        Picasso.get().load(userProfileImageUrl).placeholder(R.drawable.default_user_icon).into(friendProfileImageView);
                    }else {
                        Picasso.get().load(R.drawable.default_user_icon).placeholder(R.drawable.default_user_icon).into(friendProfileImageView);
                    }
                    //Check if the friend is already present in this group or not
                    checkIsFriendAlreadyAddedThisGroupOrNot();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void checkIsFriendAlreadyAddedThisGroupOrNot(){
        /*In this method we check if this friend is already join this group or not
          If this friend is not yet add this group, then only we show 'send friend request' option */
       valueEventListener = groupFriendsDatabaseRef
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(currentUserId)){
                            //Means Current user is already present in this group or not...
                            //this is useful when two or more group members send group request to a particular friend.
                            cancelGroupRequest();
                            displayMessageTextView.setText( "You already join this group");
                            displayMessageTextView.setVisibility(View.VISIBLE);
                            joinGroupButton.setVisibility(View.INVISIBLE);
                            joinGroupButton.setEnabled(false);
                            rejectGroupButton.setVisibility(View.GONE);
                            friendNameTextView.setVisibility(View.INVISIBLE);
                            friendProfileImageView.setVisibility(View.INVISIBLE);
                        }else {
                            //Means this friend is not present in this group. So show friend request options
                            setButtonStates();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void setButtonStates(){
        joinGroupButton.setVisibility(View.VISIBLE);
        rejectGroupButton.setVisibility(View.VISIBLE);
        joinGroupButton.setEnabled(true);
        rejectGroupButton.setEnabled(true);
        joinGroupButton.setText("Join Group");
        rejectGroupButton.setText("Reject Group");
        displayMessageTextView.setVisibility(View.VISIBLE);
        displayMessageTextView.setText(friendName + " wants to add you in this group");

    }




    private class MyButtonClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.join_group_button:
                    joinGroup();
                    break;

                case R.id.reject_group_button:
                    cancelGroupRequest();
            }
        }
    }

    private void joinGroup(){
        //if current user join this group i.e. accept group request
        Map<String, Object> map = new HashMap<>();
        map.put(requestSenderUid+"/"+requestReceiverUid + groupKey, new FriendRequest(Constant.FRIEND,true, groupKey,requestReceiverUid, System.currentTimeMillis(), true));
        map.put(requestReceiverUid+"/"+requestSenderUid + groupKey, new FriendRequest(Constant.FRIEND, true, groupKey,requestSenderUid, System.currentTimeMillis(), true));
        friendRequestDatabaseRef.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null){
                    //means all ok
                    Friend friend = new Friend(Constant.GROUP_FRIEND, groupName.toLowerCase(), System.currentTimeMillis());
                    Map<String, Object> addFriendMap = new HashMap<>();
                    addFriendMap.put("friends"+"/"+currentUserId+"/"+groupKey, friend);  //group is added current user friend's node
                    addFriendMap.put("group_friends"+"/"+groupKey+"/"+currentUserId, friend);// the current user is added to the group friend's node
                    rootDatabaseRef.updateChildren(addFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null){
                                displayMessageTextView.setText( "You Successfully join in this group");
                                //Increment 'totalGroupFriends' by 1
                                FirebaseDatabase.getInstance().getReference().child("groups")
                                        .child(groupKey).child("totalGroupFriends").setValue(ServerValue.increment(1));
                                displayMessageTextView.setVisibility(View.VISIBLE);
                                joinGroupButton.setVisibility(View.INVISIBLE);
                                joinGroupButton.setEnabled(false);
                                rejectGroupButton.setVisibility(View.GONE);
                                friendNameTextView.setVisibility(View.VISIBLE);
                                friendProfileImageView.setVisibility(View.VISIBLE);
                                //show notification
                                acceptFriendRequestNotification();
                            }
                        }
                    });

                }
            }
        });
    }

    private void acceptFriendRequestNotification() {
        //when current user accept his/her friend request, then send notification to that friend that current user accept his/her friend request.
        String title = "Join Group";
        String body = currentUserName+" join this group: "+groupName;
        MyNotification myNotification = new MyNotification(getApplicationContext());
        myNotification.send(title, body, requestReceiverUid, null, currentUserImageUrl);
    }

    private void cancelGroupRequest(){
        //if current user reject the group i.e. decline group request
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(requestSenderUid + "/" + requestReceiverUid + groupKey, null);
        dataMap.put(requestReceiverUid + "/" + requestSenderUid + groupKey, null);;
        friendRequestDatabaseRef.updateChildren(dataMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null){
                    //all ok
                    //current user reject this group request. So simply destroy this activity
                    finish();
                }else {
                    Toast.makeText(AcceptDeclineGroupRequestActivity.this, "Some error arise", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}