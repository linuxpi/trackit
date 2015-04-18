package in.co.varunbansal.linuxpi.trackit.connection.handler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import in.co.varunbansal.linuxpi.trackit.R;
import in.co.varunbansal.linuxpi.trackit.helper.Config;
import in.co.varunbansal.linuxpi.trackit.main.FirstLaunch;
import in.co.varunbansal.linuxpi.trackit.main.StartupScreen;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class NewDeviceRegistration extends AsyncTask {

    private GoogleCloudMessaging gcmInstance;
    private Context context;
    private SharedPreferences shdpref;
    private String name;
    private String email;

    public NewDeviceRegistration(Context context) {
        this(context,null,null);
    }

    public NewDeviceRegistration(Context context,String name,String email){
        this.name=name;
        this.email=email;
        this.context = context;
        if (gcmInstance == null)
            gcmInstance = GoogleCloudMessaging.getInstance(context);
    }

    @Override
    protected Object doInBackground(Object[] params) {
        String reg_id;
        try {
            reg_id = gcmInstance.register(Config.GOOGLE_PROJECT_ID);

            Log.i(LOG_TAG, "Got Reg Id from GCM. Storing in Prefs");
            Log.i(LOG_TAG, "Reg ID from GCM : " + reg_id);

            FirstLaunch.reg_id = reg_id;

            String result = "";
            Map paramsMap = new HashMap();
            paramsMap.put("name", name);
            paramsMap.put("email", email);
            paramsMap.put("regId", FirstLaunch.reg_id);

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
                Iterator<Map.Entry<String, String>> iterator = paramsMap.entrySet()
                        .iterator();

                while (iterator.hasNext()) {
                    Map.Entry param = iterator.next();
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


        } catch (IOException e) {
            reg_id = null;
            e.printStackTrace();
            Log.i(LOG_TAG, "Exception in Async Task : \n" + e.getStackTrace());
        }


        return reg_id;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        String regId = (String) o;
        if (!TextUtils.isEmpty(regId)) {
            Intent startApp = new Intent(context, StartupScreen.class);
            startApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //for calling the activity outside the context
            context.startActivity(startApp);
            storeRegistrationIdLocally((String) o);
        } else {
            Toast.makeText(context, "Device could not be registered. Retry!", Toast.LENGTH_SHORT).show();
        }

    }

    public void storeRegistrationIdLocally(String data) {
        if (shdpref == null)
            shdpref = context.getSharedPreferences(context.getString(R.string.sharedPrefsNameString), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shdpref.edit();
        editor.putString(REG_ID, data);
        editor.apply();
    }
}
