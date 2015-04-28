package in.co.varunbansal.linuxpi.trackit.main;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import in.co.varunbansal.linuxpi.trackit.R;
public class Splash extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        final ProgressBar pb = (ProgressBar) findViewById(R.id.pb1);
        Thread t = new Thread() {
            public void run() {
                int progress = 0;
                try {
                    while(progress < 100) {
                        sleep(300);
                        pb.setProgress(progress);
                        progress = progress + 10;
                    }
                } catch( InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent i = new Intent(Splash.this, FirstLaunch.class);
                    startActivity(i);
                }
            }
        };
        t.start();
    }
    @Override
    public void onPause(){
        super.onPause();
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
// Handle action bar item clicks here. The action bar will
// automatically handle clicks on the Home/Up button, so long
// as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}