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
public class My_Fragment extends Fragment {

    TextView tv;

    public static My_Fragment newInstance() {
        My_Fragment fragment = new My_Fragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.myfragment, container, false);
        tv = (TextView) rootView.findViewById(R.id.tv1);
        return rootView;
    }

}
