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
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.Model.UserStatus;
import com.nisith.smartchat.Model.ValueEventListenerModel;
import com.nisith.smartchat.Model.UserProfile;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyFriendFragmentRecyclerAdapter extends FirebaseRecyclerAdapter<Friend, MyFriendFragmentRecyclerAdapter.MyViewHolder> {

    public interface OnFriendFragmentViewsClickListener {
        void onFriendViewsClick(View view, String friendUid);
    }

    private String currentUser;
    private DatabaseReference userDatabaseRef, groupDatabaseRef, userDetailInfoDatabaseRef;
    private OnFriendFragmentViewsClickListener friendCardViewsClickListener;
    private List<ValueEventListenerModel> removeListenerFromFriendList, removeListenerFromFriendOnlineStatusList;

    public MyFriendFragmentRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Friend> options, OnFriendFragmentViewsClickListener friendCardViewsClickListener) {

        super(options);
        this.friendCardViewsClickListener = friendCardViewsClickListener;
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        groupDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups");
        userDetailInfoDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users_detail_info");
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.removeListenerFromFriendList = new ArrayList<>();
        this.removeListenerFromFriendOnlineStatusList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_appearence_for_friend_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull Friend friend) {
            final String friendKey = getRef(position).getKey();
            ValueEventListener friendValueEventListener = userDatabaseRef.child(friendKey)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                                if (userProfile != null) {
                                    String userName = userProfile.getUserName();
                                    String userStatus = userProfile.getUserStatus();
                                    holder.profileNameTextView.setText(userName);
                                    holder.userStatusTextView.setText(userStatus);
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
         removeListenerFromFriendList.add(new ValueEventListenerModel(friendKey, friendValueEventListener));

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


    public void removeValueEventListener(){
        //To remove all value event listener
        for (ValueEventListenerModel model : removeListenerFromFriendList){
            userDatabaseRef.child(model.getKey()).removeEventListener(model.getValueEventListener());
        }
        removeListenerFromFriendList.clear();

        for (ValueEventListenerModel model : removeListenerFromFriendOnlineStatusList){
            userDetailInfoDatabaseRef.child(model.getKey()).child("userStatus").removeEventListener(model.getValueEventListener());
        }
        removeListenerFromFriendOnlineStatusList.clear();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImageView;
        TextView profileNameTextView, userStatusTextView;
        ImageView onlineStatusImageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            profileNameTextView = itemView.findViewById(R.id.user_name_text_view);
            userStatusTextView = itemView.findViewById(R.id.user_status_text_view);
            onlineStatusImageView = itemView.findViewById(R.id.online_state_image_view);

            //Click Listeners
            profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    friendCardViewsClickListener.onFriendViewsClick(view, getRef(getAbsoluteAdapterPosition()).getKey());
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    friendCardViewsClickListener.onFriendViewsClick(view, getRef(getAbsoluteAdapterPosition()).getKey());
                }
            });

        }
    }



}
