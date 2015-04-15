package in.co.varunbansal.linuxpi.trackit.fragments;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.*;

import in.co.varunbansal.linuxpi.trackit.connection.handler.ShareExternalServer;
import in.co.varunbansal.linuxpi.trackit.main.FirstLaunch;

public class ButtonReceiver extends BroadcastReceiver {
    public ButtonReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationId = intent.getIntExtra("notificationId", 0);

        // Do what you want were.
        //send data to app server
        new ShareExternalServer(context, FirstLaunch.reg_id, "00000").execute(null, null, null);



        // if you want cancel notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);

        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
