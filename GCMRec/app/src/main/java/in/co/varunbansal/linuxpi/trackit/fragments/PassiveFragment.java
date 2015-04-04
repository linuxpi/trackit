package in.co.varunbansal.linuxpi.trackit.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

import in.co.varunbansal.linuxpi.trackit.R;
import in.co.varunbansal.linuxpi.trackit.adapter.ActiveUserListAdapter;
import in.co.varunbansal.linuxpi.trackit.connection.handler.RequestLocationData;
import in.co.varunbansal.linuxpi.trackit.main.FirstLaunch;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class PassiveFragment extends Fragment {

    public static String FRAGMENT_TAG;
    private static ArrayAdapter<String> adapter;
    private static ArrayList<String> activeUsers;
    private ListView activeUsersList;

    public PassiveFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.passive_layout, container, false);

        activeUsers = new ArrayList<>();
        adapter = new ActiveUserListAdapter(getActivity(), R.layout.active_user_list_item,R.id.textview_active_user, activeUsers);

        activeUsersList = (ListView) myView.findViewById(R.id.active_user_list);
        activeUsersList.setAdapter(adapter);
        activeUsersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout ll = (LinearLayout) view.findViewById(R.id.location_group);
                if(ll.getVisibility()==View.VISIBLE){
                    ll.setVisibility(View.GONE);
                }
                else{
                    ll.setVisibility(View.VISIBLE);
                }
            }
        });

        FRAGMENT_TAG = getTag();

//        activeUsersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String data = adapter.getItem(position);
//                String unKey = data.substring(0,data.length()-2);
//                String serial = data.substring(data.length() - 1);
//                Log.i(LOG_TAG,"CLicked :: " + unKey);
//                Log.i(LOG_TAG,"CLicked :: " + serial);
//
//                //send the request to server to fetch the location
//                RequestLocationData req = new RequestLocationData(getActivity(),serial,unKey,FirstLaunch.reg_id);
//                req.execute(null,null,null);
//            }
//        });

        return myView;
    }

    public void updateActiveUsersList(ArrayList<String> list) {
//        activeUsers.remove(0);
        emptyList();
        activeUsers.addAll(list);
        adapter.notifyDataSetChanged();

        Log.i(LOG_TAG, "Task Complete");
    }

    public void emptyList(){
        activeUsers.clear();
        adapter.notifyDataSetChanged();
    }

    public void addNewActiveUser(String unKey) {
        activeUsers.add(unKey);
        adapter.notifyDataSetChanged();
    }

    public void removeActiveUser(String unKey) {
        Log.i(LOG_TAG,"removing entry :: "+unKey);
        if(activeUsers.contains(unKey)) {
            activeUsers.remove(activeUsers.indexOf(unKey));
            adapter.notifyDataSetChanged();
        }
    }
}

