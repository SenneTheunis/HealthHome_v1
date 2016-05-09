package com.example.elke.healthh;

/**
 * Created by elke on 14/04/2016.
 */
public class DataPoint implements com.jjoe64.graphview.series.DataPointInterface{

    double x;
    double y;

    public DataPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
