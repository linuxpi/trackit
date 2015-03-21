package in.co.javapapers.sample;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import in.co.varunbansal.linuxpi.trackit.R;
import in.co.varunbansal.linuxpi.trackit.connection.handler.ShareExternalServer;

public class MainActivity extends Activity {

    ShareExternalServer appUtil;
    String regId;
    AsyncTask shareRegidTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        appUtil = new ShareExternalServer();

        regId = getIntent().getStringExtra("regId");
        Log.d("MainActivity", "regId: " + regId);

        final Context context = this;
        shareRegidTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                new ShareExternalServer(context, regId, 11111).execute(null, null, null);
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                shareRegidTask = null;
                Toast.makeText(getApplicationContext(), result.toString(),
                        Toast.LENGTH_LONG).show();
            }
        };
        shareRegidTask.execute(null, null, null);
    }

}