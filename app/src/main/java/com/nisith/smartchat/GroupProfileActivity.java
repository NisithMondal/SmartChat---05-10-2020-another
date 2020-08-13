package com.nisith.smartchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupProfileActivity extends AppCompatActivity {
    private Toolbar appToolbar;
    private ImageView backgroundImageView;
    private CircleImageView groupProfileImageView;
    private TextView totalGroupFriendsTextView, groupNameTextView, aboutGroupTextView;
    private Button inviteFriendsButton, groupFriendsButton;
    private TextView displayMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        initializeViews();
        appToolbar.setNavigationIcon(R.drawable.ic_back_arrow_icon);
        appToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        backgroundImageView = findViewById(R.id.background_image_view);
        groupProfileImageView = findViewById(R.id.group_profile_image_view);
        totalGroupFriendsTextView = findViewById(R.id.total_group_friends_text_view);
        groupNameTextView = findViewById(R.id.group_name_text_view);
        aboutGroupTextView = findViewById(R.id.about_group_text_view);
        inviteFriendsButton = findViewById(R.id.invite_friend_button);
        groupFriendsButton = findViewById(R.id.all_group_friends_button);
        displayMessageTextView = findViewById(R.id.display_message_text_view);
    }


}