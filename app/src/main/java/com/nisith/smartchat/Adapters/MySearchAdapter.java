package com.nisith.smartchat.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MySearchAdapter extends FirebaseRecyclerAdapter<Friend, MySearchAdapter.MyViewHolder> {


    public interface OnSearchItemsClickListener {
        void onViewClick(View view, String key, String friendType);  //key may be friend key or group key
    }

    private String currentUserId;
    private DatabaseReference usersDatabaseRef, groupsDatabaseRef, friendsDatabaseRef;
    private OnSearchItemsClickListener searchItemsClickListener;


    public MySearchAdapter(@NonNull FirebaseRecyclerOptions<Friend> options, AppCompatActivity appCompatActivity) {
        super(options);
        this.searchItemsClickListener = (OnSearchItemsClickListener) appCompatActivity;
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        groupsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups");
        friendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friends");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_appearence_for_find_friends_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull Friend friend) {
        String key = getRef(position).getKey();
        if (friend.getFriendsType().equals(Constant.SINGLE_FRIEND)){
            //single_friend
            //Here key is friend_key
            usersDatabaseRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        if (userProfile != null){
                            holder.userNameTextView.setText(userProfile.getUserName());
                            holder.userStatusTextView.setText(userProfile.getUserStatus());
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

        }else{
            //group_friend
            //Here key is group_key
            groupsDatabaseRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        GroupProfile groupProfile = snapshot.getValue(GroupProfile.class);
                        if (groupProfile != null){
                            holder.userNameTextView.setText(groupProfile.getGroupName());
                            holder.userStatusTextView.setText("Group");
                            String imageUrl = groupProfile.getGroupProfileImage();
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
        }


    }



    class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImageView;
        TextView userNameTextView, userStatusTextView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            userNameTextView = itemView.findViewById(R.id.user_name_text_view);
            userStatusTextView = itemView.findViewById(R.id.user_status_text_view);

            //Click Listeners
            profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String key = getRef(getAbsoluteAdapterPosition()).getKey();
                    searchItemsClickListener.onViewClick(view, key, null);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final String key = getRef(getAbsoluteAdapterPosition()).getKey();

                    friendsDatabaseRef.child(currentUserId).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Friend friend = snapshot.getValue(Friend.class);
                                if (friend != null){
                                    String friendType = friend.getFriendsType();
                                    if (friendType != null){
                                        searchItemsClickListener.onViewClick(view, key, friendType);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

        }
    }
}
