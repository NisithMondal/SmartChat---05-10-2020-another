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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.nisith.smartchat.Adapters.MyNotificationFragmentRecyclerAdapter;
import com.nisith.smartchat.Constant;
import com.nisith.smartchat.DialogBox.FriendImageClickDialog;
import com.nisith.smartchat.DialogBox.ImageClickDialog;
import com.nisith.smartchat.FriendsProfileActivity;
import com.nisith.smartchat.FriendsProfileActivityForGroup;
import com.nisith.smartchat.Model.UserNotification;
import com.nisith.smartchat.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NotificationFragment extends Fragment implements MyNotificationFragmentRecyclerAdapter.NotificationItemClickListener {

    private RecyclerView recyclerView;
    private MyNotificationFragmentRecyclerAdapter adapter;
    //Firebase
    private DatabaseReference notificationDatabaseRef;
    private String currentUserId;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView = view.findViewById(R.id.notification_recycler_view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        notificationDatabaseRef = FirebaseDatabase.getInstance().getReference().child("notification");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setupRecyclerViewWithAdapter();
    }

    private void setupRecyclerViewWithAdapter() {
        Query query = notificationDatabaseRef.child(currentUserId).child("all_notification")
                .orderByChild("timeStamp");//Order by ascending order
        FirebaseRecyclerOptions<UserNotification> recyclerOptions = new FirebaseRecyclerOptions.Builder<UserNotification>()
                .setQuery(query, UserNotification.class)
                .build();
        adapter = new MyNotificationFragmentRecyclerAdapter(recyclerOptions, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
            adapter.stopListening();
        }
    }

    @Override
    public void onNotificationItemClick(View view, String senderUid, String groupId, String notificationKey) {
        switch (view.getId()){
            case R.id.root_view:
                openProfileActivity(senderUid, groupId);
                break;
            case R.id.profile_image_view:
                showImageDialog(senderUid);
                break;
            case R.id.delete_image_view:
                //Delete Notification when notification delete icon is clicked
                deleteNotification(notificationKey);

        }
    }


    private void openProfileActivity(String senderUid, String groupId){
        if (groupId.equals("blank")){
            //Means the notification is not a group related notification. It may friend request
            Intent intent = new Intent(getContext(), FriendsProfileActivity.class);
            intent.putExtra(Constant.FRIEND_UID, senderUid);
            startActivity(intent);
        }else {
            //Means the notification is a group related notification. It may be group request otr may accept group request notification
            Intent intent = new Intent(getContext(), FriendsProfileActivityForGroup.class);
            intent.putExtra(Constant.FRIEND_UID, senderUid);
            intent.putExtra(Constant.GROUP_KEY, groupId);
            startActivity(intent);
        }
    }

    private void showImageDialog(String friendUid){
        ImageClickDialog dialog = new ImageClickDialog(friendUid);
        dialog.show(getActivity().getSupportFragmentManager(),"smart chat");
    }

    private void deleteNotification(String notificationKey){
        Map<String, Object> map = new HashMap<>();
        map.put(currentUserId + "/all_notification/" + notificationKey, null);
        notificationDatabaseRef.updateChildren(map).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



}