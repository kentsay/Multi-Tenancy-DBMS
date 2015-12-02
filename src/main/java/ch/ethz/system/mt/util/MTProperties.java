package ch.ethz.system.mt.util;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Created by braunl on 25.09.15.
 */
public class MTProperties extends Properties {

    public final String CLIENT_TENANT_ID_PROPERTY_NAME = "client_ttid";
    public final String DATASET_PROPERTY_NAME = "dataset_ttids";

    public MTProperties() {
        super();
    }

    public MTProperties(Properties defaults)
    {
        super(defaults);
    }

    public MTProperties(int client_ttid, Collection<Integer> dataset_ttids) {
        this();
        setClientTenantID(client_ttid);
        setDataSet(dataset_ttids);
    }

    public MTProperties(int client_ttid, Integer... dataset_ttids) {
        this();
        setClientTenantID(client_ttid);
        setDataSet(dataset_ttids);
    }

    public void setClientTenantID(int ttid) {
        setProperty(CLIENT_TENANT_ID_PROPERTY_NAME, String.valueOf(ttid));
    }

    public void setDataSet(Collection<Integer> ttids) {
        setProperty(DATASET_PROPERTY_NAME, StringUtils.join(ttids, ","));
    }

    public void setDataSet(Integer... ttids) {
        setDataSet(ImmutableList.copyOf(ttids));
    }

    public int getClientTenandID() {
        return Integer.valueOf(getProperty(CLIENT_TENANT_ID_PROPERTY_NAME));
    }

    public List<Integer> getDataSet() {
        String[] split = getProperty(DATASET_PROPERTY_NAME).split(",");
        List<Integer> result = new ArrayList<>(split.length);
        for (String ttid : split)
        {
            result.add(Integer.valueOf(ttid));
        }
        return result;
    }
}
