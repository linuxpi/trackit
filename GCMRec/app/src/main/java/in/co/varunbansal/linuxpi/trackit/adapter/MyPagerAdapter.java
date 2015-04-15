package in.co.varunbansal.linuxpi.trackit.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import in.co.varunbansal.linuxpi.trackit.fragments.ActiveFragment;
import in.co.varunbansal.linuxpi.trackit.fragments.PassiveFragment;

import static in.co.varunbansal.linuxpi.trackit.helper.StaticConstants.*;

public class MyPagerAdapter extends FragmentPagerAdapter {

    public static Fragment fragment;
    Context context;
    private String unKey;

    public MyPagerAdapter(FragmentManager fm, Context context,String unKey) {
        super(fm);
        this.context = context;
        this.unKey=unKey;
    }

    @Override
    public Fragment getItem(int position) {


        if (position == ACTIVE) {
            if(unKey!=null)
                fragment = new ActiveFragment(unKey);
            else
                fragment = new ActiveFragment();
        } else {
            fragment = new PassiveFragment();
            fragment.getId();
        }
        return fragment;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getCount() {
        return 2;
    }
}