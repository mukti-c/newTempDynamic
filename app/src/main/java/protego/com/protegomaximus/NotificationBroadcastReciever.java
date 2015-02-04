package protego.com.protegomaximus;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;


/**
 * Created by chanijindal on 04/02/15.
 */
public class NotificationBroadcastReciever extends BroadcastReceiver {

   static int notification_number;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override


    public void onReceive(Context context, Intent intent) {


        notification_number++;
        NotificationCompat.Builder  mBuilder =
                new NotificationCompat.Builder(context);
        mBuilder.setAutoCancel(true);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle("New Message");
        mBuilder.setContentText("Anomaly detected");
        mBuilder.setNumber(notification_number);
        Intent resultIntent = new Intent(context,MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        mNotificationManager.notify(0 ,mBuilder.build());

        // Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();

    }



}
