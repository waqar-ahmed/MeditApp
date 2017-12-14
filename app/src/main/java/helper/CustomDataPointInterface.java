package helper;

import com.jjoe64.graphview.series.DataPointInterface;

/**
 * Created by medit on 12/14/2017.
 */

public interface CustomDataPointInterface extends DataPointInterface {
    /**
     * @return the Label
     */
    public String getLabel();
}
