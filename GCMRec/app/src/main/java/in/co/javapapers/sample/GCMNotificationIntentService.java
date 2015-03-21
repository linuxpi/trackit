//package in.co.varunbansal.linuxpi.gcmrec;
//
//import android.app.IntentService;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.SystemClock;
//import android.support.v4.app.NotificationCompat;
//import android.util.Log;
//
//import com.google.android.gms.gcm.GoogleCloudMessaging;
//
//import java.util.ArrayList;
//
//public class GCMNotificationIntentService extends IntentService {
//
//    public static final String LOG_TAG="TrackMe Log Entry";
//    public static final int NOTIFICATION_ID = 1;
//    private NotificationManager mNotificationManager;
//    NotificationCompat.Builder builder;
//
//    public GCMNotificationIntentService() {
//        super("GcmIntentService");
//    }
//
//    public static final String TAG = "GCMNotificationIntentService";
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        Bundle extras = intent.getExtras();
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
//
//        String messageType = gcm.getMessageType(intent);
//
//        if (!extras.isEmpty()) {
//            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
//                    .equals(messageType)) {
//                sendNotification("Send error: " + extras.toString());
//            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
//                    .equals(messageType)) {
//                sendNotification("Deleted messages on server: "
//                        + extras.toString());
//            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
//                    .equals(messageType)) {
//
//                for (int i = 0; i < 3; i++) {
//                    Log.i(TAG,
//                            "Working... " + (i + 1) + "/5 @ "
//                                    + SystemClock.elapsedRealtime());
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                    }
//
//                }
//                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
//
//                String data = extras.get(Config.MESSAGE_KEY).toString();
//
//                sendNotification("Message Received from Google GCM Server: "
//                        + data);
//
//                Log.i(TAG, "Received: " + extras.toString());
//
//                //notify the list
//                    //parse the data first
//                ArrayList<Integer> tempData = new ArrayList<>();
//                int first = data.indexOf('[');
//                int last  = data.indexOf(',');
//                    while(last<data.length()){
//                        String tempString = data.substring(first+1,last);
//                        Log.i(LOG_TAG,"data : "+tempString);
//                        tempData.add(Integer.parseInt(tempString));
//                        first=last+1;
//                        last=data.indexOf(',',first);
//                    }
//
//            }
//        }
//        GcmBroadcastReceiver.completeWakefulIntent(intent);
//    }
//
//    private void sendNotification(String msg) {
//        Log.d(TAG, "Preparing to send notification...: " + msg);
//        mNotificationManager = (NotificationManager) this
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, MainActivity.class), 0);
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                this).setSmallIcon(R.drawable.ic_launcher)
//                .setContentTitle("GCM Notification")
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
//                .setContentText(msg);
//
//        mBuilder.setContentIntent(contentIntent);
//        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
//        Log.d(TAG, "Notification sent successfully.");
//    }
//}
