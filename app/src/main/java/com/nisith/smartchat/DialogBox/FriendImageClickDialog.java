package com.nisith.smartchat.DialogBox;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.ChatActivity;
import com.nisith.smartchat.Constant;
import com.nisith.smartchat.FriendsProfileActivity;
import com.nisith.smartchat.GroupProfileActivity;
import com.nisith.smartchat.ImageDisplayActivity;
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.Model.GroupProfile;
import com.nisith.smartchat.Model.UserProfile;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class FriendImageClickDialog extends DialogFragment {

    private TextView nameTextView;
    private ImageView profileImageView;
    private String name, profileImageUrl, key, friendType;


    public FriendImageClickDialog(final String key){
        this.key = key;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserId).child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Friend friend = snapshot.getValue(Friend.class);
                            if (friend != null){
                                friendType = friend.getFriendsType();
                                if (friendType != null && friendType.equals(Constant.SINGLE_FRIEND)){
                                    //means one to one friendship
                                    fetchFriendsData(key);
                                }else if (friendType != null && friendType.equals(Constant.GROUP_FRIEND)){
                                    //means group friendship
                                    fetchGroupData(key);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



    }


    private void fetchFriendsData(String friendKey){
        //key is a friend's uid
        FirebaseDatabase.getInstance().getReference().child("users").child(friendKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserProfile userProfile = snapshot.getValue(UserProfile.class);
                            if (userProfile != null && nameTextView != null){
                                name = userProfile.getUserName();
                                nameTextView.setText(name);
                                profileImageUrl = userProfile.getProfileImage();
                                if (!profileImageUrl.equalsIgnoreCase("default")) {
                                    Picasso.get().load(profileImageUrl).fit().centerCrop().placeholder(R.drawable.user_icon).into(profileImageView);
                                } else {
                                    Picasso.get().load(R.drawable.user_icon).into(profileImageView);
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
        //key is a group's key
        FirebaseDatabase.getInstance().getReference().child("groups").child(groupKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            GroupProfile groupProfile = snapshot.getValue(GroupProfile.class);
                            if (groupProfile != null && nameTextView != null){
                                name = groupProfile.getGroupName();
                                nameTextView.setText(name);
                                profileImageUrl = groupProfile.getGroupProfileImage();
                                if (!profileImageUrl.equalsIgnoreCase("default")) {
                                    Picasso.get().load(profileImageUrl).fit().centerCrop().placeholder(R.drawable.group_icon).into(profileImageView);
                                } else {
                                    Picasso.get().load(R.drawable.group_icon).placeholder(R.drawable.group_icon).into(profileImageView);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.image_click_popup_dialog_layout, null);
        nameTextView = view.findViewById(R.id.user_name_text_view);
        profileImageView = view.findViewById(R.id.profile_image_view);
        ImageView infoImageView = view.findViewById(R.id.info_image_view);
        ImageView chatImageView = view.findViewById(R.id.chat_image_view);
        //Click Listeners
        profileImageView.setOnClickListener(new MyViewClickListener());
        infoImageView.setOnClickListener(new MyViewClickListener());
        chatImageView.setOnClickListener(new MyViewClickListener());
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setView(view);

        return dialogBuilder.create();
    }



    class MyViewClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.profile_image_view:
                    if (friendType != null) {
                        Intent intent = new Intent(getContext(), ImageDisplayActivity.class);
                        intent.putExtra(Constant.USER_NAME, name);
                        intent.putExtra(Constant.FRIENDS_TYPE, friendType);
                        intent.putExtra(Constant.PROFILE_IMAGE_URL, profileImageUrl);
                        startActivity(intent);
                    }
                    break;

                case R.id.info_image_view:
                    if (friendType != null) {
                        if (friendType.equals(Constant.SINGLE_FRIEND)) {
                            openFriendProfileActivity();
                        } else if (friendType.equals(Constant.GROUP_FRIEND)) {
                            openGroupProfileActivity();
                        }
                    }
                    break;

                case R.id.chat_image_view:
                    if (key != null) {
                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                        chatIntent.putExtra(Constant.KEY, key);
                        startActivity(chatIntent);
                    }
            }
        }
    }

    private void openFriendProfileActivity(){
        Intent infoIntent = new Intent(getContext(), FriendsProfileActivity.class);
        infoIntent.putExtra(Constant.FRIEND_UID, key);
        startActivity(infoIntent);
    }

    private void openGroupProfileActivity(){
        Intent infoIntent = new Intent(getContext(), GroupProfileActivity.class);
        infoIntent.putExtra(Constant.GROUP_KEY, key);
        startActivity(infoIntent);
    }

}
