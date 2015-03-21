package in.co.varunbansal.linuxpi.trackit.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import in.co.varunbansal.linuxpi.trackit.R;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class PassiveFragment extends Fragment {

    public static String FRAGMENT_TAG;
    private static ArrayAdapter<Integer> adapter;
    private static ArrayList<Integer> activeUsers;
    private ListView activeUsersList;

    public PassiveFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.passive_layout, container, false);

        activeUsers = new ArrayList<>();
//        activeUsers.add(0);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, activeUsers);

        activeUsersList = (ListView) myView.findViewById(R.id.active_user_list);
        activeUsersList.setAdapter(adapter);

        FRAGMENT_TAG = getTag();

//        Bundle args = getArguments();

//        ((TextView) myView.findViewById(R.id.text)).setText("Page " + args.getInt("page_position"));

        return myView;

    }

    public void updateActiveUsersList(ArrayList<Integer> list) {
//        activeUsers.remove(0);
        emptyList();
        activeUsers.addAll(list);
//        adapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_list_item_1, list);
//        activeUsersList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
//        activeUsersList.deferNotifyDataSetChanged();

        Log.i(LOG_TAG, "Task Complete");
    }

    public void emptyList(){
        activeUsers.clear();
        adapter.notifyDataSetChanged();
    }

    public void addNewActiveUser(String unKey) {
        activeUsers.add(Integer.parseInt(unKey));
        adapter.notifyDataSetChanged();
    }

    public void removeActiveUser(String unKey) {
        Integer u=Integer.parseInt(unKey.substring(1));
        if(activeUsers.contains(u))
            activeUsers.remove(activeUsers.indexOf(u));
        adapter.notifyDataSetChanged();
    }


//    public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
//
//
//        public static final String LOG_TAG = "TrackMe Log Entry";
//
//        public GcmBroadcastReceiver(){
//            super();
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //if the device is in passive mode then parse the data from server
//            Log.i(LOG_TAG, "Received Notification from server");
//            if (!StartupScreen.MODE) {
////                ComponentName comp = new ComponentName(context.getPackageName(),
////                        GCMNotificationIntentService.class.getName());
////                startWakefulService(context, (intent.setComponent(comp)));
//
//                Bundle extras = intent.getExtras();
//
//                Log.i(LOG_TAG,"data recieved : " + extras.get(Config.MESSAGE_KEY).toString());
//
//                setResultCode(Activity.RESULT_OK);
//            }
//        }
//    }

//    class GCMNotificationIntentService extends IntentService {
//
//        public static final String LOG_TAG="TrackMe Log Entry";
//        public static final int NOTIFICATION_ID = 1;
//        private NotificationManager mNotificationManager;
//        NotificationCompat.Builder builder;
//
//        public GCMNotificationIntentService() {
//            super("GcmIntentService");
//        }
//
//        public static final String TAG = "GCMNotificationIntentService";
//
//        @Override
//        protected void onHandleIntent(Intent intent) {
//            Bundle extras = intent.getExtras();
//            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
//
//            String messageType = gcm.getMessageType(intent);
//
//            if (!extras.isEmpty()) {
//                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
//                        .equals(messageType)) {
//                    sendNotification("Send error: " + extras.toString());
//                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
//                        .equals(messageType)) {
//                    sendNotification("Deleted messages on server: "
//                            + extras.toString());
//                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
//                        .equals(messageType)) {
//
//                    for (int i = 0; i < 3; i++) {
//                        Log.i(TAG,
//                                "Working... " + (i + 1) + "/5 @ "
//                                        + SystemClock.elapsedRealtime());
//                        try {
//                            Thread.sleep(5000);
//                        } catch (InterruptedException e) {
//                        }
//
//                    }
//                    Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
//
//                    String data = extras.get(Config.MESSAGE_KEY).toString();
//
//                    sendNotification("Message Received from Google GCM Server: "
//                            + data);
//
//                    Log.i(TAG, "Received: " + extras.toString());
//
//                    //notify the list
//                    //parse the data first
//                    ArrayList<Integer> tempData = new ArrayList<>();
//                    int first = data.indexOf('[');
//                    int last  = data.indexOf(',');
//                    while(last<data.length()){
//                        String tempString = data.substring(first+1,last);
//                        Log.i(LOG_TAG,"data : "+tempString);
//                        tempData.add(Integer.parseInt(tempString));
//                        first=last+1;
//                        last=data.indexOf(',',first);
//                    }
//
//                }
//            }
//            in.co.varunbansal.linuxpi.gcmrec.GcmBroadcastReceiver.completeWakefulIntent(intent);
//        }
//
//        private void sendNotification(String msg) {
//            Log.d(TAG, "Preparing to send notification...: " + msg);
//            mNotificationManager = (NotificationManager) this
//                    .getSystemService(Context.NOTIFICATION_SERVICE);
//
//            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                    new Intent(this, MainActivity.class), 0);
//
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
//                    this).setSmallIcon(R.drawable.ic_launcher)
//                    .setContentTitle("GCM Notification")
//                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
//                    .setContentText(msg);
//
//            mBuilder.setContentIntent(contentIntent);
//            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
//            Log.d(TAG, "Notification sent successfully.");
//        }
//    }


}

