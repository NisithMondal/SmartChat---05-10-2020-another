package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Model.UserProfile;
import com.squareup.picasso.Picasso;

public class ChatActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    private ImageView backArrowImageView;
    private RecyclerView recyclerView;
    private CircleImageView profileImageView;
    private TextView profileNameTextView, onlineStatusTextView;
    private String userUid;
    //Firebase
    private DatabaseReference userDatabaseRef;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initializeViews();
        setSupportActionBar(appToolbar);
        backArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitle("");
        //Firebase
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        fetchDataFromIntent();
    }

    private void initializeViews(){
      appToolbar = findViewById(R.id.app_toolbar);
      backArrowImageView = findViewById(R.id.back_arrow_image_view);
      profileNameTextView = findViewById(R.id.profile_name_text_view);
      onlineStatusTextView = findViewById(R.id.online_state_image_view);
      profileImageView = findViewById(R.id.profile_image_view);
      recyclerView = findViewById(R.id.recycler_view);
    }

    private void fetchDataFromIntent(){
        Intent intent = getIntent();
        if (intent != null){
            userUid = intent.getStringExtra(Constant.FRIEND_UID);
//            userName = intent.getStringExtra(Constant.USER_NAME);
//            profileImageUrl = intent.getStringExtra(Constant.PROFILE_IMAGE_URL);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setDataOnViews();
    }

    private void setDataOnViews(){
        if (userUid == null){
            return;
        }

       valueEventListener = userDatabaseRef.child(userUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                        UserProfile userProfile =snapshot.getValue(UserProfile.class);
                        if (userProfile != null) {
                            String userName = userProfile.getUserName();
                            profileNameTextView.setText(userName);
                            onlineStatusTextView.setText("online");
                            String profileImageUrl = userProfile.getProfileImage();
                            if (!profileImageUrl.equalsIgnoreCase("default")) {
                                Picasso.get().load(profileImageUrl).placeholder(R.drawable.user_icon).into(profileImageView);
                            } else {
                                Picasso.get().load(R.drawable.user_icon).placeholder(R.drawable.user_icon).into(profileImageView);
                            }
                        }
                    }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userDatabaseRef != null){
            userDatabaseRef.child(userUid).removeEventListener(valueEventListener);
        }
    }
}