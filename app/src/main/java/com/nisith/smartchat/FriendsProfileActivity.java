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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.Model.FriendRequest;
import com.nisith.smartchat.Model.UserNotification;
import com.nisith.smartchat.Model.UserProfile;
import com.nisith.smartchat.Notification.MyNotification;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

 public class FriendsProfileActivity extends AppCompatActivity {

     private Toolbar appToolbar;
     private ImageView backgroundImageView;
     private CircleImageView profileImageView;
     private TextView userNameTextView, userStatusTextView, userInfoTextView, infoTextHeading;
     private Button friendRequestButton, declineRequestButton;
     private TextView displayMessageTextView;
     private String friendName, currentUserName, profileImageUrl, currentUserImageUrl;
     //Firebase
     private DatabaseReference databaseRef, rootDatabaseRef;
     private DatabaseReference friendRequestDatabaseRef;
     private DatabaseReference friendsDatabaseRef;
     private ValueEventListener valueEventListener;
     private String friendUid, currentUserId;
     private String requestSenderUid, requestReceiverUid;
     private String requestStatus = Constant.NOT_FRIEND; //Status of request i.e. send_request, cancel_request, accept_request, decline_request

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);
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
        if (friendUid != null){
            databaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendUid);
            friendRequestDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
            friendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friends");
            rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
            requestSenderUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //means Current User Id
            currentUserId = requestSenderUid;
            requestReceiverUid = friendUid; //means Friend User Id
            setFriendRequestButtonState();
            getCurrentUserInfo();
        }
        friendRequestButton.setOnClickListener(new MyButtonClickListener());
        declineRequestButton.setOnClickListener(new MyButtonClickListener());
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageDisplayIntent = new Intent(FriendsProfileActivity.this, ImageDisplayActivity.class);
                imageDisplayIntent.putExtra(Constant.USER_NAME, friendName);
                imageDisplayIntent.putExtra(Constant.PROFILE_IMAGE_URL, profileImageUrl);
                startActivity(imageDisplayIntent);
            }
        });
    }

    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        backgroundImageView = findViewById(R.id.background_image_view);
        profileImageView = findViewById(R.id.profile_image_view);
        userNameTextView = findViewById(R.id.user_name_text_view);
        userStatusTextView = findViewById(R.id.user_status_text_view);
        userInfoTextView = findViewById(R.id.user_info_text_view);
        infoTextHeading = findViewById(R.id.info_text_heading);
        friendRequestButton = findViewById(R.id.friend_request_button);
        declineRequestButton = findViewById(R.id.decline_request_button);
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


    private void showFriendProfile(){
      valueEventListener = databaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                      UserProfile friendUserProfile = snapshot.getValue(UserProfile.class);
                      if (friendUserProfile != null){
                          friendName = friendUserProfile.getUserName();
                          String friendStatus = friendUserProfile.getUserStatus();
                          profileImageUrl = friendUserProfile.getProfileImage();
                          userNameTextView.setText(friendName);
                          //set the heading of about user fields
                          infoTextHeading.setText(friendName);
                          userStatusTextView.setText(friendStatus);
                          if (! profileImageUrl.equals("default")){
                              Picasso.get().load(profileImageUrl).placeholder(R.drawable.default_user_icon).into(profileImageView);
                          }else {
                              Picasso.get().load(R.drawable.default_user_icon).placeholder(R.drawable.default_user_icon).into(profileImageView);
                          }
                      }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(FriendsProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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


     @Override
     protected void onStart() {
         super.onStart();
         if (databaseRef != null){
             showFriendProfile();
         }
     }



     @Override
     protected void onStop() {
         super.onStop();
         if (databaseRef != null){
             //remove value event listener
             databaseRef.removeEventListener(valueEventListener);
         }
     }

     private void setFriendRequestButtonState(){
        friendRequestDatabaseRef.child(requestSenderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(requestReceiverUid)){
                           FriendRequest friendRequest = snapshot.child(requestReceiverUid).getValue(FriendRequest.class);
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
                                requestStatus = Constant.NOT_FRIEND;
                            }
                        }


                        if (requestStatus.equals(Constant.NOT_FRIEND)){
                            //They are new to each other.
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

                        }else if (requestStatus.equals(Constant.RECEIVE_REQUEST)){
                            //This friend send request to the Current User.
                            friendRequestButton.setText("Accept Request");
                            friendRequestButton.setBackground(getDrawable(R.drawable.button_background_shape1));
                            declineRequestButton.setText("Decline Request");
                            declineRequestButton.setEnabled(true);
                            displayMessageTextView.setVisibility(View.GONE);
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
        if (requestStatus.equals(Constant.NOT_FRIEND)) {
            //Current user wants to send friend request to this friend
            Map<String, Object> dataMap = new HashMap<>();
            //send request for single i.e. for one to one chat friendship
            //here 'read' is false means current user send the notification to his friends which is not yet seen by his/her friend
            dataMap.put(requestSenderUid + "/" + requestReceiverUid, new FriendRequest(Constant.SEND_REQUEST, false, "",requestReceiverUid, System.currentTimeMillis(), false));
            dataMap.put(requestReceiverUid + "/" + requestSenderUid, new FriendRequest(Constant.RECEIVE_REQUEST,false, "",requestSenderUid, System.currentTimeMillis(), false));
            requestStatus = Constant.SEND_REQUEST;
            friendRequestDatabaseRef.updateChildren(dataMap);
            //Save Friend Request notification to firebase
            saveFriendRequestNotificationOnFirebase();
        }else if (requestStatus.equals(Constant.SEND_REQUEST)){
          //Current user wants to Cancel friend request
          cancelFriendRequest();
        }else if (requestStatus.equals(Constant.RECEIVE_REQUEST)){
            //Current user wants to accept friend request
            acceptFriendRequest();

        }
     }



     private void saveFriendRequestNotificationOnFirebase(){
         final String title = "Friend Request";
         final String body = currentUserName+" send you friend request.";
         String key = rootDatabaseRef.child("notification").child(currentUserId).child(Constant.ALL_NOTIFICATION).push().getKey();
         UserNotification userNotification = new UserNotification(title, body, System.currentTimeMillis(),"date_time"
                 ,currentUserImageUrl, currentUserId, "blank");
        Map<String, Object> map = new HashMap<>();
        map.put(requestReceiverUid + "/" + Constant.ALL_NOTIFICATION + "/" +key , userNotification);
        rootDatabaseRef.child("notification").updateChildren(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Current user send friend request. So, have to show friend request notification
                       sendFriendRequestNotification(title, body);
                    }
                });
     }


     private void sendFriendRequestNotification(String title, String body) {
         MyNotification myNotification = new MyNotification(getApplicationContext());
         myNotification.send(title, body, requestReceiverUid, null, currentUserImageUrl);
     }


     private void acceptFriendRequest(){
        Map<String, Object> map = new HashMap<>();
         //here 'read' is true means user seen the notification
        map.put(requestSenderUid+"/"+requestReceiverUid,new FriendRequest(Constant.FRIEND,false, "", requestReceiverUid, System.currentTimeMillis(), true));
        map.put(requestReceiverUid+"/"+requestSenderUid,new FriendRequest(Constant.FRIEND, false, "",requestSenderUid, System.currentTimeMillis(), true));
        friendRequestDatabaseRef.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null){
                    //all ok
                    Friend senderFriendObj = new Friend(Constant.SINGLE_FRIEND, friendName.toLowerCase(), System.currentTimeMillis());
                    Friend receiverFriendObj = new Friend(Constant.SINGLE_FRIEND, currentUserName.toLowerCase(), System.currentTimeMillis());
                    Map<String, Object> friendsMap = new HashMap<>();
                    friendsMap.put(requestSenderUid+"/"+requestReceiverUid,senderFriendObj);
                    friendsMap.put(requestReceiverUid+"/"+requestSenderUid,receiverFriendObj);
                    friendsDatabaseRef.updateChildren(friendsMap);
                    saveAcceptRequestNotificationOnFirebase();
                }
            }
        });
     }


     private void saveAcceptRequestNotificationOnFirebase(){
         final String title = "Accept Request";
         final String body = currentUserName+" accept your friend request.";
         String key = rootDatabaseRef.child("notification").child(currentUserId).child(Constant.ALL_NOTIFICATION).push().getKey();
         UserNotification userNotification = new UserNotification(title, body, System.currentTimeMillis(),"date_time"
                 ,currentUserImageUrl, currentUserId, "blank");
         Map<String, Object> map = new HashMap<>();
         map.put(friendUid + "/" + Constant.ALL_NOTIFICATION + "/" +key , userNotification);
         rootDatabaseRef.child("notification").updateChildren(map)
                 .addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void aVoid) {
                         //Current user Accept friend request. So, have to show Accept request notification
                         acceptFriendRequestNotification(title, body);
                     }
                 });
     }


     private void acceptFriendRequestNotification(String title, String body) {
        //when current user accept his/her friend request, then send notification to that friend that current user accept his/her friend request.
         MyNotification myNotification = new MyNotification(getApplicationContext());
         myNotification.send(title, body, friendUid, null, currentUserImageUrl);
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
         unFriendMap.put(requestSenderUid+"/"+requestReceiverUid, null);
         unFriendMap.put(requestReceiverUid+"/"+requestSenderUid, null);
         friendRequestDatabaseRef.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
             @Override
             public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                 if (error == null){
                     //all ok
                     Map<String, Object> friendsMap = new HashMap<>();
                     friendsMap.put(requestSenderUid+"/"+requestReceiverUid,null);
                     friendsMap.put(requestReceiverUid+"/"+requestSenderUid,null);
                     friendsDatabaseRef.updateChildren(friendsMap);

                 }
             }
         });

     }

     private void cancelFriendRequest(){
         Map<String, Object> dataMap = new HashMap<>();
         dataMap.put(requestSenderUid + "/" + requestReceiverUid, null);
         dataMap.put(requestReceiverUid + "/" + requestSenderUid, null);;
         requestStatus = Constant.NOT_FRIEND;
         friendRequestDatabaseRef.updateChildren(dataMap);
     }




 }