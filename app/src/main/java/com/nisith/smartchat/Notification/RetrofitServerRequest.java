package com.nisith.smartchat.Notification;

import android.content.Context;
import android.widget.Toast;
import com.nisith.smartchat.Notification.Model.Notification;
import com.nisith.smartchat.Notification.Model.NotificationData;
import com.nisith.smartchat.Notification.Model.ServerResponse;
import com.nisith.smartchat.Notification.Model.UserNotification;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitServerRequest {
    private Retrofit retrofit;
    private RetrofitApiService retrofitApiService;
    private Context context;
    public RetrofitServerRequest(Context context){
       createRetrofitObject();
       this.context = context;
    }

    public void sendNotification(String deviceToken, Notification notification, NotificationData data){
        if (retrofit == null && retrofitApiService == null){
            createRetrofitObject();
        }else {
            UserNotification userNotification = new UserNotification(deviceToken, notification, data);
            Call<ServerResponse> call = retrofitApiService.sendNotificationToFCMServer(userNotification);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    assert response.body() != null;
                    if (response.body().success == 1){
                        Toast.makeText(context, "Notification Send Successfully", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "Notification Not Send ", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void createRetrofitObject(){
        retrofit = new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitApiService = retrofit.create(RetrofitApiService.class);
    }
}
