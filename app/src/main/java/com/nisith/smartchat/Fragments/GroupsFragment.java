package com.nisith.smartchat.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.nisith.smartchat.Adapters.MyGroupFragmentRecyclerAdapter;
import com.nisith.smartchat.Constant;
import com.nisith.smartchat.CreateGroupActivity;
import com.nisith.smartchat.DialogBox.FriendImageClickDialog;
import com.nisith.smartchat.FindFriendsActivity;
import com.nisith.smartchat.GroupProfileActivity;
import com.nisith.smartchat.HomeActivity;
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.R;

public class GroupsFragment extends Fragment implements MyGroupFragmentRecyclerAdapter.OnGroupFragmentViewsClickListener{

    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private MyGroupFragmentRecyclerAdapter adapter;
    //Firebase
    private DatabaseReference currentUserFriendsDatabaseRef;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        floatingActionButton = view.findViewById(R.id.floating_action_button);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //This method is called after the onCreate() method is executed of activity i.e. in this case HomeActivity
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserFriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserId);
        setUpRecyclerViewWithAdapter();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                startActivity(intent);
            }
        });

    }

    private void setUpRecyclerViewWithAdapter(){
        Query query = currentUserFriendsDatabaseRef.orderByChild("friendsType").equalTo(Constant.GROUP_FRIEND);
        FirebaseRecyclerOptions<Friend> recyclerOptions = new FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(query, Friend.class)
                .build();
        adapter = new MyGroupFragmentRecyclerAdapter(recyclerOptions, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null){
            adapter.removeValueEventListener(); //To remove all value event listener
            adapter.stopListening();
        }
    }

    @Override
    public void onFriendViewsClick(View view, String groupKey) {
        //called when each row of Friend Fragment is clicked
        switch (view.getId()){
            case R.id.group_profile_image_view:
                showImageDialog(groupKey);
                Log.d("MNBV", "profile_image_view is called");
                break;

            case R.id.root_view:
                Intent intent = new Intent(getContext(), GroupProfileActivity.class);
                intent.putExtra(Constant.GROUP_KEY, groupKey);
                startActivity(intent);
        }
    }

    private void showImageDialog(String key){
        FriendImageClickDialog dialog = new FriendImageClickDialog(key);
        dialog.show(getActivity().getSupportFragmentManager(),"smart chat");
    }


}