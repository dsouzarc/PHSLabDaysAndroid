package com.ryan.phslabdays;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class SendMessageReceiver extends BroadcastReceiver {
    public SendMessageReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent globalIntent) {
        final int dayOfWeek = (new GregorianCalendar().get(Calendar.DAY_OF_WEEK));
        if(dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return;
        }

        final Intent intent = new Intent(context, LoginScreen.class);
        intent.putExtra("title", "PHS Lab Days");
        intent.putExtra("text", "Time to send lab day message");

        final PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context)
                // Set Icon
                .setSmallIcon(R.drawable.login_background)
                .setTicker("Time to send lab day message")
                .setContentTitle("PHS Lab Days")
                .setContentText("Time to send lab day message")
                .addAction(R.drawable.ic_launcher, "Action Button", pIntent)
                .setContentIntent(pIntent)
                .setAutoCancel(true);
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationmanager.notify(0, builder.build());

    }
}
