package com.nisith.smartchat.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Constant;
import com.nisith.smartchat.Model.FriendRequest;
import com.nisith.smartchat.Model.UserProfile;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyFriendRequestFragmentRecyclerAdapter extends RecyclerView.Adapter<MyFriendRequestFragmentRecyclerAdapter.MyViewHolder> {

    public interface OnRequestButtonClickListener{
        void onRequestButtonClick(View view);
    }

    private OnRequestButtonClickListener requestButtonClickListener;
    private Fragment fragment;
    private List<FriendRequest> friendRequestList;
    private DatabaseReference userDatabaseRef;

    public MyFriendRequestFragmentRecyclerAdapter(Fragment fragment, List<FriendRequest> friendRequestList) {
        this.fragment = fragment;
        this.friendRequestList = friendRequestList;
        this.requestButtonClickListener = (OnRequestButtonClickListener) fragment;
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
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
            holder.acceptRequestButton.setVisibility(View.GONE);
            holder.declineRequestButton.setText("Cancel Request");
            holder.declineRequestButton.setBackground(fragment.getActivity().getDrawable(R.drawable.button_background_shape4));
        }else {
            holder.requestTypeTextView.setText("Request Received");
            holder.requestTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null, fragment.getActivity().getDrawable(R.drawable.ic_down_arrow),null);
            holder.acceptRequestButton.setText("Accept");
            holder.acceptRequestButton.setVisibility(View.VISIBLE);
            holder.declineRequestButton.setText("Decline");
            holder.declineRequestButton.setBackground(fragment.getActivity().getDrawable(R.drawable.button_background_shape3));
        }
        String userId = friendRequest.getFriendKey();
        userDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    String userName = userProfile.getUserName();
                    holder.profileNameTextView.setText(userName);
                    String imageUrl = userProfile.getProfileImage();
                    if (!imageUrl.equalsIgnoreCase("default")) {
                        Picasso.get().load(imageUrl).placeholder(R.drawable.user_icon).into(holder.profileImageView);
                    } else {
                        Picasso.get().load(R.drawable.user_icon).into(holder.profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        int totalItems = 0;
        if (friendRequestList != null){
            totalItems = friendRequestList.size();
        }
        return totalItems;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImageView;
        TextView profileNameTextView, requestTypeTextView;
        Button acceptRequestButton, declineRequestButton;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            profileNameTextView = itemView.findViewById(R.id.user_name_text_view);
            requestTypeTextView = itemView.findViewById(R.id.request_type_text_view);
            acceptRequestButton = itemView.findViewById(R.id.accept_request_button);
            declineRequestButton = itemView.findViewById(R.id.decline_request_button);
            itemView.setOnClickListener(new MyClickListener());
            profileImageView.setOnClickListener(new MyClickListener());
            acceptRequestButton.setOnClickListener(new MyClickListener());
            declineRequestButton.setOnClickListener(new MyClickListener());
        }
    }

    class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
           requestButtonClickListener.onRequestButtonClick(view);
        }
    }
}
