package in.co.varunbansal.linuxpi.trackit.connection.handler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.co.varunbansal.linuxpi.trackit.helper.Config;
import in.co.varunbansal.linuxpi.trackit.main.StartupScreen;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    private double latFromString;
    private double lonFromString;

    @Override
    public void onReceive(Context context, Intent intent) {
        //if the device is in passive mode then parse the data from server
        Log.i(LOG_TAG, "Received Notification from server");
        if (!StartupScreen.MODE) {
            Bundle extras = intent.getExtras();
            String data = extras.get(Config.MESSAGE_KEY).toString();
            //parse the data first
            Log.i(LOG_TAG, "Received raw data  : " + data + "(" + data.length() + ")");
            if (data.charAt(0) != 'g') {
                ArrayList<String> tempData = null;
                String unKey = null;

                Intent i = new Intent(ACTIVE_USERS_LIST_UPDATE_INTENT_TAG);

                if (data.charAt(0) == '[') {
                    tempData = parseDataStringToArrayList(data);
                    i.putStringArrayListExtra(ACTIVE_USERS_ARRAY_LIST, tempData);
                } else {
                    unKey = data;
                    i.putExtra("unKey", unKey);
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
            } else if (data.charAt(0) == 'g') {
                Log.i(LOG_TAG, "Received location data");
//                Toast.makeText(context, "Location data :: " + data, Toast.LENGTH_Short).show();
//                List<Address> add;
//
//                double lat = getLatFromString(data);
//                double lon = getLonFromString(data);
//
//                Geocoder geocoder = new Geocoder(context);
//                try {
//                    add = geocoder.getFromLocation(lat, lon, 1);
//                    Address locationAdd = add.get(0);
//                    Toast.makeText(context,"Locality :: "+locationAdd.getAddressLine(0),Toast.LENGTH_LONG).show();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                    i.putExtra("locationData",locationAdd.getAddressLine(0));
                Intent i = new Intent(ACTIVE_USERS_LIST_UPDATE_INTENT_TAG);

                i.putExtra("locationData",data);
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);

            }
            setResultCode(Activity.RESULT_OK);
        }
    }

    private ArrayList<String> parseDataStringToArrayList(String data) {
        ArrayList<String> tempData = new ArrayList<>();
        if (data.charAt(0) == '[') {
            int first = data.indexOf('[');
            int last = data.indexOf(',');
            while (last < data.length()) {
                if(last>0) {
                    String tempString = data.substring(first + 1, last);
                    Log.i(LOG_TAG, "data : " + tempString);
                    tempData.add(tempString);
                    first = last + 1;
                    last = data.indexOf(',', first);
                    if (last < 0 - 1) {  //end of the data string
                        tempData.add(data.substring(first + 1, data.length() - 1));
                        break;
                    }
                }else{
                    tempData.add(data.substring(first + 1, data.length() - 1));
                    break;
                }
                Log.d(LOG_TAG, "Last = " + last);
            }


            Log.i(LOG_TAG, "temp data : " + tempData);
            return tempData;
        }
        return null;
    }


}