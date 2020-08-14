package com.nisith.smartchat.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nisith.smartchat.Adapters.MyFriendRequestFragmentRecyclerAdapter;
import com.nisith.smartchat.Constant;
import com.nisith.smartchat.DialogBox.ImageClickDialog;
import com.nisith.smartchat.FriendsProfileActivity;
import com.nisith.smartchat.Model.Friend;
import com.nisith.smartchat.Model.FriendRequest;
import com.nisith.smartchat.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendRequestFragment extends Fragment implements MyFriendRequestFragmentRecyclerAdapter.OnRequestButtonClickListener {

    private RecyclerView recyclerView;
    private MyFriendRequestFragmentRecyclerAdapter adapter;
    private List<FriendRequest> friendRequestList;
    //Firebase
    private DatabaseReference friendsDatabaseRef;
    private DatabaseReference friendRequestDatabaseRef;
    private DatabaseReference friendRequestDatabaseRootRef;
    private ChildEventListener childEventListener;
    private String currentUserId;
    private String requestSenderUid, requestReceiverUid;
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
        friendRequestDatabaseRootRef = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        friendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("friends");
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
    public void onRequestButtonClick(View view, int position) {
        String friendUid = friendRequestList.get(position).getFriendKey();
        switch (view.getId()){
            case R.id.root_view:
                //Card view is Clicked
                Intent intent = new Intent(getContext(), FriendsProfileActivity.class);
                intent.putExtra(Constant.FRIEND_UID, friendUid);
                startActivity(intent);
                break;

            case R.id.profile_image_view:
                //Circle Image view is Clicked
                showImageDialog(friendUid);
                break;

            case R.id.accept_request_button:
                //Accept Button is Clicked
                acceptFriendRequest(friendUid);
                break;

            case R.id.decline_request_button:
                //Decline Button is Clicked
                Button button = (Button) view;
                String buttonCaption = button.getText().toString();
                //cancelFriendRequest() is called in both if and else condition. This is redendant. But I write this two times because for easy understanding.
                //But this is not necessary.
                if (buttonCaption.equalsIgnoreCase("Decline")){
                    cancelFriendRequest(friendUid);
                }else if (buttonCaption.equalsIgnoreCase("Cancel Request")){
                    cancelFriendRequest(friendUid);
                }
        }
    }

    private void showImageDialog(String friendUid){
        ImageClickDialog dialog = new ImageClickDialog(friendUid);
        dialog.show(getActivity().getSupportFragmentManager(),"smart chat");
    }

    private void acceptFriendRequest(String friendUid){
        requestSenderUid = currentUserId; //means Current User Id
        requestReceiverUid = friendUid; //means Friend User Id
        Map<String, Object> map = new HashMap<>();
        map.put(requestSenderUid+"/"+requestReceiverUid,new FriendRequest(Constant.FRIEND));
        map.put(requestReceiverUid+"/"+requestSenderUid,new FriendRequest(Constant.FRIEND));
        friendRequestDatabaseRootRef.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null){
                    //all ok
//                    Map<String, Object> map1 = new HashMap<>();
                    Friend friend = new Friend("now", Constant.SINGLE_FRIEND);
                    Map<String, Object> friendsMap = new HashMap<>();
                    friendsMap.put(requestSenderUid+"/"+requestReceiverUid,friend);
                    friendsMap.put(requestReceiverUid+"/"+requestSenderUid,friend);
                    friendsDatabaseRef.updateChildren(friendsMap);
                }
            }
        });
    }



    private void cancelFriendRequest(String friendUid){
        requestSenderUid = currentUserId; //means Current User Id
        requestReceiverUid = friendUid; //means Friend User Id
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(requestSenderUid + "/" + requestReceiverUid, null);
        dataMap.put(requestReceiverUid + "/" + requestSenderUid, null);;
        friendRequestDatabaseRootRef.updateChildren(dataMap);
    }


}