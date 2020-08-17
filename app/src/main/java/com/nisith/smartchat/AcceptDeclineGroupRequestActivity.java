package com.nisith.smartchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class AcceptDeclineGroupRequestActivity extends AppCompatActivity {
    private Toolbar appToolbar;
    private ImageView groupProfileImageView;
    private CircleImageView friendProfileImageView;
    private TextView friendNameTextView, aboutGroupTextView, displayMessageTextView, groupNameTextView;
    private Button joinGroupButton, rejectGroupButton;
    private String currentUserId, friendUid, requestSenderUid, requestReceiverUid, groupKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_decline_group_request);
        initializeViews();
        setSupportActionBar(appToolbar);
        setTitle("");
        appToolbar.setNavigationIcon(R.drawable.ic_back_arrow_icon);
        appToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        friendUid = intent.getStringExtra(Constant.FRIEND_UID);
        groupKey = intent.getStringExtra(Constant.GROUP_KEY);
    }


    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        groupProfileImageView = findViewById(R.id.group_profile_image_view);
        friendProfileImageView = findViewById(R.id.friend_profile_image_view);
        groupNameTextView = findViewById(R.id.group_name_text_view);
        friendNameTextView = findViewById(R.id.friend_name_text_view);
        aboutGroupTextView = findViewById(R.id.about_group_text_view);
        joinGroupButton = findViewById(R.id.join_group_button);
        rejectGroupButton = findViewById(R.id.reject_group_button);
        displayMessageTextView = findViewById(R.id.display_message_text_view);
    }

}