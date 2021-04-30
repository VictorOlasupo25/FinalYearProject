package com.example.streamer.Utility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.streamer.LoginActivity;
import com.example.streamer.MessagesActivity;
import com.example.streamer.Models.User;
import com.example.streamer.R;
import com.example.streamer.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    Intent intent;
    String title,body,action,media,content;

    @Override
    public void onNewToken(String s) {
        //Log.v("NEW_TOKEN", s);
        newToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> params = remoteMessage.getData();
        JSONObject object = new JSONObject(params);

        try {

           title=object.getString("title");
           body=object.getString("body");
            media=object.getString("media");
           action=object.getString("click_action");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if(!body.isEmpty()&&!media.isEmpty()){
            content=media+": "+body;
        }
        else if(!body.isEmpty()){
            content=body;
        }
        else if(!media.isEmpty()){
            content=media;
        }


        String NOTIFICATION_CHANNEL_ID = getResources().getString(R.string.app_name)+" MESSAGING_CHANNEL";

        long pattern[] = {0, 1000, 500, 1000};


        Intent intent;

        intent = new Intent(action);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Your Notifications",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription("");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(pattern);
            notificationChannel.enableVibration(true);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.purple_500))
                .setContentTitle(title)
                .setContentText(content)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);



        //notificationBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1000, notificationBuilder.build());
    }

    public static void newToken(String token){

        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null) {

            String userId = user.getUid();

            Map<String, Object> childUpdates;

            childUpdates = new HashMap<>();
            childUpdates.put("tokenId", token);

            FirebaseDatabase.getInstance().getReference("Users").child(userId).
                    updateChildren(childUpdates);

        }
    }
}