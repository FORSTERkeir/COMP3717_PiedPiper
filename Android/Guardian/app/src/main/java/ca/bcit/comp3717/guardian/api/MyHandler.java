package ca.bcit.comp3717.guardian.api;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.microsoft.windowsazure.notifications.NotificationsHandler;

import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.controller.MainActivity;
import ca.bcit.comp3717.guardian.controller.MapsActivity;

public class MyHandler extends NotificationsHandler {
    public static final String NOTIFICATION_TITLE_ALERT = "Guardian Alert Triggered";
    public static final String NOTIFICATION_TITLE_UNALERT = "Guardian Alert Dismissed";
    public static final String NOTIFICATION_CONTENT_ALERT = "Help!";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Context ctx;

    /**
     * NOTE TO FRONT END:
     * This is where you handle received message.
     */
    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        String nhMessage = bundle.getString("message");
        sendNotification(nhMessage);
        if (MainActivity.isVisible) {
            MainActivity.mainActivity.ToastNotify(nhMessage);
        }
    }

    private void sendNotification(String msg) {
        Intent intent = new Intent(ctx, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);

        String msgContent = msg.split(" ")[1];
        boolean isAlert = msgContent.equalsIgnoreCase(NOTIFICATION_CONTENT_ALERT);

        Uri soundAlarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Uri soundNotify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx);

        mBuilder.setSmallIcon(R.drawable.guardianlogo_v2);
        if (isAlert) {
            mBuilder.setContentTitle(NOTIFICATION_TITLE_ALERT);
            mBuilder.setSound(soundAlarm);
            mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        } else {
            mBuilder.setContentTitle(NOTIFICATION_TITLE_UNALERT);
            mBuilder.setSound(soundNotify);
        }
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
        mBuilder.setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}