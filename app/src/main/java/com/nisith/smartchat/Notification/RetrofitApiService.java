package com.nisith.smartchat.Notification;

import com.nisith.smartchat.Notification.Model.ServerResponse;
import com.nisith.smartchat.Notification.Model.UserNotification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitApiService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAafAF0Qk:APA91bH0XkhGWAXZCf63hQfgAAnLVtys8sKKx5Ncdb4VAOA5NYTXpAQBCCDvxaGCiZ-9YPiffoW0JTJpizJIhXo54yZjkiRWHCGlcl1RvkVf2PUIeeFetY-J4LvBrgios3jtjMEcjQnd"
    })
    @POST("fcm/send")
    Call<ServerResponse> sendNotificationToFCMServer(@Body UserNotification userNotification);
}

