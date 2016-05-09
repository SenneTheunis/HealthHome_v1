package com.example.elke.healthh;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

/**
 * Created by elke on 18/03/2016.
 */
public class Fragment_two extends Fragment {

    TextView tv;

    public static Fragment_two newInstance() {
        Fragment_two fragment = new Fragment_two();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.myfragmenttwo, container, false);
        //tv = (TextView) rootView.findViewById(R.id.tv2);


        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 8.2),
                new DataPoint(1, 8.4),
                new DataPoint(2, 8.6),
                new DataPoint(3, 8.5),
                new DataPoint(4, 8.6),
                new DataPoint(5, 8.7),
                new DataPoint(6, 8.6),
                new DataPoint(7, 8.6),
                new DataPoint(8, 8.6),
                new DataPoint(9, 8.6),
                new DataPoint(10, 8.6),
                new DataPoint(11, 8.6),
                new DataPoint(12, 8.6),
                new DataPoint(13, 8.6),

        });
        graph.addSeries(series);
        series.setColor(Color.BLUE);
        series.setTitle("lower pressure");
        series.setSize(10);


        PointsGraphSeries<DataPoint> series2 = new PointsGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 12.8),
                new DataPoint(1, 12.9),
                new DataPoint(2, 12.6),
                new DataPoint(3, 12.9),
                new DataPoint(4, 13.0),
                new DataPoint(5, 13.2),
                new DataPoint(6, 13.0),
                new DataPoint(7, 13.0),
                new DataPoint(8, 13.0),
                new DataPoint(9, 13.0),
                new DataPoint(10, 13.0),
                new DataPoint(11, 13.0),
                new DataPoint(12, 13.0),
                new DataPoint(13, 13.0)

        });
        graph.addSeries(series2);
        series2.setColor(Color.RED);
        series2.setTitle("upper pressure");
        series2.setSize(10);

        graph.setTitle("bloodpressure");
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(5);
        graph.getViewport().setMaxY(17);


        graph.getViewport().setScalable(true);       //makes the data fit on whole grapgh,
        graph.getViewport().setScrollable(true);     //maar zet assen ook op kommmagetallen

    return rootView;
}
}
