package in.co.varunbansal.linuxpi.trackit.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import in.co.varunbansal.linuxpi.trackit.R;
import in.co.varunbansal.linuxpi.trackit.connection.handler.ShareExternalServer;
import in.co.varunbansal.linuxpi.trackit.main.FirstLaunch;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class ActiveFragment extends Fragment {

    public static String FRAGMENT_TAG;
    NumberPicker[] key;
    Button bc;
    ShareExternalServer shareTask;

    public ActiveFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.active_layout, container, false);

        key = new NumberPicker[5];

        key[0] = (NumberPicker) myView.findViewById(R.id.un1);
        key[1] = (NumberPicker) myView.findViewById(R.id.un2);
        key[2] = (NumberPicker) myView.findViewById(R.id.un3);
        key[3] = (NumberPicker) myView.findViewById(R.id.un4);
        key[4] = (NumberPicker) myView.findViewById(R.id.un5);

        for (NumberPicker s : key) {
            s.setMaxValue(9);
            s.setMinValue(0);
        }

        FRAGMENT_TAG = getTag();

        bc = (Button) myView.findViewById(R.id.active_broadcast);

        bc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                performServerSync(bc.getText().toString());

            }
        });

        return myView;
    }

    private void performServerSync(String condition) {
        int temp = 0;

        if (condition.equals(getResources().getString(R.string.broadcast_button_text))) {

            bc.setText(getResources().getString(R.string.unbroadcast_button_text));

            for (NumberPicker s : key) {
                temp = temp * 10 + s.getValue();
            }

            Log.i(LOG_TAG, "unique key of the user is : " + temp);

//                getLocationData();

        } else {
            bc.setText(getResources().getString(R.string.broadcast_button_text));
            //stop broadcast
        }

        //send data to app server
        shareTask = new ShareExternalServer(getActivity(), FirstLaunch.reg_id, temp);
        shareTask.execute(null, null, null);
    }

    public void stopBroadcast() {
        if(bc.getText().toString().equals(getResources().getString(R.string.unbroadcast_button_text)))
            performServerSync(getResources().getString(R.string.unbroadcast_button_text));
    }


}

