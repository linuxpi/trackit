package in.co.varunbansal.linuxpi.trackit.fragments;


import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.co.varunbansal.linuxpi.trackit.R;
import in.co.varunbansal.linuxpi.trackit.adapter.ActiveUserListAdapter;
import in.co.varunbansal.linuxpi.trackit.connection.handler.RequestLocationData;
import in.co.varunbansal.linuxpi.trackit.main.FirstLaunch;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class PassiveFragment extends Fragment {

    public static String FRAGMENT_TAG;
    private static ActiveUserListAdapter  adapter;
    private static ArrayList<String> activeUsers;
    private ListView activeUsersList;
    private String locationString;
    private boolean window = true;  //open
    private View v;
    Button locationPlot;
    RequestLocationData req;

    public PassiveFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.passive_layout, container, false);

        activeUsers = new ArrayList<>();
//        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, activeUsers);
         adapter = new ActiveUserListAdapter(getActivity(), R.layout.active_user_list_item,R.id.textview_active_user, activeUsers);
        activeUsersList = (ListView) myView.findViewById(R.id.active_user_list);
        activeUsersList.setAdapter(adapter);

        FRAGMENT_TAG = getTag();

        activeUsersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                for(int i=0;i<parent.getAdapter().getCount();i++){
//                    View tempView = (View)parent.getAdapter().getView(i,view,parent);
//                    Log.i(LOG_TAG,"Found View :: "+tempView);
//                    ((LinearLayout)tempView.findViewById(R.id.location_group)).setVisibility(View.GONE);
//                }
                if(v==view){
                    if(window)
                        window=false;
                    else
                        window=true;
                }
                String data = adapter.getItem(position);
                Log.i(LOG_TAG,"Adapter Data :: " + data);
                String unKey = getUnkeyFromString(data);
                String serial = getSerialFromString(data);
                Log.i(LOG_TAG,"CLicked :: " + unKey);
                Log.i(LOG_TAG,"CLicked :: " + serial);

                if (v!=null) {
                    LinearLayout llOld = (LinearLayout) v.findViewById(R.id.location_group);
                    if(llOld.getVisibility()==View.VISIBLE){
                        req.cancel(true);
                        TextView tv = (TextView) v.findViewById(R.id.location_string);
                        tv.setText("getting location ...");
                        locationPlot.setOnClickListener(null);
                        llOld.setVisibility(View.GONE);
                    }
                }

                if (v!=view || window) {
//                    window=false;
                    LinearLayout ll = (LinearLayout) view.findViewById(R.id.location_group);
                    if(ll.getVisibility()==View.GONE){
                        ll.setVisibility(View.VISIBLE);
                        //send the request to server to fetch the location
                        req = new RequestLocationData(getActivity(),serial,unKey,FirstLaunch.reg_id,view);
                        req.execute(null,null,null);

                        locationPlot = (Button) view.findViewById(R.id.location_plot);
                        locationPlot.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View vn) {
                                Uri locationUri = Uri.parse(((TextView) v.findViewById(R.id.location_dats)).getText().toString());
                                Log.i(LOG_TAG,"location Uri :: " + locationUri.toString());

                                plotOnMap(locationUri);

                            }
                        });

                    }
                }
                v=view;
            }
        });

        return myView;
    }

    private void plotOnMap(Uri locationUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(locationUri);
        if(intent.resolveActivity(getActivity().getPackageManager())!=null){
            startActivity(intent);
        }
    }

    private String getSerialFromString(String data) {
        return data.substring(data.indexOf('|')+1);
    }

    private String getUnkeyFromString(String data) {
        return data.substring(0,data.indexOf('|'));
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
        Log.i(LOG_TAG, "removing entry :: " + unKey);
//        if(activeUsers.contains(unKey)) {
//            activeUsers.remove(activeUsers.indexOf(unKey));
//            adapter.notifyDataSetChanged();
//        }
            String r[] = unKey.split("\\|");
            Log.i(LOG_TAG,r[0]);
          int count = activeUsers.size();
        for(int i=0;i<count;i++){
            String data = activeUsers.get(i);
            String sp[]=data.split("\\|");
            Log.i(LOG_TAG,data);
            if(r[0].equals(sp[0]) && r[1].equals(sp[1])){
                activeUsers.remove(i);
                adapter.notifyDataSetChanged();
                break;
            }
        }


    }

    public void setLocationString(String locationString) {

        //parse the raw string
        List<Address> add;

        double lat = getLatFromString(locationString);
        double lon = getLonFromString(locationString);
        String unKeyString = getUnKeyFromLocationString(locationString);
        Address locationAdd;
        Geocoder geocoder = new Geocoder(getActivity());
        try {
            add = geocoder.getFromLocation(lat, lon, 1);
            locationAdd = add.get(0);

        }catch (IOException e){
            locationAdd=null;
            e.printStackTrace();
            Toast.makeText(getActivity(),"Error parsing the location data",Toast.LENGTH_SHORT).show();
        }

        if(((TextView)v.findViewById(R.id.textview_active_user)).getText().toString().equals(unKeyString)){
            LinearLayout ll = (LinearLayout) v.findViewById(R.id.location_group);
            if(ll.getVisibility()==View.VISIBLE){
                TextView tv = (TextView) v.findViewById(R.id.location_string);
                if (locationAdd != null) {
                    Log.i(LOG_TAG,"setting the location :: " + locationAdd.toString());
                    tv.setText(locationAdd.getAddressLine(0));
                }else{
                    tv.setText("Error!");
                }



                TextView tv1 = (TextView) v.findViewById(R.id.location_dats);
                tv1.setText("geo:0,0?q="+Double.toString(lat)+","+Double.toString(lon)+"("+unKeyString+")");
        }

        }


    }

    private String getUnKeyFromLocationString(String locationString) {
        return locationString.substring(locationString.indexOf('?')+1);
    }

    public double getLatFromString(String data) {
        return Double.parseDouble(data.substring(data.indexOf('|')+1,data.indexOf('?')-1));
    }

    public double getLonFromString(String data) {
        return Double.parseDouble(data.substring(3,data.indexOf('|')-1));
    }

    public String getLocationString() {
        return locationString;
    }
}

