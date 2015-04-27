package in.co.varunbansal.linuxpi.trackit.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import in.co.varunbansal.linuxpi.trackit.R;
/**
 * Created by shivam on 4/4/15.
 */
public class ActiveUserListAdapter extends ArrayAdapter<String> {
    private Context context;
    private int resource;
    private ArrayList<String> list;
    public ActiveUserListAdapter(Context context, int resource, int textViewResourceId, ArrayList<String> list) {
        super(context, resource, textViewResourceId, list);
        this.context =context;
        this.list=list;
        this.resource=resource;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView t=null;
        TextView time=null;
        LinearLayout ll=null;
        if(convertView==null){
            LayoutInflater li= (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=li.inflate(resource,null);
            t=(TextView)convertView.findViewById(R.id.textview_active_user);
            time=(TextView)convertView.findViewById(R.id.textview_active_user_time);
            ll= (LinearLayout)convertView.findViewById(R.id.location_group);
        }
        else{
            Log.d("Yo","getview");
            t = (TextView)convertView.findViewById(R.id.textview_active_user);
            time = (TextView)convertView.findViewById(R.id.textview_active_user_time);
            ll= (LinearLayout)convertView.findViewById(R.id.location_group);
//t.setText(this.list.get(position).split("|")[0]);
        }
//t = (TextView)convertView.findViewById(R.id.textview_active_user);
        ll.setVisibility(View.GONE);
        String s= this.list.get(position);
        String sp[]=s.split("\\|");
        t.setText(sp[0]);
        time.setText(sp[2]+" minutes ago");
        return convertView;
    }
}
