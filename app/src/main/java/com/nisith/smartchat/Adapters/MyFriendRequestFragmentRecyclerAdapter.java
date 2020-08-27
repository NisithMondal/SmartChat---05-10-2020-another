package com.nisith.smartchat.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Constant;
import com.nisith.smartchat.Model.FriendRequest;
import com.nisith.smartchat.Model.GroupProfile;
import com.nisith.smartchat.Model.UserProfile;
import com.nisith.smartchat.Model.ValueEventListenerModel;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyFriendRequestFragmentRecyclerAdapter extends RecyclerView.Adapter<MyFriendRequestFragmentRecyclerAdapter.MyViewHolder> {

    public interface OnRequestButtonClickListener{
        //Here search name is required to perform searching operation
        void onRequestButtonClick(View view, FriendRequest friendRequest, String searchName);
    }

    private OnRequestButtonClickListener requestButtonClickListener;
    private Fragment fragment;
    private List<FriendRequest> friendRequestList;
    private DatabaseReference userDatabaseRef, groupsDatabaseRef;
    private String currentUserId;
    private List<ValueEventListenerModel> removeListenerFromSingleFriendList, removeListenerFromGroupFriendList;

    public MyFriendRequestFragmentRecyclerAdapter(Fragment fragment, List<FriendRequest> friendRequestList) {
        this.fragment = fragment;
        this.friendRequestList = friendRequestList;
        this.requestButtonClickListener = (OnRequestButtonClickListener) fragment;
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        groupsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        removeListenerFromSingleFriendList = new ArrayList<>();
        removeListenerFromGroupFriendList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_appearence_for_friend_request_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        FriendRequest friendRequest = friendRequestList.get(position);
        if (friendRequest.getRequestType().equals(Constant.SEND_REQUEST)) {
            holder.requestTypeTextView.setText("Request Send");
            holder.requestTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null, fragment.getActivity().getDrawable(R.drawable.ic_arrow_up_icon),null);
            holder.acceptRequestButton.setVisibility(View.INVISIBLE);
            holder.declineRequestButton.setText("Cancel Req.");
            holder.declineRequestButton.setBackground(fragment.getActivity().getDrawable(R.drawable.button_background_shape4));
        }else {
            holder.requestTypeTextView.setText("Request Received");
            holder.requestTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null, fragment.getActivity().getDrawable(R.drawable.ic_down_arrow),null);
            holder.acceptRequestButton.setText("Accept");
            holder.acceptRequestButton.setVisibility(View.VISIBLE);
            holder.declineRequestButton.setText("Decline");
            holder.declineRequestButton.setBackground(fragment.getActivity().getDrawable(R.drawable.button_background_shape3));
        }

        String uId = friendRequest.getSenderOrReceiverUid(); //key may be friend key or group key
        if (! friendRequest.isGroup()){
            //not a group friend request i.e. one to one chat friend request
            //here key is friend key
            holder.groupRequestTextView.setVisibility(View.INVISIBLE);
            userDatabaseRef.child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        if (userProfile != null) {
                            String userName = userProfile.getUserName();
                            holder.profileNameTextView.setText(userName);
                            String imageUrl = userProfile.getProfileImage();
                            if (!imageUrl.equalsIgnoreCase("default")) {
                                Picasso.get().load(imageUrl).placeholder(R.drawable.default_user_icon).into(holder.profileImageView);
                            } else {
                                Picasso.get().load(R.drawable.default_user_icon).placeholder(R.drawable.default_user_icon).into(holder.profileImageView);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

//            removeListenerFromSingleFriendList.add(new ValueEventListenerModel(uId, singleValueEventListener));


        }else {
            //Friend request for group
            holder.groupRequestTextView.setVisibility(View.VISIBLE);
            userDatabaseRef.child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        if (userProfile != null) {
                            String userName = userProfile.getUserName();
                            holder.profileNameTextView.setText(userName);
                            String imageUrl = userProfile.getProfileImage();
                            if (!imageUrl.equalsIgnoreCase("default")) {
                                Picasso.get().load(imageUrl).placeholder(R.drawable.ic_group_icon1).into(holder.profileImageView);
                            } else {
                                Picasso.get().load(R.drawable.ic_group_icon1).placeholder(R.drawable.ic_group_icon1).into(holder.profileImageView);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
//            removeListenerFromGroupFriendList.add(new ValueEventListenerModel(uId, groupValueEventListener));
        }
    }

    @Override
    public int getItemCount() {
        int totalItems = 0;
        if (friendRequestList != null){
            totalItems = friendRequestList.size();
        }
        return totalItems;
    }

//
//    public void removeValueEventListener(){
//        //remove all value event listener from friends
//        for (ValueEventListenerModel model : removeListenerFromSingleFriendList){
//            userDatabaseRef.child(model.getKey()).removeEventListener(model.getValueEventListener());
//        }
//        removeListenerFromSingleFriendList.clear();
//        //remove value event listeners from groups
//        for (ValueEventListenerModel model : removeListenerFromGroupFriendList){
//            userDatabaseRef.child(model.getKey()).removeEventListener(model.getValueEventListener());
//        }
//        removeListenerFromGroupFriendList.clear();
//    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImageView;
        TextView profileNameTextView, requestTypeTextView, groupRequestTextView;
        Button acceptRequestButton, declineRequestButton;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            profileNameTextView = itemView.findViewById(R.id.user_name_text_view);
            requestTypeTextView = itemView.findViewById(R.id.request_type_text_view);
            groupRequestTextView = itemView.findViewById(R.id.group_request_text_view);
            acceptRequestButton = itemView.findViewById(R.id.accept_request_button);
            declineRequestButton = itemView.findViewById(R.id.decline_request_button);
            itemView.setOnClickListener(new MyClickListener(this));
            profileImageView.setOnClickListener(new MyClickListener(this));
            acceptRequestButton.setOnClickListener(new MyClickListener(this));
            declineRequestButton.setOnClickListener(new MyClickListener(this));

        }
    }

    class MyClickListener implements View.OnClickListener{
        private MyViewHolder myViewHolder;
        public MyClickListener(MyViewHolder myViewHolder){
            this.myViewHolder = myViewHolder;
        }
        @Override
        public void onClick(final View view) {
            final FriendRequest friendRequest = friendRequestList.get(myViewHolder.getAbsoluteAdapterPosition());
            if (friendRequest.getRequestType().equals(Constant.RECEIVE_REQUEST)){
                if (! friendRequest.isGroup()){
                    //one to one friend request received
                    userDatabaseRef.child(friendRequest.getSenderOrReceiverUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                                if (userProfile != null) {
                                    String  friendName = "";
                                    //name of the friend who send this request
                                    friendName = userProfile.getUserName();
                                    requestButtonClickListener.onRequestButtonClick(view, friendRequest, friendName);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }else {
                    //group Request receive
                    groupsDatabaseRef.child(friendRequest.getGroupKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                GroupProfile groupProfile = snapshot.getValue(GroupProfile.class);
                                if (groupProfile != null){
                                    String groupName = "";
                                    groupName = groupProfile.getGroupName();
                                    requestButtonClickListener.onRequestButtonClick(view, friendRequest, groupName);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }else if (friendRequest.getRequestType().equals(Constant.SEND_REQUEST)){
                //here search name is not necessary
                //current user send friend request
                requestButtonClickListener.onRequestButtonClick(view, friendRequest, "");
            }
        }
    }
}
