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
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.Model.GroupProfile;
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

    private String currentUserUid;
    private DatabaseReference userDatabaseRef, groupDatabaseRef;
    private OnChatsFragmentViewsClickListener chatsCardViewsClickListener;
    private List<ValueEventListenerModel> removeListenerFromSingleFriendList; //value event listener for friends key
    private List<ValueEventListenerModel> removeListenerFromGroupFriendList; //value event listener for groups key

    public MyChatsFragmentRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Friend> options, OnChatsFragmentViewsClickListener chatsCardViewsClickListener) {

        super(options);
        this.chatsCardViewsClickListener = chatsCardViewsClickListener;
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        groupDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups");
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.removeListenerFromSingleFriendList = new ArrayList<>();
        this.removeListenerFromGroupFriendList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_appearence_for_chat_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull Friend friend) {
        if (friend.getFriendsType().equals(Constant.SINGLE_FRIEND)) {
            // this means value event listener is add on one to one friend chat
            final String userKey = getRef(position).getKey();
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
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImageView;
        TextView profileNameTextView, lastMessageTextView, lastSeenTextView;
        ImageView onlineStateImageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            profileNameTextView = itemView.findViewById(R.id.user_name_text_view);
            lastMessageTextView = itemView.findViewById(R.id.last_message_text_view);
            lastSeenTextView = itemView.findViewById(R.id.last_seen_text_view);
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
