package com.nisith.smartchat.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nisith.smartchat.Constant;
import com.nisith.smartchat.HomeActivity;
import com.nisith.smartchat.R;
import com.nisith.smartchat.TestActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        updateFCMToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getNotification() != null) {
            prepareNotification(remoteMessage);
        }

    }

    private void prepareNotification(RemoteMessage remoteMessage){
       final String title = remoteMessage.getNotification().getTitle();
       final String body = remoteMessage.getNotification().getBody();
       final Uri imageUrl = remoteMessage.getNotification().getImageUrl();
       final String senderUid = remoteMessage.getData().get("senderUid");
       if (imageUrl != null && !TextUtils.isEmpty(imageUrl.toString())){
           //Load image from that url in background
           new Handler(Looper.getMainLooper())
                   .post(new Runnable() {
                       @Override
                       public void run() {
                           Picasso.get().load(imageUrl)
                                   .into(new Target() {
                                       @Override
                                       public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                           //Image is available in bitmap format
                                           showNotification(senderUid, title, body, bitmap);
                                       }

                                       @Override
                                       public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                       }

                                       @Override
                                       public void onPrepareLoad(Drawable placeHolderDrawable) {

                                       }
                                   });
                       }
                   });
       }else {
           //imageUrl is null
           showNotification(senderUid, title, body, null);
       }

    }

    private void showNotification(String senderUid, String title, String body, Bitmap largeIcon){
        String channelId = "MyNotificationChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(channelId,"MyNotify", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constant.NOTIFICATION_CLICK_ACTION, Constant.ACTION_REQUEST);
        intent.putExtra("senderUid", senderUid);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),100,intent,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),channelId)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(body)
                .setDefaults(Notification.DEFAULT_ALL)
                .setColor(Color.BLACK)
                .setSmallIcon(R.drawable.ic_person)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify((int) SystemClock.currentThreadTimeMillis(),builder.build());
    }



    private void updateFCMToken(String token){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference notificationRootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("notification");
            String currentUserId = firebaseUser.getUid();
            Map<String, Object> map = new HashMap<>();
            map.put(currentUserId+"/device_token", token);
            notificationRootDatabaseRef.updateChildren(map);
        }
    }


}
