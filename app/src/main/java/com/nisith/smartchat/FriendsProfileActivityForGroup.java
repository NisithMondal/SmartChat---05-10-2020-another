package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
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
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.Model.FriendRequest;
import com.nisith.smartchat.Model.GroupProfile;
import com.nisith.smartchat.Model.UserProfile;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class FriendsProfileActivityForGroup extends AppCompatActivity {

    private Toolbar appToolbar;
    private ImageView backgroundImageView;
    private CircleImageView friendProfileImageView, groupProfileImageView;
    private TextView userNameTextView, userStatusTextView, userInfoTextView, infoTextHeading;
    private Button friendRequestButton, declineRequestButton;
    private TextView displayMessageTextView, groupNameTextView;
    //Firebase
    private DatabaseReference rootDatabaseRef, friendUserDatabaseRef, friendsDatabaseRef, friendRequestDatabaseRef, groupDatabaseRef;
    private ValueEventListener valueEventListener;
    private String friendName, userProfileImageUrl;
    private String currentUserId, friendUid, requestSenderUid, requestReceiverUid, groupKey;
    private String requestStatus = Constant.NOT_GROUP_FRIEND; //status of request i.e. send_request, cancel_request, accept_request, decline_request


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile_for_group);
        initializeViews();
        setSupportActionBar(appToolbar);
        setTitle("");
        appToolbar.setNavigationIcon(R.drawable.ic_back_arrow_icon);
        appToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Picasso.get().load(R.drawable.wallpaper2).into(backgroundImageView);
        Intent intent = getIntent();
        friendUid = intent.getStringExtra(Constant.FRIEND_UID);
        groupKey = intent.getStringExtra(Constant.GROUP_KEY);
        if (friendUid != null){
            rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
            friendUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendUid);
            friendRequestDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
            friendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friends");
            groupDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups").child(groupKey);
            requestSenderUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //means Current User Id
            currentUserId = requestSenderUid;
            requestReceiverUid = friendUid; //means Friend User Id
            setFriendRequestButtonState();
        }
        friendRequestButton.setOnClickListener(new MyButtonClickListener());
        declineRequestButton.setOnClickListener(new MyButtonClickListener());
        friendProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageDisplayIntent = new Intent(FriendsProfileActivityForGroup.this, ImageDisplayActivity.class);
                imageDisplayIntent.putExtra(Constant.USER_NAME, friendName);
                imageDisplayIntent.putExtra(Constant.PROFILE_IMAGE_URL, userProfileImageUrl);
                startActivity(imageDisplayIntent);
            }
        });

    }


    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        backgroundImageView = findViewById(R.id.background_image_view);
        friendProfileImageView = findViewById(R.id.user_profile_image_view);
        groupProfileImageView = findViewById(R.id.group_profile_image_view);
        userNameTextView = findViewById(R.id.user_name_text_view);
        userStatusTextView = findViewById(R.id.user_status_text_view);
        userInfoTextView = findViewById(R.id.user_info_text_view);
        infoTextHeading = findViewById(R.id.info_text_heading);
        friendRequestButton = findViewById(R.id.friend_request_button);
        declineRequestButton = findViewById(R.id.decline_request_button);
        displayMessageTextView = findViewById(R.id.display_message_text_view);
        groupNameTextView = findViewById(R.id.group_name_text_view);
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
        if (friendUserDatabaseRef != null){
            //remove value event listener
            friendUserDatabaseRef.removeEventListener(valueEventListener);
        }
    }


    private void showFriendProfile(){
        valueEventListener = friendUserDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile friendUserProfile = snapshot.getValue(UserProfile.class);
                if (friendUserProfile != null){
                    friendName = friendUserProfile.getUserName();
                    String friendStatus = friendUserProfile.getUserStatus();
                    userProfileImageUrl = friendUserProfile.getProfileImage();
                    userNameTextView.setText(friendName);
                    //set the heading of about user fields
                    infoTextHeading.setText(friendName);
                    userStatusTextView.setText(friendStatus);
                    if (! userProfileImageUrl.equals("default")){
                        Picasso.get().load(userProfileImageUrl).placeholder(R.drawable.default_user_icon).into(friendProfileImageView);
                    }else {
                        Picasso.get().load(R.drawable.default_user_icon).placeholder(R.drawable.default_user_icon).into(friendProfileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FriendsProfileActivityForGroup.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDataForGroup(){
        if (groupDatabaseRef != null){
            groupDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        GroupProfile groupProfile = snapshot.getValue(GroupProfile.class);
                        if (groupProfile != null){
                            String groupName = groupProfile.getGroupName();
                            String groupProfileImageUrl = groupProfile.getGroupProfileImage();
                            groupNameTextView.setText(groupName);
                            if (! userProfileImageUrl.equals("default")){
                                Picasso.get().load(groupProfileImageUrl).placeholder(R.drawable.ic_group_icon1).into(groupProfileImageView);
                            }else {
                                Picasso.get().load(R.drawable.ic_group_icon1).placeholder(R.drawable.ic_group_icon1).into(groupProfileImageView);
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





    private class MyButtonClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.friend_request_button:
                    handelRequestOperation();
                    break;

                case R.id.decline_request_button:
                    declineFriendRequest();
            }
        }
    }


    private void setFriendRequestButtonState(){
        friendRequestDatabaseRef.child(requestSenderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String combineGroupKey = requestReceiverUid+groupKey;
                        if (snapshot.hasChild(combineGroupKey)){
                            FriendRequest friendRequest = snapshot.child(combineGroupKey).getValue(FriendRequest.class);
                            if (friendRequest != null){
                                requestStatus =  friendRequest.getRequestType();
                            }
                        }else {
                            //if current user has no child of this friend
                            if (currentUserId.equals(friendUid)){
                                //Means Current user open his/her own profile
                                requestStatus = Constant.SAME_USER;
                            }else {
                                //Means they are new to each other
                                requestStatus = Constant.NOT_GROUP_FRIEND;
                            }
                        }

                        if (requestStatus.equals(Constant.NOT_GROUP_FRIEND)){
                            //This Friend is not group friend still now.
                            friendRequestButton.setVisibility(View.VISIBLE);
                            friendRequestButton.setEnabled(true);
                            friendRequestButton.setText("Send Friend Request");
                            friendRequestButton.setBackground(getDrawable(R.drawable.button_background_shape));
                            displayMessageTextView.setVisibility(View.GONE);
                            declineRequestButton.setVisibility(View.INVISIBLE);
                            declineRequestButton.setEnabled(false);

                        }else if (requestStatus.equals(Constant.SAME_USER)){
                            //Current user open his/her own profile.
                            displayMessageTextView.setText("Your Own Profile");
                            displayMessageTextView.setVisibility(View.VISIBLE);
                            friendRequestButton.setVisibility(View.INVISIBLE);
                            declineRequestButton.setVisibility(View.INVISIBLE);
                            friendRequestButton.setEnabled(false);
                            declineRequestButton.setEnabled(false);

                        } else if (requestStatus.equals(Constant.SEND_REQUEST)){
                            //Current User send friend request to this friend.
                            friendRequestButton.setText("Cancel Friend Request");
                            friendRequestButton.setBackground(getDrawable(R.drawable.button_background_shape3));
                            displayMessageTextView.setVisibility(View.GONE);
                            declineRequestButton.setVisibility(View.INVISIBLE);
                            declineRequestButton.setEnabled(false);

                        }
                        else if (requestStatus.equals(Constant.RECEIVE_REQUEST)){
                            //This friend send request to the Current User.
                            friendRequestButton.setText("Accept Request");
                            friendRequestButton.setBackground(getDrawable(R.drawable.button_background_shape1));
                            declineRequestButton.setText("Decline Request");
                            declineRequestButton.setEnabled(true);
                            displayMessageTextView.setVisibility(View.VISIBLE);
                            displayMessageTextView.setText(friendName + " wants to add you in this group");
                            declineRequestButton.setVisibility(View.VISIBLE);

                        }else if (requestStatus.equals(Constant.FRIEND)){
                            //Both of you are friends now.
                            displayMessageTextView.setText("Both of you are friends now");
                            displayMessageTextView.setVisibility(View.VISIBLE);
                            declineRequestButton.setText("UnFriend");
                            declineRequestButton.setVisibility(View.VISIBLE);
                            declineRequestButton.setEnabled(true);
                            friendRequestButton.setVisibility(View.INVISIBLE);
                            friendRequestButton.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }




    private void handelRequestOperation(){
        if (requestStatus.equals(Constant.NOT_GROUP_FRIEND)) {
            //Current user wants to send friend request to this friend
            Map<String, Object> dataMap = new HashMap<>();
            //send request for group friendship
            //I concatinate group key to generate unike request key for group's friend request
            dataMap.put(requestSenderUid + "/" + requestReceiverUid + groupKey, new FriendRequest(Constant.SEND_REQUEST, true, groupKey, requestReceiverUid,"now"));
            dataMap.put(requestReceiverUid + "/" + requestSenderUid + groupKey, new FriendRequest(Constant.RECEIVE_REQUEST,  true, groupKey, requestSenderUid,"now"));
            requestStatus = Constant.SEND_REQUEST;
            friendRequestDatabaseRef.updateChildren(dataMap);

        }
        else if (requestStatus.equals(Constant.SEND_REQUEST)){
            //Current user wants to Cancel friend request
            cancelFriendRequest();
        }
        else if (requestStatus.equals(Constant.RECEIVE_REQUEST)){
            //Current user wants to accept friend request
            acceptFriendRequest();

        }
    }



    private void acceptFriendRequest(){
        Map<String, Object> map = new HashMap<>();
        map.put(requestSenderUid+"/"+requestReceiverUid + groupKey, new FriendRequest(Constant.FRIEND,true, groupKey,requestReceiverUid,"now"));
        map.put(requestReceiverUid+"/"+requestSenderUid + groupKey, new FriendRequest(Constant.FRIEND, true, groupKey,requestSenderUid,"now"));
        friendRequestDatabaseRef.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null){
                    //means all ok
                    Friend friend = new Friend("now", Constant.GROUP_FRIEND);
                    Map<String, Object> addFriendMap = new HashMap<>();
                    addFriendMap.put("friends"+"/"+currentUserId+"/"+groupKey,friend);  //group is added current user friend's node
                    addFriendMap.put("group_friends"+"/"+groupKey+"/"+currentUserId,friend);// the current user is added to the group friend's node
                    rootDatabaseRef.updateChildren(addFriendMap);

                }
            }
        });
    }

    private void declineFriendRequest(){
        if (declineRequestButton.getVisibility() == View.VISIBLE){
            if (declineRequestButton.getText().toString().equalsIgnoreCase("Decline Request")) {
                //Decline Request
                cancelFriendRequest();
            }else if (declineRequestButton.getText().toString().equalsIgnoreCase("UnFriend")){
                //UnFriend
                unFriend();
            }
        }
    }

    private void unFriend(){
        Map<String, Object> unFriendMap = new HashMap<>();
        unFriendMap.put(requestSenderUid+"/"+requestReceiverUid + groupKey, null);
        unFriendMap.put(requestReceiverUid+"/"+requestSenderUid + groupKey, null);
        friendRequestDatabaseRef.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null){
                    //means all ok
                    Map<String, Object> addFriendMap = new HashMap<>();
                    addFriendMap.put("friends"+"/"+currentUserId+"/"+groupKey, null);  //group is deleted from current user friend's node
                    addFriendMap.put("group_friends"+"/"+groupKey+"/"+currentUserId, null);// the current user is deleted from the group friend's node
                    rootDatabaseRef.updateChildren(addFriendMap);

                }
            }
        });

    }



    private void cancelFriendRequest(){
        //cancel group friend request
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(requestSenderUid + "/" + requestReceiverUid + groupKey, null);
        dataMap.put(requestReceiverUid + "/" + requestSenderUid + groupKey, null);;
        requestStatus = Constant.NOT_GROUP_FRIEND;
        friendRequestDatabaseRef.updateChildren(dataMap);
    }


}