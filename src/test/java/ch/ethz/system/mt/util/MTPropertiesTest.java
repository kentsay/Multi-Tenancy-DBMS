package ch.ethz.system.mt.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by braunl on 25.09.15.
 */
public class MTPropertiesTest {

    @Test
    public void testDefaultConstructorAndSetters() {
        MTProperties properties = new MTProperties();
        properties.setClientTenantID(1);
        properties.setDataSet(2,3);
        assert (properties.getClientTenandID() == 1);
        List<Integer> dataset = properties.getDataSet();
        assert (dataset.get(0) == 2 && dataset.get(1) == 3);

        properties.setDataSet(new Integer[]{4,5});
        dataset = properties.getDataSet();
        assert (dataset.get(0) == 4 && dataset.get(1) == 5);
    }

    @Test
    public void testOtherCunstructors() {
        MTProperties properties = new MTProperties(1,2,3);
        assert (properties.getClientTenandID() == 1);
        List<Integer> dataset = properties.getDataSet();
        assert (dataset.get(0) == 2 && dataset.get(1) == 3);

        properties = new MTProperties(1, new Integer[]{4,5});
        dataset = properties.getDataSet();
        assert (dataset.get(0) == 4 && dataset.get(1) == 5);
    }
}
