package com.example.elke.healthh;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by elke on 18/03/2016.
 */
public class Fragment_three extends Fragment {

    TextView tv;

    public static Fragment_three newInstance() {
        Fragment_three fragment = new Fragment_three();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.myfragmentthree, container, false);

       // tv = (TextView) rootView.findViewById(R.id.tv3);

        return rootView;
    }
}
