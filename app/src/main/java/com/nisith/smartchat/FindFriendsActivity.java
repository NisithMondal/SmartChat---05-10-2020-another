package com.nisith.smartchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Adapters.MyFindFriendsPaginationAdapter;
import com.nisith.smartchat.Model.UserProfile;

public class FindFriendsActivity extends AppCompatActivity implements MyFindFriendsPaginationAdapter.OnCardItemClickListener {

    private Toolbar appToolbar;
    private TextView toolbarTextView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MyFindFriendsPaginationAdapter findFriendsPaginationAdapter;
    //Firebase
    private DatabaseReference rootDatabaseRef;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        initializeViews();
        setSupportActionBar(appToolbar);
        setTitle("");
        toolbarTextView.setText("Find Friends");
        appToolbar.setNavigationIcon(R.drawable.ic_back_arrow_icon);
        appToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        progressBar.setVisibility(View.GONE);
        //Firebase
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        //To get Updated data from server
        rootDatabaseRef.keepSynced(true);
        findFriendsFromDatabase();

    }


    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        toolbarTextView = findViewById(R.id.toolbar_text_view);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (findFriendsPaginationAdapter != null){
            findFriendsPaginationAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (findFriendsPaginationAdapter != null){
            findFriendsPaginationAdapter.stopListening();

        }
        if(rootDatabaseRef != null){
            rootDatabaseRef.keepSynced(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(rootDatabaseRef != null){
            //if any reason onStop() is not called
            rootDatabaseRef.keepSynced(false);
        }
    }


    private void findFriendsFromDatabase(){
        Query query = rootDatabaseRef;
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build();
        DatabasePagingOptions<UserProfile> pagingOptions = new DatabasePagingOptions.Builder<UserProfile>()
                .setQuery(query,config,UserProfile.class)
                .build();
        findFriendsPaginationAdapter = new MyFindFriendsPaginationAdapter(pagingOptions,this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(findFriendsPaginationAdapter);
    }


    @Override
    public void onCardItemClick(String userProfileId) {
        Intent intent = new Intent(this,FriendsProfileActivity.class);
        intent.putExtra(Constant.FRIEND_UID,userProfileId);
        startActivity(intent);
    }
}