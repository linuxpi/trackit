package in.co.varunbansal.linuxpi.trackit.connection.handler;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

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

public class ShareExternalServer extends AsyncTask implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener {

    private String regId;
    private String locationString;
    private int uniqueKey;
    private Context context;
    private String lat;
    private String lon;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocationService;

    public ShareExternalServer(Context context, String regId, int unKey) {
        this.context = context;
        this.regId = regId;
        this.uniqueKey = unKey;
    }

    @Override
    protected Object doInBackground(Object[] params) {


        String result = "";
        Map paramsMap = new HashMap();
        paramsMap.put("regId", regId);
        paramsMap.put("unKey", uniqueKey);

        if(uniqueKey!=0 || uniqueKey!=100000) {
            buildGoogleAPIClient();

            mGoogleApiClient.connect();

            while (!mGoogleApiClient.isConnected()) ;

            Log.i(LOG_TAG, "Location String :: " + locationString);
            while(locationString==null);
            paramsMap.put("locString", locationString);
        }
        try {
            URL serverUrl = null;
            try {
                serverUrl = new URL(Config.APP_SERVER_URL);
            } catch (MalformedURLException e) {
                Log.e("AppUtil", "URL Connection Error: "
                        + Config.APP_SERVER_URL, e);
                result = "Invalid URL: " + Config.APP_SERVER_URL;
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
                    result = "RegId shared with Application Server. RegId: "
                            + regId;
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


    public synchronized void buildGoogleAPIClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationService = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocationService != null) {
            lat = Double.toString(mLocationService.getLatitude());
            lon = Double.toString(mLocationService.getLongitude());
        }

        Log.i(LOG_TAG, "Latitude : " + lat);
        Log.i(LOG_TAG, "Longitude : " + lon);

        locationString = lon+"|"+lat;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

