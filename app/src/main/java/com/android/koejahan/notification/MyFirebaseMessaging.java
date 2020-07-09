package com.android.koejahan.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.koejahan.data.FriendDB;
import com.android.koejahan.data.SharedPreferenceHelper;
import com.android.koejahan.data.StaticConfig;
import com.android.koejahan.model.ListFriend;
import com.android.koejahan.ui.ChatActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    Context context;
    ListFriend listFriend;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);

        String sented = remoteMessage.getData().get("sented");

        String id = SharedPreferenceHelper.getInstance(context).getUID();

        Log.d("FIREBASE MESSAGING", "From: " + remoteMessage.getFrom());


        if (id != null && sented.equals(id)){
            sendNotification(remoteMessage);
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        //get data dari database;
        listFriend = FriendDB.getInstance(context).getListSpesifik(user);
        final String name = listFriend.getListFriend().get(0).name;
        final String id = user;
        final String idRoom = listFriend.getListFriend().get(0).idRoom;
        final String avata = listFriend.getListFriend().get(0).avata;

        Log.d("Messaging","NILAI = "+ name + id + idRoom);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, name);
        ArrayList<CharSequence> idFriend = new ArrayList<CharSequence>();
        idFriend.add(id);
        intent.putExtra(StaticConfig.INTENT_ID_FRIEND, id);
        intent.putCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID, idFriend);
        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, idRoom);
        intent.putExtra(StaticConfig.INTENT_KEY_AVATA_FRIEND, avata);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setSound(defaultsound)
                .setContentIntent(pendingIntent);
        NotificationManager not = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0){
            i = j;
        }

        not.notify(i, builder.build());
    }
}
