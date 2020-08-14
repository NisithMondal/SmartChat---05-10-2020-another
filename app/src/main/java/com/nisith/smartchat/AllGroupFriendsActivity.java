package com.nisith.smartchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.nisith.smartchat.Adapters.MyGroupFriendsRecyclerAdapter;
import com.nisith.smartchat.Model.Friend;
import com.squareup.picasso.Picasso;

public class AllGroupFriendsActivity extends AppCompatActivity implements MyGroupFriendsRecyclerAdapter.OnGroupFriendsViewsClickListener {

    private Toolbar appToolbar;
    private ImageView backArrowImageView;
    private TextView groupNameTextView;
    private CircleImageView groupProfileImageView;
    private RecyclerView recyclerView;
    private String groupKey, groupName, groupProfileImageUrl;
    private MyGroupFriendsRecyclerAdapter adapter;
    //Firebase
    private DatabaseReference groupFriendsDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_group_friends);
        initializeViews();
        setSupportActionBar(appToolbar);
        setTitle("");
        backArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent != null){
            groupKey = intent.getStringExtra(Constant.GROUP_KEY);
            groupName = intent.getStringExtra(Constant.GROUP_NAME);
            groupProfileImageUrl = intent.getStringExtra(Constant.GROUP_PROFILE_IMAGE_URL);
            setDataOnViews();
            if (groupKey != null) {
                groupFriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("group_friends").child(groupKey);
                setUpRecyclerviewWithAdapter();
            }
        }
    }

    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        backArrowImageView = findViewById(R.id.back_arrow_image_view);
        groupNameTextView = findViewById(R.id.group_name_text_view);
        groupProfileImageView = findViewById(R.id.group_profile_image_view);
        recyclerView = findViewById(R.id.recycler_view);
    }

    private void setUpRecyclerviewWithAdapter(){
        Query query = groupFriendsDatabaseRef.orderByKey();
        FirebaseRecyclerOptions<Friend> options = new FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(query,Friend.class)
                .build();
        adapter = new MyGroupFriendsRecyclerAdapter(options, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null){
            adapter.stopListening();
        }
    }

    private void setDataOnViews(){
        if (groupName != null){
            groupNameTextView.setText(groupName);
        }
        if (groupProfileImageUrl !=null && !groupProfileImageUrl.equalsIgnoreCase("default")) {
            Picasso.get().load(groupProfileImageUrl).placeholder(R.drawable.group_icon).into(groupProfileImageView);
        } else {
            Picasso.get().load(R.drawable.group_icon).placeholder(R.drawable.group_icon).into(groupProfileImageView);
        }
    }


    @Override
    public void onViewClick(View view, String friendUid) {

    }
}