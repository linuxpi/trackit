package in.co.varunbansal.linuxpi.trackit.main;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import in.co.varunbansal.linuxpi.trackit.adapter.MyPagerAdapter;
import in.co.varunbansal.linuxpi.trackit.adapter.OnPageChangeAdapter;
import in.co.varunbansal.linuxpi.trackit.fragments.ActiveFragment;
import in.co.varunbansal.linuxpi.trackit.fragments.PassiveFragment;
import in.co.varunbansal.linuxpi.trackit.R;
import in.co.varunbansal.linuxpi.trackit.connection.handler.ShareExternalServer;
import in.co.varunbansal.linuxpi.trackit.adapter.TabAdapter;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class StartupScreen extends FragmentActivity{

    public static boolean MODE = false;
    private LinearLayout activeLayout;
    private LinearLayout passiveLayout;
    private LinearLayout superLayout;
    private ActionBar actionBar;
    private Context context;
    private ViewPager tabView;
    private MyPagerAdapter adapter;
    private ShareExternalServer passiveModeTransitionThread;
    private ColorDrawable cdDarkGreen[];

    private BroadcastReceiver OnActiveUserListUpdateReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> activeUsers = intent.getStringArrayListExtra(ACTIVE_USERS_ARRAY_LIST);
            String unKey;

            if(passiveFragment==null)
                passiveFragment = (PassiveFragment) getSupportFragmentManager().findFragmentByTag(PassiveFragment.FRAGMENT_TAG);

            if(activeUsers==null){
                unKey=intent.getStringExtra(UN_KEY);
                if(unKey==null){
                    String location = intent.getStringExtra("locationData");
                    if(location!=null)
                        passiveFragment.setLocationString(location);
                    else
                        Toast.makeText(getApplicationContext(),"Location not Available!",Toast.LENGTH_SHORT).show();

                }else{
                    Log.i(LOG_TAG, "data recieved : " + unKey);
                    if(unKey.charAt(0)=='-'){
                        passiveFragment.removeActiveUser(unKey.substring(1));
                    }else {
                        passiveFragment.addNewActiveUser(unKey.substring(1));
                    }
                }
            }else{
                Log.i(LOG_TAG, "data recieved : " + activeUsers.toString());
                if(passiveFragment!=null)
                passiveFragment.updateActiveUsersList(activeUsers);
            }

        }
    };

    private PassiveFragment passiveFragment;
    private ActiveFragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_choose_acitivity);

        variableInitialization();

        LocalBroadcastManager.getInstance(this)
                     .registerReceiver(OnActiveUserListUpdateReceived, new IntentFilter(ACTIVE_USERS_LIST_UPDATE_INTENT_TAG));

        activeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpInitialLayout(ACTIVE,null);
                //Turn on GPS

            }
        });

        passiveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpInitialLayout(PASSIVE,null);
            }
        });

        tabView.setOnPageChangeListener(new OnPageChangeAdapter() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);

                Log.i(LOG_TAG, "Performing Transition algorithm");
                switch (position) {
                    case ACTIVE:
                        //transition from passive to active
                        MODE = true;

                        //stop the process of passive user
                        if(!passiveModeTransitionThread.isCancelled()){
                            passiveModeTransitionThread.cancel(true);
                        }
                        if(passiveFragment!=null){
                            passiveFragment = (PassiveFragment) getSupportFragmentManager().findFragmentByTag(PassiveFragment.FRAGMENT_TAG);
                            passiveFragment.emptyList();
                        }

                        break;
                    case PASSIVE:

//                        //alert dialog
//                        tabView.setCurrentItem(ACTIVE);// change tab back to active

                        //transition from active to passive
                        MODE=false;
                        if(activeFragment==null) {
                            activeFragment = (ActiveFragment) getSupportFragmentManager().findFragmentByTag(ActiveFragment.FRAGMENT_TAG);
                        }
                        if(activeFragment!=null) {
                            activeFragment.stopBroadcast();
                        }
                        //ask the server for a list of active users
                        passiveModeTransitionThread =new ShareExternalServer(context, FirstLaunch.reg_id, PASSIVE_USER_UNIQUE_RESERVED_KEY);
                        passiveModeTransitionThread.execute(null, null, null);
                        break;
                    default:
                }

            }
        });

        ActionBar.TabListener tabListener = new TabAdapter() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                tabView.setCurrentItem(tab.getPosition(), true); //changes the page
                Log.i(LOG_TAG, "PAGE POSITION : " + tab.getPosition());
                actionBar.setBackgroundDrawable(cdDarkGreen[tab.getPosition()]);
            }
        };

        //setting the labels
        for (int i = 0; i < 2; i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(getResources().getStringArray(R.array.mode_label)[i])
                            .setTabListener(tabListener));
        }


        if(getIntent().getBooleanExtra("NotificationMode",false)){

            String un=null;
            un = getIntent().getStringExtra("UniqueKey");
            Log.i(LOG_TAG,un);
            setUpInitialLayout(ACTIVE,un);
            tabView.setCurrentItem(ACTIVE);
            if(activeFragment==null)
                activeFragment=(ActiveFragment) adapter.getItem(0);

            if(activeFragment!=null){
//                activeFragment.setUniqueKey(un);
            }

        }

    }

    private void variableInitialization() {
        context = getApplicationContext();

        actionBar = getActionBar();
        if (actionBar != null)
            actionBar.hide();

        activeLayout = (LinearLayout) findViewById(R.id.active_big_layout);
        passiveLayout = (LinearLayout) findViewById(R.id.passive_big_layout);
        superLayout = (LinearLayout) findViewById(R.id.super_mode_layout);

        tabView = (ViewPager) findViewById(R.id.tabs_view);
        tabView.setVisibility(View.GONE);
    }

    private void setUpInitialLayout(int mode, String unKey) {
        animateTabLayout();
        actionBar.show();

        cdDarkGreen = new ColorDrawable[2];

        cdDarkGreen[1] = new ColorDrawable(getResources().getColor(R.color.dark_blueGreen));
        cdDarkGreen[0] = new ColorDrawable(getResources().getColor(R.color.light_blueGreen));

        actionBar.setBackgroundDrawable(cdDarkGreen[mode]);


        actionBar.setTitle(getString(R.string.track_me_text));

//        if(unKey==null){
            adapter = new MyPagerAdapter(getSupportFragmentManager(), getApplicationContext(),unKey);
//        }
//        else{
//ada
//        }
        tabView.setAdapter(adapter);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getActionBar().setSelectedNavigationItem(mode);
    }

    private void animateTabLayout() {

        superLayout.setVisibility(View.GONE);
        tabView.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onStop() {
        Log.i(LOG_TAG,"onstop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG,"onpause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(LOG_TAG,"ondestroy");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.i(LOG_TAG,"onresume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.i(LOG_TAG,"onrestart");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.i(LOG_TAG,"onstart");
        super.onStart();
    }

}



