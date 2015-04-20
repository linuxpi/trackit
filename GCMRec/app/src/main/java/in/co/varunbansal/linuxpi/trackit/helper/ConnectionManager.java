package in.co.varunbansal.linuxpi.trackit.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by shivam on 20/4/15.
 */
public class ConnectionManager {
    private Context context;

    public ConnectionManager(Context context) {
        this.context = context;
    }
    public boolean isConnectionAvailable(){
        ConnectivityManager cm= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm!=null){
            NetworkInfo[] info=cm.getAllNetworkInfo();
            if(info!=null){
                for(int i=0;i<info.length;i++){
                    if(info[i].getState()== NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
