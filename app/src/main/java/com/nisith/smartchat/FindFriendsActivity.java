package com.nisith.smartchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.Adapters.MyFindFriendsPaginationAdapter;
import com.nisith.smartchat.DialogBox.ImageClickDialog;
import com.nisith.smartchat.Model.UserProfile;

public class FindFriendsActivity extends AppCompatActivity implements MyFindFriendsPaginationAdapter.OnCardItemClickListener {

    private Toolbar appToolbar;
    private TextView toolbarTextView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MyFindFriendsPaginationAdapter findFriendsPaginationAdapter;
    private String searchFriendsType;
    //Firebase
    private DatabaseReference rootUsersDatabaseRef;
    private ValueEventListener valueEventListener;
    private String groupKey; //this is required to open FriendsProfileActivityForGroup

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
        Intent intent = getIntent();
        searchFriendsType = intent.getStringExtra(Constant.SEARCH_FRIENDS_TYPE);
        //for find friend for one to one chat groupKey will be null. But for find friend for group
        // group key will not be null.
        groupKey = intent.getStringExtra(Constant.GROUP_KEY);
        //Firebase
        rootUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        //To get Updated data from server
        rootUsersDatabaseRef.keepSynced(true);
        findFriendsFromDatabase();

    }


    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        toolbarTextView = findViewById(R.id.toolbar_text_view);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.find_friends_menu,menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_friend).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("ZXCVB",newText);
                return true;
            }
        });
        return true;
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
        if(rootUsersDatabaseRef != null){
            rootUsersDatabaseRef.keepSynced(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(rootUsersDatabaseRef != null){
            //if any reason onStop() is not called
            rootUsersDatabaseRef.keepSynced(false);
        }
    }



    private void findFriendsFromDatabase(){
        Query query = rootUsersDatabaseRef;
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
    public void onCardItemClick(View view, String userProfileId) {
        switch (view.getId()){
            case R.id.profile_image_view:
                showImageDialog(userProfileId);
                break;

            case R.id.root_view:
                if (searchFriendsType != null) {
                    if (searchFriendsType.equals(Constant.SEARCH_FRIENDS_FOR_ONE_TO_ONE_FRIENDSHIP)) {
                        //If current user finds friend for one to one chat
                        Intent intent = new Intent(this, FriendsProfileActivity.class);
                        intent.putExtra(Constant.FRIEND_UID, userProfileId);
                        startActivity(intent);
                    }else if (searchFriendsType.equals(Constant.SEARCH_FRIENDS_FOR_GROUP_FRIENDSHIP)){
                        //If current user finds friend for group chat
                        Intent intent = new Intent(this, FriendsProfileActivityForGroup.class);
                        intent.putExtra(Constant.FRIEND_UID, userProfileId);
                        intent.putExtra(Constant.GROUP_KEY, groupKey);
                        startActivity(intent);
                    }
                }

        }

    }

    private void showImageDialog(String friendUid){
        ImageClickDialog dialog = new ImageClickDialog(friendUid);
        dialog.show(getSupportFragmentManager(),"smart chat");
    }

}