package in.co.varunbansal.linuxpi.trackit.fragments;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import in.co.varunbansal.linuxpi.trackit.R;
import in.co.varunbansal.linuxpi.trackit.connection.handler.ShareExternalServer;
import in.co.varunbansal.linuxpi.trackit.main.FirstLaunch;
import in.co.varunbansal.linuxpi.trackit.main.StartupScreen;
import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class ActiveFragment extends Fragment {

    public static String FRAGMENT_TAG;
    private String unKey=null;
    NumberPicker[] key;
    Button bc;
    ShareExternalServer shareTask;
    public final int NOTIFICATION_ID = 1;
    private static String UNKEY="00000";
    private TextView text;

    public ActiveFragment() {

    }

    public ActiveFragment(String unKey) {
        this.unKey = unKey;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.active_layout, container, false);

        text = (TextView) myView.findViewById(R.id.confirmMessage);

        key = new NumberPicker[5];
        key[0] = (NumberPicker) myView.findViewById(R.id.un1);
        key[1] = (NumberPicker) myView.findViewById(R.id.un2);
        key[2] = (NumberPicker) myView.findViewById(R.id.un3);
        key[3] = (NumberPicker) myView.findViewById(R.id.un4);
        key[4] = (NumberPicker) myView.findViewById(R.id.un5);

        for (NumberPicker s : key) {
            s.setMaxValue(9);
            s.setMinValue(0);
        }

        text.setVisibility(View.INVISIBLE);


        FRAGMENT_TAG = getTag();

        bc = (Button) myView.findViewById(R.id.active_broadcast);

        if(unKey!=null){
            for(int i=0;i<5;i++){
                key[i].setValue(Integer.parseInt(unKey.substring(i,i+1)));
            }

            disableNumbericKey();
            text.setVisibility(View.VISIBLE);
            bc.setText(getActivity().getResources().getString(R.string.unbroadcast_button_text));
        }


        bc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                performServerSync(bc.getText().toString());

            }
        });

        return myView;
    }

    private void performServerSync(String condition) {
        StringBuilder temp = new StringBuilder( );

        if (condition.equals(getResources().getString(R.string.broadcast_button_text))) {

            final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            Log.i(LOG_TAG, "gps is "+ statusOfGPS);
            if ( !statusOfGPS) {
                    buildAlertMessageNoGps();
            }  else {
                for (NumberPicker s : key) {
                    temp.append(Integer.toString(s.getValue()));
                }

                if (temp.toString().equals("00000")) {
                    Toast.makeText(getActivity(), "Unique key cannot be 00000", Toast.LENGTH_SHORT).show();
                    return;
                }

                //disable the NumericPicker
                disableNumbericKey();

                // change the text on the button to 'STOP BROADCAST'
                bc.setText(getResources().getString(R.string.unbroadcast_button_text));

                //make the confirmation text view visible on broadcast
                text.setVisibility(View.VISIBLE);

                createActiveNotification(temp);

                Log.i(LOG_TAG, "unique key of the user is : " + temp);
            }

        } else {
            bc.setText(getResources().getString(R.string.broadcast_button_text));
            //stop broadcast
            temp.append("00000");

            //enable the NumericPicker
            enableNumbericKey();
            text.setVisibility(View.INVISIBLE);


            //remove notification
            NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
        }
        UNKEY=temp.toString();
        //send data to app server
        shareTask = new ShareExternalServer(getActivity(), FirstLaunch.reg_id, temp.toString());
        shareTask.execute(null, null, null);
    }

    private void createActiveNotification(StringBuilder temp) {
        //We get a reference to the NotificationManager
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        //The three parameters are: 1. an icon, 2. a title, 3. time when the notification appears

        String MyNotificationTitle = "Track It";
        String MyNotificationText  = "Broadcasting at "+temp.toString();

        //for showing the ActiveFragment
        Intent MyIntent = new Intent(getActivity(), StartupScreen.class);
        MyIntent.putExtra("NotificationMode",true);
        MyIntent.putExtra("UniqueKey", temp.toString());
//            MyIntent.putExtra("context",getActivity());

        //for action button in notification, it calls a Broadcast Receiver-|
        Intent buttonIntent = new Intent(getActivity(), ButtonReceiver.class);
        buttonIntent.putExtra("notificationId",NOTIFICATION_ID);

        //for showing the ActiveFragment
        PendingIntent StartIntent = PendingIntent.getActivity(getActivity(),0,MyIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        //for action button in notification
        PendingIntent buttonPendingIntent = PendingIntent.getBroadcast(getActivity(), 0, buttonIntent,0);

        //issuing the notification
        NotificationCompat.Builder mb = new NotificationCompat.Builder(getActivity().getBaseContext());
        mb.setSmallIcon(R.drawable.logo_notify);
        mb.setContentTitle(MyNotificationTitle);
        mb.setContentText(MyNotificationText);
        mb.setPriority(NotificationCompat.PRIORITY_HIGH);
        mb.setOngoing(true);
        mb.setStyle(new NotificationCompat.BigTextStyle().bigText(MyNotificationText));
        mb.setContentIntent(StartIntent);
        mb.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Broadcast",buttonPendingIntent );

        //starting the notification via notification manager
        notificationManager.notify(NOTIFICATION_ID , mb.build());
        //We are passing the notification to the NotificationManager with a unique id.
    }

    public void enableNumbericKey() {
        for (NumberPicker s : key) {
            s.setEnabled(true);
        }

    }

    public void disableNumbericKey() {
        for (NumberPicker s : key) {
            s.setEnabled(false);
        }

    }

    public void stopBroadcast() {
        if(bc.getText().toString().equals(getResources().getString(R.string.unbroadcast_button_text)))
            performServerSync(getResources().getString(R.string.unbroadcast_button_text));
    }


    public void setUniqueKey(String uniqueKey) {
        Log.i(LOG_TAG,uniqueKey);
        int i=0;
       for(char c : uniqueKey.toCharArray()){
           key[i].setValue(Integer.parseInt(Character.toString(c)));
           i++;
       }
        disableNumbericKey();
    }

    private void buildAlertMessageNoGps(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}

