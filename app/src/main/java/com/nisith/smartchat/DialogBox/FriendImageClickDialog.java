package com.nisith.smartchat.DialogBox;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.auto.value.AutoAnnotation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Model.UserProfile;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class FriendImageClickDialog extends DialogFragment {

    private TextView userNameTextView;
    private ImageView profileImageView;

    public FriendImageClickDialog(String uId){
        FirebaseDatabase.getInstance().getReference().child("users").child(uId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserProfile userProfile = snapshot.getValue(UserProfile.class);
                            if (userProfile != null){
                                String userName = userProfile.getUserName();
                                userNameTextView.setText(userName);
                                String profileImageUrl = userProfile.getProfileImage();
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
                    Toast.makeText(getContext(), "profile Image View", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.info_image_view:
                    Toast.makeText(getContext(), "info Image View", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.chat_image_view:
                    Toast.makeText(getContext(), "chat Image View", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
