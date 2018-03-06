package com.example.user.mychat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by USER on 20-02-2018.
 */

public class MyMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title=remoteMessage.getNotification().getTitle();
        String body=remoteMessage.getNotification().getBody();
        String click_action=remoteMessage.getNotification().getClickAction();
        String from_user_id=remoteMessage.getData().get("from_user_id");

        NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title).setContentText(body);

        Intent resultIntent=new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);
        PendingIntent resultPendingIntent=PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        int notification_id=(int)System.currentTimeMillis();
        NotificationManager mNotify=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotify.notify(notification_id,mBuilder.build());


    }
}
