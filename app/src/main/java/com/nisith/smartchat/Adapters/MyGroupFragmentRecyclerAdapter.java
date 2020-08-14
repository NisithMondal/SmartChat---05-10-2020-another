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
import com.nisith.smartchat.Model.GroupProfile;
import com.nisith.smartchat.Model.ValueEventListenerModel;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyGroupFragmentRecyclerAdapter extends FirebaseRecyclerAdapter<Friend, MyGroupFragmentRecyclerAdapter.MyViewHolder> {

    public interface OnGroupFragmentViewsClickListener {
        void onFriendViewsClick(View view, String groupKey);
    }

    private String currentUser;
    private DatabaseReference groupsDatabaseRef;
    private OnGroupFragmentViewsClickListener groupFragmentViewsClickListener;
    private List<ValueEventListenerModel> removeListenerList;

    public MyGroupFragmentRecyclerAdapter(@NonNull FirebaseRecyclerOptions<Friend> options, OnGroupFragmentViewsClickListener groupFragmentViewsClickListener) {

        super(options);
        this.groupFragmentViewsClickListener = groupFragmentViewsClickListener;
        groupsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups");
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.removeListenerList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_appearence_for_groups_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull final Friend friend) {
        final String groupKey = getRef(position).getKey();
        ValueEventListener valueEventListener = groupsDatabaseRef.child(groupKey)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                GroupProfile groupProfile = snapshot.getValue(GroupProfile.class);
                                if (groupProfile != null) {
                                    String groupName = groupProfile.getGroupName();
                                    int totalGroupFriends = groupProfile.getTotalGroupFriends();
                                    holder.groupNameTextView.setText(groupName);
                                    String text = String.valueOf(totalGroupFriends);
                                    if (totalGroupFriends == 1){
                                        text =  text + " Friend";
                                    }else {
                                        text = text + " Friends";
                                    }
                                    holder.totalGroupFriendsTextView.setText(text);
                                    String imageUrl = groupProfile.getGroupProfileImage();
                                    if (!imageUrl.equalsIgnoreCase("default")) {
                                        Picasso.get().load(imageUrl).placeholder(R.drawable.group_icon).into(holder.groupProfileImageView);
                                    } else {
                                        Picasso.get().load(R.drawable.group_icon).placeholder(R.drawable.group_icon).into(holder.groupProfileImageView);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        removeListenerList.add(new ValueEventListenerModel(groupKey, valueEventListener));

    }


    public void removeValueEventListener(){
        //To remove all value event listener
        for (ValueEventListenerModel model : removeListenerList){
            groupsDatabaseRef.child(model.getKey()).removeEventListener(model.getValueEventListener());
        }
        removeListenerList.clear();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView groupProfileImageView;
        TextView groupNameTextView, totalGroupFriendsTextView;
        ImageView onlineStateImageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            groupProfileImageView = itemView.findViewById(R.id.group_profile_image_view);
            groupNameTextView = itemView.findViewById(R.id.group_name_text_view);
            totalGroupFriendsTextView = itemView.findViewById(R.id.total_group_friends_text_view);
            //Click Listeners
            groupProfileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    groupFragmentViewsClickListener.onFriendViewsClick(view, getRef(getAbsoluteAdapterPosition()).getKey());
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    groupFragmentViewsClickListener.onFriendViewsClick(view, getRef(getAbsoluteAdapterPosition()).getKey());
                }
            });

        }
    }



}
