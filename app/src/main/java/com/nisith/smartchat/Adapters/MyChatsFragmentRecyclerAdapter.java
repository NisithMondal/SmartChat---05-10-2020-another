package com.nisith.smartchat.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Constant;
import com.nisith.smartchat.Fragments.ChatFragment;
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.Model.GroupProfile;
import com.nisith.smartchat.Model.Message;
import com.nisith.smartchat.Model.UnreadChatsModel;
import com.nisith.smartchat.Model.UserProfile;
import com.nisith.smartchat.Model.UserStatus;
import com.nisith.smartchat.Model.ValueEventListenerModel;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyChatsFragmentRecyclerAdapter extends FirebaseRecyclerAdapter<Friend, MyChatsFragmentRecyclerAdapter.MyViewHolder> {

    public interface OnChatsFragmentViewsClickListener {
        void onViewClick(View view, String key);  //key may be friend key or group key
    }

    private ChatFragment chatFragment;
    private String currentUserUid;
    private DatabaseReference userDatabaseRef, groupDatabaseRef, messagesRootDatabaseRef, userDetailInfoDatabaseRef;
    private OnChatsFragmentViewsClickListener chatsCardViewsClickListener;
    private List<UnreadChatsModel> unreadChatsModelList;
    private List<ValueEventListenerModel> removeListenerFromFriendOnlineStatusList, removeListenerFromLastChatMessagesList;
    private List<ValueEventListenerModel> removeListenerFromSingleFriendList; //value event listener for friends key
    private List<ValueEventListenerModel> removeListenerFromGroupFriendList, removeValueEventListenerFromUnreadMessagesForFriends; //value event listener for groups key

    public MyChatsFragmentRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Friend> options, ChatFragment chatFragment) {

        super(options);
        this.chatsCardViewsClickListener = chatFragment;
        this.chatFragment = chatFragment;
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        userDetailInfoDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users_detail_info");
        groupDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups");
        messagesRootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("messages");
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        unreadChatsModelList = new ArrayList<>();
        this.removeListenerFromFriendOnlineStatusList = new ArrayList<>();
        this.removeListenerFromLastChatMessagesList = new ArrayList<>();
        this.removeListenerFromSingleFriendList = new ArrayList<>();
        this.removeListenerFromGroupFriendList = new ArrayList<>();
        this.removeValueEventListenerFromUnreadMessagesForFriends = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_appearence_for_chat_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull Friend friend) {
        createAllRows(position);
        if (friend.getFriendsType().equals(Constant.SINGLE_FRIEND)) {
            // this means value event listener is add on one to one friend chat
            final String userKey = getRef(position).getKey();
            getLastMessageOfPrivateOrGroupChat(userKey, holder);
            holder.totalUnreadMessageTextView.setVisibility(View.GONE);
            getTotalUnreadMessagesCount(userKey, holder, Constant.SINGLE_FRIEND);
            //Friends online state is only shows for SINGLE_FRIEND not for GROUP_FRIEND...
            getFriendsOnlineStates(userKey, holder);
            ValueEventListener valueEventListener = userDatabaseRef.child(userKey)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                                if (userProfile != null) {
                                    String userName = userProfile.getUserName();
                                    holder.profileNameTextView.setText(userName);
                                    String imageUrl = userProfile.getProfileImage();
                                    if (!imageUrl.equalsIgnoreCase("default")) {
                                        Picasso.get().load(imageUrl).placeholder(R.drawable.user_icon).into(holder.profileImageView);
                                    } else {
                                        Picasso.get().load(R.drawable.user_icon).placeholder(R.drawable.user_icon).into(holder.profileImageView);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            // add value event listener for friends key
            removeListenerFromSingleFriendList.add(new ValueEventListenerModel(userKey, valueEventListener));
        }else {
            //this means value event listener is add on group chats key
            final String groupKey = getRef(position).getKey();
            getLastMessageOfPrivateOrGroupChat(groupKey, holder);
            holder.totalUnreadMessageTextView.setVisibility(View.GONE);
            getTotalUnreadMessagesCount(groupKey, holder, Constant.GROUP_FRIEND);
            ValueEventListener groupValueEventListener = groupDatabaseRef.child(groupKey)
                 .addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                         if (snapshot.exists()) {
                             GroupProfile groupProfile = snapshot.getValue(GroupProfile.class);
                             if (groupProfile != null) {
                                 String groupName = groupProfile.getGroupName();
                                 holder.profileNameTextView.setText(groupName);
                                 // We not showing online status in group chat row
                                 holder.onlineStatusImageView.setVisibility(View.GONE);
                                 String imageUrl = groupProfile.getGroupProfileImage();
                                 if (!imageUrl.equalsIgnoreCase("default")) {
                                     Picasso.get().load(imageUrl).placeholder(R.drawable.group_icon).into(holder.profileImageView);
                                 } else {
                                     Picasso.get().load(R.drawable.group_icon).placeholder(R.drawable.group_icon).into(holder.profileImageView);
                                 }
                             }
                         }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError error) {

                     }
                 });
            //add value event listener for groups key
            removeListenerFromGroupFriendList.add(new ValueEventListenerModel(groupKey, groupValueEventListener));
        }

    }

    private void createAllRows(int position){
        //To create all rows of recycler view. This is necessary for count all unread messages of the current user...
        if (position == getItemCount()-1){
            chatFragment.setRecyclerViewPositionToScroll(0);
        }else {
            chatFragment.setRecyclerViewPositionToScroll(getItemCount());
        }
    }


    private void getTotalUnreadMessagesCount(final String key, final MyViewHolder holder, final String friendType){
    //Key may be friend_key or group_key
    //This method will give us all unread messages number of every single friend. This method will not give unread messages for group...
     final String messageSenderUid = currentUserUid;
     ValueEventListener  valueEventListener = messagesRootDatabaseRef.child(messageSenderUid).child(key).orderByChild("read").equalTo(false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       int totalUnreadMessageCount = 0;
                       for (DataSnapshot snapshot1 : snapshot.getChildren()){
                           Message message = snapshot1.getValue(Message.class);
                           if (message != null) {

                               if (! message.getSenderUid().equals(currentUserUid)){
                                   totalUnreadMessageCount++;
                               }
                           }
                       }
                       if (totalUnreadMessageCount != 0) {
                           holder.totalUnreadMessageTextView.setVisibility(View.VISIBLE);
                           holder.totalUnreadMessageTextView.setText("" + totalUnreadMessageCount);
                       }else {
                           holder.totalUnreadMessageTextView.setVisibility(View.GONE);
                       }
                        //To count total unread Chats
                        UnreadChatsModel unreadChatsModel = new UnreadChatsModel(key);
                        if (! unreadChatsModelList.contains(unreadChatsModel) && totalUnreadMessageCount>0){
                            unreadChatsModelList.add(unreadChatsModel);
                        }
                        int totalUnreadChats = unreadChatsModelList.size();
                        if (totalUnreadChats != 0){
                            chatFragment.setTotalUnreadChats(""+totalUnreadChats, View.VISIBLE);
                        }else {
                            chatFragment.setTotalUnreadChats("", View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
     removeValueEventListenerFromUnreadMessagesForFriends.add(new ValueEventListenerModel(key, valueEventListener));
    }



    private void getFriendsOnlineStates(String friendKey, final MyViewHolder holder){
        //Friends online state is only shows for SINGLE_FRIEND not for GROUP_FRIEND...
        //Perform operation if friends are online or not. This info is fetch from 'user_details_info' database node.
        //I store user status in this database
        ValueEventListener statusValueEventListener = userDetailInfoDatabaseRef.child(friendKey).child("userStatus")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserStatus userStatus = snapshot.getValue(UserStatus.class);
                            if (userStatus != null){
                                boolean isOnline = userStatus.isOnline();
                                if (isOnline){
                                    holder.onlineStatusImageView.setVisibility(View.VISIBLE);
                                }else {
                                    holder.onlineStatusImageView.setVisibility(View.GONE);
                                }
                            }else {
                                //If data not available for any reason make online status invisible
                                holder.onlineStatusImageView.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        removeListenerFromFriendOnlineStatusList.add(new ValueEventListenerModel(friendKey, statusValueEventListener));
    }



    private void getLastMessageOfPrivateOrGroupChat(String key, final MyViewHolder holder){

        //Here 'key' may be friend_key or group_key...
        //This method fetch the last message of any group or friends of the current users
      ValueEventListener valueEventListener = messagesRootDatabaseRef.child(currentUserUid).child(key).orderByKey().limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                Message message = snapshot1.getValue(Message.class);
                                if (message != null){
                                    holder.lastMessageTextView.setText(message.getMessage());
                                    String currentDate = getCurrentDate();
                                    if (currentDate.equals(message.getDate())){
                                        /* Last message date is today. So we only show the time of last message. If last message date is not today,
                                           we simply show message date */
                                        holder.lastMessageDateTextView.setText(message.getTime());
                                    }else {
                                        //Last message date is not today. So simply set date of the last message
                                        holder.lastMessageDateTextView.setText(message.getDate());
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

      removeListenerFromLastChatMessagesList.add(new ValueEventListenerModel(key, valueEventListener));

    }


    private String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("MMM dd, yyyy");
        return date.format(calendar.getTime());
    }




    public void removeValueEventListener(){
        //remove all value event listener from friends
        for (ValueEventListenerModel model : removeListenerFromSingleFriendList){
            userDatabaseRef.child(model.getKey()).removeEventListener(model.getValueEventListener());
        }
        removeListenerFromSingleFriendList.clear();
        //remove value event listeners from groups
        for (ValueEventListenerModel model : removeListenerFromGroupFriendList){
            groupDatabaseRef.child(model.getKey()).removeEventListener(model.getValueEventListener());
        }
        removeListenerFromGroupFriendList.clear();

        for (ValueEventListenerModel model : removeValueEventListenerFromUnreadMessagesForFriends) {
            messagesRootDatabaseRef.child(currentUserUid).child(model.getKey()).removeEventListener(model.getValueEventListener());
        }
        removeValueEventListenerFromUnreadMessagesForFriends.clear();

        for (ValueEventListenerModel model : removeListenerFromFriendOnlineStatusList){
            userDetailInfoDatabaseRef.child(model.getKey()).child("userStatus").removeEventListener(model.getValueEventListener());
        }
        removeListenerFromFriendOnlineStatusList.clear();

        int i = 1;
        for (ValueEventListenerModel model : removeListenerFromLastChatMessagesList){
            messagesRootDatabaseRef.child(currentUserUid).child(model.getKey()).removeEventListener(model.getValueEventListener());
            Log.d("MNBVCX","i = "+i);
            i++;
        }
        removeListenerFromLastChatMessagesList.clear();

        unreadChatsModelList.clear();

    }



    class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImageView;
        TextView profileNameTextView, lastMessageTextView, lastMessageDateTextView, totalUnreadMessageTextView;
        ImageView onlineStatusImageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            profileNameTextView = itemView.findViewById(R.id.user_name_text_view);
            lastMessageTextView = itemView.findViewById(R.id.last_message_text_view);
            lastMessageDateTextView = itemView.findViewById(R.id.last_message_date_text_view);
            totalUnreadMessageTextView = itemView.findViewById(R.id.total_unread_message_text_view);
            onlineStatusImageView = itemView.findViewById(R.id.online_state_image_view);

            //Click Listeners
            profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    chatsCardViewsClickListener.onViewClick(view, getRef(getAbsoluteAdapterPosition()).getKey());
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chatsCardViewsClickListener.onViewClick(view, getRef(getAbsoluteAdapterPosition()).getKey());
                }
            });

        }
    }



}
