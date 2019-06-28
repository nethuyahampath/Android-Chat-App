package com.example.ganesh.appchat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Ganesh on 9/23/2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationTitle=remoteMessage.getNotification().getTitle();
        String notificationMessage=remoteMessage.getNotification().getBody();
        String click_action=remoteMessage.getNotification().getClickAction();
        String from_user_id=remoteMessage.getData().get("from_user_id");

        NotificationCompat.Builder mBuilder= new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.launcher_icon2)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage);

        Intent resultIntent=new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);

        PendingIntent resultPendingIntent=PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId=(int)System.currentTimeMillis();

                NotificationManager mNotificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                mNotificationManager.notify(mNotificationId,mBuilder.build());
    }
}
