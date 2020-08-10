package com.nisith.smartchat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nisith.smartchat.Adapters.MyFriendRequestFragmentRecyclerAdapter;
import com.nisith.smartchat.Constant;
import com.nisith.smartchat.Model.FriendRequest;
import com.nisith.smartchat.R;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestFragment extends Fragment implements MyFriendRequestFragmentRecyclerAdapter.OnRequestButtonClickListener {

    private RecyclerView recyclerView;
    private MyFriendRequestFragmentRecyclerAdapter adapter;
    private List<FriendRequest> friendRequestList;
    //Firebase
    private DatabaseReference friendRequestDatabaseRef;
    private ChildEventListener childEventListener;
    private String currentUserId;

    public FriendRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_request, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //This method is called after the onCreate() method is executed of activity i.e. in this case HomeActivity
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendRequestDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friend_requests").child(currentUserId);
        friendRequestList = new ArrayList<>();
        setUpRecyclerViewWithAdapter();
    }


    private void setUpRecyclerViewWithAdapter(){
        adapter = new MyFriendRequestFragmentRecyclerAdapter(this, friendRequestList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (friendRequestDatabaseRef != null){
            getCurrentUsersFriendRequestsData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (friendRequestDatabaseRef != null){
            friendRequestDatabaseRef.removeEventListener(childEventListener);
        }
    }

    private void getCurrentUsersFriendRequestsData(){
        friendRequestList.clear();
        if (adapter != null){
            //this is solved the problem of last item remove from list
            adapter.notifyDataSetChanged();
        }
        childEventListener = friendRequestDatabaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                FriendRequest friendRequest = snapshot.getValue(FriendRequest.class);
                if (friendRequest != null){
                    String requestType = friendRequest.getRequestType();
                    if (! requestType.equals(Constant.FRIEND)){
                        friendRequest.setFriendKey(snapshot.getKey());
                        friendRequestList.add(friendRequest);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){
                    FriendRequest friendRequest = snapshot.getValue(FriendRequest.class);
                    if (friendRequest != null && friendRequest.getRequestType().equals(Constant.FRIEND)){
                        //Means both of them are friends now. So remove this friendRequest object from the friendRequestList.
                        friendRequest.setFriendKey(snapshot.getKey());
                        friendRequestList.remove(friendRequest);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    FriendRequest friendRequest = snapshot.getValue(FriendRequest.class);
                    if (friendRequest != null){
                        friendRequest.setFriendKey(snapshot.getKey());
                            friendRequestList.remove(friendRequest);
                            adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestButtonClick(View view) {
        switch (view.getId()){
            case R.id.root_view:
                //Card view is Clicked
                Toast.makeText(getContext(), "Parent View", Toast.LENGTH_SHORT).show();
                break;

            case R.id.profile_image_view:
                //Circle Image view is Clicked
                Toast.makeText(getContext(), "Image View", Toast.LENGTH_SHORT).show();
                break;

            case R.id.accept_request_button:
                //Accept Button is Clicked
                Toast.makeText(getContext(), "Accept", Toast.LENGTH_SHORT).show();
                break;

            case R.id.decline_request_button:
                //Decline Button is Clicked
                Button button = (Button) view;
                String buttonCaption = button.getText().toString();
                if (buttonCaption.equalsIgnoreCase("Decline")){
                    Toast.makeText(getContext(), "Decline", Toast.LENGTH_SHORT).show();
                }else if (buttonCaption.equalsIgnoreCase("Cancel Request")){
                    Toast.makeText(getContext(), "Cancel Request", Toast.LENGTH_SHORT).show();
                }
        }
    }
}