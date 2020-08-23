package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Adapters.MyChatActivityRecyclerViewAdapter;
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.Model.GroupProfile;
import com.nisith.smartchat.Model.Message;
import com.nisith.smartchat.Model.UserProfile;
import com.nisith.smartchat.Model.UserStatus;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    private ImageView backArrowImageView;
    private RecyclerView chatRecyclerView;
    private CircleImageView profileImageView;
    private ImageView sendMessageImageView;
    private EditText writeMessageEditText;
    private TextView profileNameTextView, onlineStatusTextView, messageDateTextView;
    private String key; //key may be friend key or group key
    //Firebase
    private FirebaseUser currentUser;
    private DatabaseReference userDatabaseRef, groupsDatabaseRef, groupFriendsDatabaseRef, currentUserFriendsDatabaseRef, friendStatusDatabaseRef, messagesDatabaseRef;
    private ValueEventListener valueEventListenerForFriend, valueEventListenerForGroup, friendStatusValueEventListener;

    private ChildEventListener testValueEventListener;
    private ChildEventListener totalGroupFriendsChildEventListener;

    private String messageSenderId, messageReceiverId, groupUid, friendType, profileImageUrl;
    private List<Message> messageList;
    private List<String> totalGroupFriendsList; // Contains all the friend's uid of a particular group...
    private MyChatActivityRecyclerViewAdapter recyclerViewAdapter;

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
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        messageSenderId = currentUser.getUid();
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        groupsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups");
        groupFriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("group_friends");
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserFriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserUid);
        messagesDatabaseRef = FirebaseDatabase.getInstance().getReference().child("messages");
        fetchDataFromIntent();
        messageList = new ArrayList<>();
        totalGroupFriendsList = new ArrayList<>();
        setupRecyclerViewWithAdapter();
        sendMessageImageView.setOnClickListener(new MyClickListener());
        messageDateTextView.setVisibility(View.GONE);
    }

    private void initializeViews(){
      appToolbar = findViewById(R.id.app_toolbar);
      backArrowImageView = findViewById(R.id.back_arrow_image_view);
      profileNameTextView = findViewById(R.id.profile_name_text_view);
      onlineStatusTextView = findViewById(R.id.friend_status_text_view);
      profileImageView = findViewById(R.id.profile_image_view);
      chatRecyclerView = findViewById(R.id.chat_recycler_view);
      writeMessageEditText = findViewById(R.id.message_edit_text);
      sendMessageImageView = findViewById(R.id.send_message_image_view);
      messageDateTextView = findViewById(R.id.message_date_text_view);
    }

    private void fetchDataFromIntent(){
        Intent intent = getIntent();
        if (intent != null){
            key = intent.getStringExtra(Constant.KEY);
        }
    }

    private void setupRecyclerViewWithAdapter(){
        recyclerViewAdapter = new MyChatActivityRecyclerViewAdapter(messageList, this);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setAdapter(recyclerViewAdapter);

    }



    public void setMessageDateTextView(String date){
        //set message date text view
        messageDateTextView.setVisibility(View.VISIBLE);
        messageDateTextView.setText(date);
    }




    private class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.send_message_image_view:
                    //when send image view of chat activity is clicked
                    sendMessage();
                    break;
            }

        }
    }


    private void sendMessage(){
        String message = writeMessageEditText.getText().toString();
        if (TextUtils.isEmpty(message)){
            //if edit text is empty
            Toast.makeText(this, "Write you message...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (friendType != null && key != null){
            //means friend_uid and friend_type i.e. group_friend or private_friend is available...
            if (friendType.equals(Constant.SINGLE_FRIEND)){
                //means current user wants one to one chat
                String messageType = "text";
                sendPrivateChatMessageToServer(message, messageType);

            }else if (friendType.equals(Constant.GROUP_FRIEND)){
                //Means Current user wants group chat
                sendGroupChatMessageToServer(message, "text");
            }

        }

    }


    private void sendPrivateChatMessageToServer(String inputMessage, String messageType){
        //This method send private i.e. one to one chat messages to server
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("MMM dd, yyyy");
        String currentDate = date.format(calendar.getTime());
        SimpleDateFormat time = new SimpleDateFormat("hh:mm a");
        String currentTime = time.format(calendar.getTime());
        //create message to be sent
        Message message = new Message(messageSenderId, messageType, inputMessage, false, currentDate, currentTime);
        String messageKey = messagesDatabaseRef.child(messageSenderId).child(messageReceiverId).push().getKey();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(messageSenderId + "/" + messageReceiverId + "/" + messageKey, message);
        messageMap.put(messageReceiverId + "/" + messageSenderId + "/" + messageKey, message);
        messagesDatabaseRef.updateChildren(messageMap);
        //After send message clear the edit text
        writeMessageEditText.setText("");
    }


    private void sendGroupChatMessageToServer(String inputMessage, String messageType){
        //This method send group chat messages to server
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("MMM dd, yyyy");
        String currentDate = date.format(calendar.getTime());
        SimpleDateFormat time = new SimpleDateFormat("hh:mm a");
        String currentTime = time.format(calendar.getTime());
        if (totalGroupFriendsList != null){
            String groupKey = key;
            //create message to be sent
            Message groupMessage = new Message(messageSenderId, messageType, inputMessage, false, currentDate, currentTime);
            String messageKey = messagesDatabaseRef.child(messageSenderId).child(groupKey).push().getKey();
            Map<String, Object> groupMessageMap = new HashMap<>();
            //write this message to all of the group friend's message node. The current user is also present in 'totalGroupFriendsList' list.
            for (String friendKey : totalGroupFriendsList) {
                groupMessageMap.put(friendKey + "/" + groupKey + "/" + messageKey, groupMessage);
            }
            messagesDatabaseRef.updateChildren(groupMessageMap);
            //After send message clear the edit text
            writeMessageEditText.setText("");

        }else {
            Toast.makeText(this, "Message not send", Toast.LENGTH_SHORT).show();
        }

    }





    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null){
            //Current user is already logged in
            updateUserStatus(true);
        }
        if (totalGroupFriendsList != null){
            totalGroupFriendsList.clear();
        }
        setDataOnViews();
        ////////////////////
        if (messageReceiverId != null){
            showChatMessages();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (currentUser != null){
            //Current user is already logged in
            updateUserStatus(false);
        }
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

        if (friendStatusDatabaseRef != null){
            friendStatusDatabaseRef.removeEventListener(friendStatusValueEventListener);
        }
        if (totalGroupFriendsList != null){
            totalGroupFriendsList.clear();
        }
        //For one to oen chat 'totalGroupFriendsChildEventListener' will be null...
        if (totalGroupFriendsChildEventListener != null){
            //remove child event listener from getTotalGroupFriends
            //This condition will only true for group chat
            String groupKey = key; //here key means groupKey
            groupFriendsDatabaseRef.child(groupKey).removeEventListener(totalGroupFriendsChildEventListener);
        }
        ///////////////////////////////////////////////////////

            messagesDatabaseRef.child(currentUser.getUid()).child(key).removeEventListener(testValueEventListener);
    }




    private void showChatMessages(){
        messageList.clear();
       testValueEventListener = messagesDatabaseRef.child(currentUser.getUid()).child(key)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {
                            Message message = snapshot.getValue(Message.class);
                            if (message != null) {
                                message.setMessageKey(snapshot.getKey());
                                messageList.add(message);
                                recyclerViewAdapter.notifyDataSetChanged();
                                chatRecyclerView.smoothScrollToPosition(recyclerViewAdapter.getItemCount());
                                if (friendType.equals(Constant.SINGLE_FRIEND)) {
                                    setMessageReadStatusForOneToOneChat(message);
                                }else if (friendType.equals(Constant.GROUP_FRIEND)){
                                    setMessageReadStatusForGroupChat(message, key);
                                }
                            }

                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()){
                                Message message = snapshot.getValue(Message.class);
                                if (message != null) {
                                    message.setMessageKey(snapshot.getKey());
                                    if (message.isRead() && messageList.contains(message)) {
                                        int elementPosition = messageList.indexOf(message);
                                        messageList.set(elementPosition, message);
                                        recyclerViewAdapter.notifyItemChanged(elementPosition);
                                    }
                                }
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    private void setMessageReadStatusForOneToOneChat(Message message){
        String senderUid = message.getSenderUid();
            if (senderUid.equals(messageReceiverId)  && ! message.isRead()){
                //Means current user receive message from his friend. So set the message read status in both the messages
                String messageKey = message.getMessageKey();
                Map<String, Object> messageMap = new HashMap<>();
                //current user read the received message
                messageMap.put(messageSenderId + "/" + messageReceiverId + "/" + messageKey + "/" + "read", true);
                messageMap.put(messageReceiverId + "/" + messageSenderId + "/" + messageKey + "/" + "read", true);
                messagesDatabaseRef.updateChildren(messageMap);
        }
    }


    private void setMessageReadStatusForGroupChat(Message message, String groupKey){
        String groupMessageSenderUid = message.getSenderUid();
        String currentUserId = messageSenderId;
        if (! groupMessageSenderUid.equals(currentUserId) && ! message.isRead()){
            //if message sender id is not equal to current user id. Means current user send the message
            String messageKey = message.getMessageKey();
            Map<String, Object> messageMap = new HashMap<>();
            //current user read the received message
            messageMap.put(currentUserId + "/" + groupKey + "/" + messageKey + "/" + "read", true);
            messageMap.put(groupMessageSenderUid + "/" + groupKey + "/" + messageKey + "/" + "read", true);
            messagesDatabaseRef.updateChildren(messageMap);
        }

    }



    private void updateUserStatus(boolean isOnline){
        Map<String, Object> userStatusMap = new HashMap<>();
        UserStatus userStatus = new UserStatus(isOnline, System.currentTimeMillis());
        DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        userStatusMap.put("users_detail_info" + "/" + currentUser.getUid() + "/" + "userStatus", userStatus);
        //update user's states
        rootDatabaseRef.updateChildren(userStatusMap);
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
                                friendType = friend.getFriendsType();
                                //set friend type in recycler view adapter class
                                recyclerViewAdapter.setFriendType(friendType);
                                if (friendType.equals(Constant.SINGLE_FRIEND)){
                                    messageReceiverId = key;
                                    //means one to one friendship
                                    fetchFriendsData(key);
                                    //friend is online or not
                                    fetchFriendOnlineStatus(key);
                                }else if (friendType.equals(Constant.GROUP_FRIEND)){
                                    groupUid = key;
                                    //means group friendship
                                    fetchGroupData(key);
                                    getTotalFriendsFromGroup(key);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }




    private void getTotalFriendsFromGroup(String groupKey){
        if (groupKey != null){
            totalGroupFriendsChildEventListener = groupFriendsDatabaseRef.child(groupKey)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            if (snapshot.exists()){
                                //add all group friends uid
                                totalGroupFriendsList.add(snapshot.getKey());
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                String key = snapshot.getKey();
                                totalGroupFriendsList.remove(key);
                            }

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
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
                                profileImageUrl = userProfile.getProfileImage();
                                if (!profileImageUrl.equalsIgnoreCase("default")) {
                                    Picasso.get().load(profileImageUrl).placeholder(R.drawable.user_icon).into(profileImageView);
                                } else {
                                    Picasso.get().load(R.drawable.user_icon).placeholder(R.drawable.user_icon).into(profileImageView);
                                }
                                ////////////////
                                recyclerViewAdapter.setProfileImageUrl(profileImageUrl);
                                showChatMessages();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void fetchFriendOnlineStatus(String friendKey){
        friendStatusDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users_detail_info").child(friendKey).child("userStatus");
        friendStatusValueEventListener = friendStatusDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserStatus userStatus = snapshot.getValue(UserStatus.class);
                    if (userStatus != null){
                        boolean isOnline = userStatus.isOnline();
                        long lastSeen = userStatus.getLastSeen();
                        if (isOnline){
                            //Friend is online
                            onlineStatusTextView.setText("online");
                        }else {
                            //Friend is offline
                            String onlineStatus = GetTimeAgo.getTimeAgo(lastSeen, getApplicationContext());
                            onlineStatusTextView.setText(onlineStatus);
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
                                onlineStatusTextView.setText("group");
                                String profileImageUrl = groupProfile.getGroupProfileImage();
                                if (!profileImageUrl.equalsIgnoreCase("default")) {
                                    Picasso.get().load(profileImageUrl).placeholder(R.drawable.ic_group_icon_white).into(profileImageView);
                                } else {
                                    Picasso.get().load(R.drawable.ic_group_icon_white).placeholder(R.drawable.ic_group_icon_white).into(profileImageView);
                                }
                                /////////////////////
                                showChatMessages();

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}

