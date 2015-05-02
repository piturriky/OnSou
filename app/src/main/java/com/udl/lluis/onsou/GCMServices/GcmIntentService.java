package com.udl.lluis.onsou.GCMServices;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.udl.lluis.onsou.MainActivity;
import com.udl.lluis.onsou.R;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Lluís on 21/04/2015.
 */
public class GcmIntentService extends IntentService {

    private static final int NOTIF_ALERTA_ID = 1;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            // Since we're not using two way messaging, this is all we really to check for
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Logger.getLogger("GCM_RECEIVED").log(Level.INFO, extras.toString());

                mostrarNotification(extras);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void mostrarNotification(Bundle msg)
    {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_onsou)
                        .setContentTitle(getString(R.string.app_name))
                        .setAutoCancel(true);

        Intent notIntent =  new Intent(this, MainActivity.class);

        switch(msg.getString("type")){
            case "addFriend":
                mBuilder.setContentText(String.format(getString(R.string.addFriend),msg.getString("senderNotification")));
                notIntent.putExtra("fromNotification",true);
                notIntent.putExtra("senderNotification",msg.getString("senderNotification"));
                break;
            case "simpleMessage":
                mBuilder.setContentText(msg.getString("message"));
                break;
        }

        PendingIntent contIntent = PendingIntent.getActivity(
                this, 0, notIntent, 0);

        mBuilder.setContentIntent(contIntent);

        mNotificationManager.notify(NOTIF_ALERTA_ID, mBuilder.build());
    }

    protected void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
