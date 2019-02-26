package su.ju.arlu1695.projectgame;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static su.ju.arlu1695.projectgame.Util.getCurrentUserId;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String LOG_TAG = "MyFirebaseMessaging";
    public static final String INVITE = "Invite";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(LOG_TAG, "From: " + remoteMessage.getFrom());

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();

        String fromUserId = remoteMessage.getData().get("fromId");
        String fromName = remoteMessage.getData().get("fromName");
        String type = remoteMessage.getData().get("type");

        if (type.equals("invite") && Constants.ALLOW_INVITES) {
            handleInvite(fromUserId, fromName);
        }
        else if (type.equals("accept")) {
            /*
            startActivity(new Intent(getBaseContext(), MainActivity.class)
                .putExtra("type", "wifi")
                .putExtra("me", "x")
                .putExtra("gameId", getCurrentUserId() + "-" + fromId)
                .putExtra("withId", fromId));
                */
        }
        else if (type.equals("reject")) {
            displayNotification(getApplicationContext(),"Ouch!","Your game invite has been rejected");
        }

    }

    private void handleInvite(String fromUserId, String fromName) {
        //Reject handling
        Intent rejectIntent = new Intent(getApplicationContext(), NotificationHandler.class)
                .setAction("reject")
                .putExtra("to", fromName)
                .putExtra("withId",fromUserId);

        PendingIntent pendingRejectIntent = PendingIntent.getBroadcast(this,
                0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        //Accept handling
        Intent acceptIntent = new Intent(getApplicationContext(), NotificationHandler.class)
                .setAction("accept")
                .putExtra("to",fromName)
                .putExtra("withId",fromUserId);

        PendingIntent pendingAcceptIntent = PendingIntent.getBroadcast(this,
                2, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Show Notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, INVITE)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(String.format("%s has invite you to a duel", fromName))
                        .setContentTitle("Challenge!")
                        .addAction(R.mipmap.ic_launcher, "Accept", pendingAcceptIntent)
                        .setChannelId(INVITE)
                        .addAction(R.mipmap.ic_launcher, "Decline", pendingRejectIntent)
                        .setAutoCancel(true)
                        .setVibrate(new long[4000])
                        .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
        mNotificationMgr.notify(1,mBuilder.build());

    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(LOG_TAG,"Refreshed token: " + token);

    }

    public static void displayNotification(Context context, String title, String body) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                        .setSmallIcon(R.drawable.app_background)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(context);
        mNotificationMgr.notify(1,mBuilder.build());
    }

}
