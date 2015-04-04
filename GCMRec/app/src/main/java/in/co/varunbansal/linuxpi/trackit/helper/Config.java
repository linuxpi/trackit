package in.co.varunbansal.linuxpi.trackit.helper;

public interface Config {

    // used to share GCM regId with application server - using php app server
//    static final String APP_SERVER_URL = "http://192.168.1.17/gcm/gcm.php?shareRegId=1";

    // GCM server using java
    static final String APP_SERVER_URL ="http://varun.varunbansal.co.in/com.lpu.android.trackme/AppServer?shareRegId=1";
    static final String APP_SERVER_URL_LOC ="http://varun.varunbansal.co.in/com.lpu.android.trackme/AppServer?shareRegId=2";
//    static final String APP_SERVER_URL ="http://localhost:8080/com.lpu.android.trackme/AppServer?shareRegId=1";

    // Google Project Number
    static final String GOOGLE_PROJECT_ID = "285694043566";
    static final String MESSAGE_KEY = "message";

}

