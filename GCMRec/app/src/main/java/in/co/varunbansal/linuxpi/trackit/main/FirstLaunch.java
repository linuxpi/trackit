package in.co.varunbansal.linuxpi.trackit.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import in.co.varunbansal.linuxpi.trackit.connection.handler.NewDeviceRegistration;
import in.co.varunbansal.linuxpi.trackit.R;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class FirstLaunch extends Activity {


    public static String reg_id;
    Context context;
    private SharedPreferences shdpref;
    private Button getRegId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_time_launch);

        context = getApplicationContext();

        final boolean isAlreadyRegistered = getRegIdIfAvailable();

        Log.i(LOG_TAG, "IS reg Present? " + isAlreadyRegistered);

        if (isAlreadyRegistered) {
            Intent intent = new Intent(FirstLaunch.this, StartupScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        getRegId = (Button) findViewById(R.id.getRegId);

        getRegId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAlreadyRegistered) {
                    new NewDeviceRegistration(context).execute(null, null, null);
                }
            }
        });
    }

    public boolean getRegIdIfAvailable() {
        if (shdpref == null)
            shdpref = getSharedPreferences(getString(R.string.sharedPrefsNameString), Context.MODE_PRIVATE);
        reg_id = shdpref.getString(REG_ID, null);
        if (reg_id != null) {
            //reg_id available. Device already registered
            Log.i(LOG_TAG, "reg_id" + reg_id);
            return true;
        } else {
            return false;
        }
    }
}
