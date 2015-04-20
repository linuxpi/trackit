package in.co.varunbansal.linuxpi.trackit.connection.handler;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import in.co.varunbansal.linuxpi.trackit.helper.Config;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class RequestLocationData extends AsyncTask {

    private int uniqueKey;
    private String serial;
    private Context context;
    private String regId;
    private View view;

    public RequestLocationData(Context context, String serial, String unKey,String regId,View v) {
        this.context = context;
        this.uniqueKey = Integer.parseInt(unKey);
        this.serial = serial;
        this.regId=regId;
        this.view=v;
    }

    @Override
    protected Object doInBackground(Object[] params) {


        String result = "";
        Map paramsMap = new HashMap();
        paramsMap.put("unKey", uniqueKey);
        paramsMap.put("serial", serial);
        paramsMap.put("regId", regId);

        Log.i(LOG_TAG,paramsMap.toString());

        try {
            URL serverUrl = null;
            try {
                serverUrl = new URL(Config.APP_SERVER_URL_LOC);
            } catch (MalformedURLException e) {
                Log.e("AppUtil", "URL Connection Error: "
                        + Config.APP_SERVER_URL_LOC, e);
                result = "Invalid URL: " + Config.APP_SERVER_URL_LOC;
            }

            StringBuilder postBody = new StringBuilder();
            Iterator<Entry<String, String>> iterator = paramsMap.entrySet()
                    .iterator();

            while (iterator.hasNext()) {
                Entry param = iterator.next();
                postBody.append(param.getKey()).append('=')
                        .append(param.getValue());
                if (iterator.hasNext()) {
                    postBody.append('&');
                }
            }
            String body = postBody.toString();
            Log.i(LOG_TAG, "Body : " + body);
            byte[] bytes = body.getBytes();
            HttpURLConnection httpCon = null;
            try {
                httpCon = (HttpURLConnection) serverUrl.openConnection();
                httpCon.setDoOutput(true);
                httpCon.setUseCaches(false);
                httpCon.setFixedLengthStreamingMode(bytes.length);
                httpCon.setRequestMethod("POST");
                httpCon.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded;charset=UTF-8");
                OutputStream out = httpCon.getOutputStream();
                out.write(bytes);
                out.close();

                int status = httpCon.getResponseCode();
                if (status == 200) {
                    result = "Request sent to server for location data";
                } else {
                    result = "Post Failure." + " Status: " + status;
                }
            } finally {
                if (httpCon != null) {
                    httpCon.disconnect();
                }
            }

        } catch (IOException e) {
            result = "Post Failure. Error in sharing with App Server.";
            Log.e("AppUtil", "Error in sharing with App Server: " + e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Log.i(LOG_TAG, "result =  " + o);
    }


}