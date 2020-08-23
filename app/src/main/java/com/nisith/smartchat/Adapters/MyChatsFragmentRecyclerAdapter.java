package com.nisith.smartchat.Adapters;

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
import com.nisith.smartchat.Model.ValueEventListenerModel;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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
    private DatabaseReference userDatabaseRef, groupDatabaseRef, messagesDatabaseRef;
    private OnChatsFragmentViewsClickListener chatsCardViewsClickListener;
    private List<UnreadChatsModel> unreadChatsModelList;
    private List<ValueEventListenerModel> removeListenerFromSingleFriendList; //value event listener for friends key
    private List<ValueEventListenerModel> removeListenerFromGroupFriendList, removeValueEventListenerFromUnreadMessagesForFriends; //value event listener for groups key

    public MyChatsFragmentRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Friend> options, ChatFragment chatFragment) {

        super(options);
        this.chatsCardViewsClickListener = chatFragment;
        this.chatFragment = chatFragment;
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        groupDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups");
        messagesDatabaseRef = FirebaseDatabase.getInstance().getReference().child("messages");
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        unreadChatsModelList = new ArrayList<>();
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
            holder.totalUnreadMessageTextView.setVisibility(View.GONE);
            getTotalUnreadMessages(userKey, holder, Constant.SINGLE_FRIEND);
            ValueEventListener valueEventListener = userDatabaseRef.child(userKey)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                                if (userProfile != null) {
                                    String userName = userProfile.getUserName();
                                    holder.profileNameTextView.setText(userName);
                                    holder.lastMessageTextView.setText("Hello there");
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
            holder.totalUnreadMessageTextView.setVisibility(View.GONE);
            getTotalUnreadMessages(groupKey, holder, Constant.GROUP_FRIEND);
            ValueEventListener groupValueEventListener = groupDatabaseRef.child(groupKey)
                 .addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                         if (snapshot.exists()) {
                             GroupProfile groupProfile = snapshot.getValue(GroupProfile.class);
                             if (groupProfile != null) {
                                 String groupName = groupProfile.getGroupName();
                                 holder.profileNameTextView.setText(groupName);
                                 holder.lastMessageTextView.setText("Last Message");
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


    private void getTotalUnreadMessages(final String key, final MyViewHolder holder, final String friendType){
    //Key may be friend_key or group_key
    //This method will give us all unread messages number of every single friend. This method will not give unread messages for group...
     final String messageSenderUid = currentUserUid;
     ValueEventListener  valueEventListener = messagesDatabaseRef.child(messageSenderUid).child(key).orderByChild("read").equalTo(false)
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
            messagesDatabaseRef.child(currentUserUid).child(model.getKey()).removeEventListener(model.getValueEventListener());
        }
        removeValueEventListenerFromUnreadMessagesForFriends.clear();
        unreadChatsModelList.clear();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImageView;
        TextView profileNameTextView, lastMessageTextView, lastSeenTextView, totalUnreadMessageTextView;
        ImageView onlineStateImageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            profileNameTextView = itemView.findViewById(R.id.user_name_text_view);
            lastMessageTextView = itemView.findViewById(R.id.last_message_text_view);
            lastSeenTextView = itemView.findViewById(R.id.last_seen_text_view);
            totalUnreadMessageTextView = itemView.findViewById(R.id.total_unread_message_text_view);
            onlineStateImageView = itemView.findViewById(R.id.online_state_image_view);

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
