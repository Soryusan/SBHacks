package com.cisco.cmx.app.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cisco.cmx.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class CMXGcmIntentService extends IntentService {

	public static final int NOTIFICATION_ID = 1000;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
	
	public CMXGcmIntentService() {
		super("CMX Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        SharedPreferences settings = getSharedPreferences("settings", 0);
        if(settings.getBoolean("pushNotif", true)){
	        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
	            // Filter messages based on message type.
	            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
	                Log.d("GMC", "Send error: " + extras.toString());
	            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
	            	Log.d("GMC", "Deleted messages on server: " + extras.toString());
	            // If it's a regular GCM message.
	            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
	                // Post notification of received message.
	            	Log.d("GMC", "Received: " + extras.toString());
	            	sendNotification(extras);
	            }
	        }
        }
        
        CMXGcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	
	private void sendNotification(Bundle extras) {
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, CMXGcmActivity.class);
		if (extras.containsKey("preferredNetwork")) {
			intent.putExtra("preferredNetwork", extras.getCharSequence("preferredNetwork"));
		}
		
		if (extras.containsKey("networkIp")) {
			intent.putExtra("networkIp", extras.getCharSequence("networkIp"));
		}
		
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder mBuilder =
	                new NotificationCompat.Builder(this)
	        .setContentTitle(extras.getString("message"))
	        .setSmallIcon(R.drawable.ic_launcher);
        

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
