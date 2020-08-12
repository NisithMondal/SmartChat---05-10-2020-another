package com.nisith.smartchat.DialogBox;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Constant;
import com.nisith.smartchat.FriendsProfileActivity;
import com.nisith.smartchat.ImageDisplayActivity;
import com.nisith.smartchat.Model.UserProfile;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ImageClickDialog extends DialogFragment {

    private TextView userNameTextView;
    private ImageView profileImageView;
    private String userName, profileImageUrl, uId;

    public ImageClickDialog(String uId){
        this.uId = uId;
        FirebaseDatabase.getInstance().getReference().child("users").child(uId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserProfile userProfile = snapshot.getValue(UserProfile.class);
                            if (userProfile != null){
                                userName = userProfile.getUserName();
                                userNameTextView.setText(userName);
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



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.image_click_popup_dialog_layout, null);
        userNameTextView = view.findViewById(R.id.user_name_text_view);
        profileImageView = view.findViewById(R.id.profile_image_view);
        ImageView infoImageView = view.findViewById(R.id.info_image_view);
        ImageView chatImageView = view.findViewById(R.id.chat_image_view);
        chatImageView.setVisibility(View.GONE);
        //Click Listeners
        profileImageView.setOnClickListener(new MyViewClickListener());
        infoImageView.setOnClickListener(new MyViewClickListener());
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setView(view);

        return dialogBuilder.create();
    }



    class MyViewClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.profile_image_view:
                    Intent intent = new Intent(getContext(), ImageDisplayActivity.class);
                    intent.putExtra(Constant.USER_NAME,userName);
                    intent.putExtra(Constant.PROFILE_IMAGE_URL,profileImageUrl);
                    startActivity(intent);
                    break;

                case R.id.info_image_view:
                    Intent infoIntent = new Intent(getContext(), FriendsProfileActivity.class);
                    infoIntent.putExtra(Constant.FRIEND_UID, uId);
                    startActivity(infoIntent);
            }
        }
    }

}
