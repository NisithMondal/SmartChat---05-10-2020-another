package com.nisith.smartchat.Notification;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.HomeActivity;
import com.nisith.smartchat.Notification.Model.Notification;
import com.nisith.smartchat.Notification.Model.NotificationData;

import androidx.annotation.NonNull;

public class MyNotification {
    private Context context;
    public MyNotification(Context context){
        this.context = context;
    }
    public void send(final String title, final String body, String receiverUid, final String clickAction, final String senderImageUrl) {
        DatabaseReference notificationRootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("notification");
        final String  currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notificationRootDatabaseRef.child(receiverUid).child("device_token").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String receiverDeviceToken = snapshot.getValue().toString();
                    Notification notification = new Notification(title, body, "com.nisith.smartchat.abcde", senderImageUrl);
                    NotificationData data = new NotificationData(currentUserId);
                    RetrofitServerRequest serverRequest = new RetrofitServerRequest(context);
                    serverRequest.sendNotification(receiverDeviceToken, notification, data);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
