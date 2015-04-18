package in.co.varunbansal.linuxpi.trackit.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import in.co.varunbansal.linuxpi.trackit.connection.handler.NewDeviceRegistration;
import in.co.varunbansal.linuxpi.trackit.R;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class FirstLaunch extends Activity {


    public static String reg_id;
    Context context;
    private SharedPreferences shdpref;
    private Button getRegId;
    private Button regUser;
    private EditText name;
    private EditText email;

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
        regUser = (Button) findViewById(R.id.submit);
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);

        getRegId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                (findViewById(R.id.layout_gcm)).setVisibility(View.GONE);

                (findViewById(R.id.layout_email)).setVisibility(View.VISIBLE);


            }
        });

        regUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameString = name.getText().toString();
                String emailString = name.getText().toString();

                if(nameString==null || nameString.isEmpty() || emailString==null || emailString.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Enter required information!",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isAlreadyRegistered) {
                    new NewDeviceRegistration(context,nameString,emailString).execute(null, null, null);
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
