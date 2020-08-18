package com.nisith.smartchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ImageDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);
        Toolbar appToolbar = findViewById(R.id.app_toolbar);
        TextView toolbarTextView = findViewById(R.id.toolbar_text_view);
        ImageView imageView = findViewById(R.id.image_view);
        appToolbar.setBackgroundColor(Color.BLACK);
        setTitle("");
        appToolbar.setNavigationIcon(R.drawable.ic_back_arrow_icon);
        appToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        String userName = intent.getStringExtra(Constant.USER_NAME);
        String imageUrl = intent.getStringExtra(Constant.PROFILE_IMAGE_URL);
        String friendType = intent.getStringExtra(Constant.FRIENDS_TYPE);
        if (userName != null){
            toolbarTextView.setText(userName);
        }
        if (imageUrl != null){
            if (friendType != null) {
                if (friendType.equals(Constant.SINGLE_FRIEND)) {
                    Picasso.get().load(imageUrl).placeholder(R.drawable.default_user_icon).into(imageView);
                } else if (friendType.equals(Constant.GROUP_FRIEND)) {
                    Picasso.get().load(imageUrl).placeholder(R.drawable.ic_group_icon1).into(imageView);
                }
            }else {
                //if any activity not send friendType, then do this
                Picasso.get().load(imageUrl).placeholder(R.drawable.default_user_icon).into(imageView);
            }
        }
    }
}