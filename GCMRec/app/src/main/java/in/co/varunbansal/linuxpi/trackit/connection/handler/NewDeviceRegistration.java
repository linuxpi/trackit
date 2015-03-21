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

import in.co.varunbansal.linuxpi.trackit.R;
import in.co.varunbansal.linuxpi.trackit.helper.Config;
import in.co.varunbansal.linuxpi.trackit.main.FirstLaunch;
import in.co.varunbansal.linuxpi.trackit.main.StartupScreen;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class NewDeviceRegistration extends AsyncTask {

    private GoogleCloudMessaging gcmInstance;
    private Context context;
    private SharedPreferences shdpref;

    public NewDeviceRegistration(Context context) {
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
