package helper;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by medit on 12/14/2017.
 */

public class CustomDataPoint implements CustomDataPointInterface, Serializable {
    private static final long serialVersionUID=1428263322645L;

    private double x;
    private double y;
    private String label;

    public CustomDataPoint(double x, double y, String label) {
        this.x=x;
        this.y=y;
        this.label = label;
    }

    public CustomDataPoint(Date x, double y, String label) {
        this.x = x.getTime();
        this.y = y;
        this.label = label;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "["+x+"/"+y+"]:" + label;
    }
}