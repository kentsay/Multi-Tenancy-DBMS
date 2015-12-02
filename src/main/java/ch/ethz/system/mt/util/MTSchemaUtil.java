package ch.ethz.system.mt.util;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

//TODO: I am certain this class is very useful, however you should definitely comment it more and maybe also
//use more types, e.g. you can use enums for table generality and attribute comparability...

/**
 * Created by kentsay on 8/28/15.
 */
public class MTSchemaUtil {

    static JSONParser parser = new JSONParser();

    public static String getMTSchema(String fileName) {
        String result = "";
        ClassLoader classLoader = MTSchemaUtil.class.getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
        } catch (IOException e) {
            System.err.println("Schema cannot be loaded, make sure your file exists");
        }
        return result;
    }

    public static String getMTTable(String schema) {
        String tableName = "";
        try {
            JSONObject obj = (JSONObject) parser.parse(schema);
            if (obj.containsKey("table_name")) {
                tableName = obj.get("table_name").toString();
            } else {
                throw new ParseException(0);
            }
        } catch (ParseException e) {
            System.err.println("Schema does not contains table information");
        }
        return tableName;
    }

    public static MTSchema.Attributes getMTAttribute(String schema, String key) {
        String attribute = "";
        try {
            JSONObject obj = (JSONObject) parser.parse(schema);
            if (obj.containsKey(key)) {
                attribute = obj.get(key).toString();
            } else {
                throw new ParseException(0);
            }
        } catch(ParseException e) {
            System.err.println("Key does not exists");
        }
        return MTSchema.Attributes.valueOf(attribute);
    }

    //attributes we want to filt out
    //e.g: table_name, ttid, etc
    public static ArrayList<String> getMTFilter() {
        ArrayList<String> blackList = new ArrayList<String>();

        blackList.add("table_name");
        blackList.add("ttid");

        return blackList;
    }

    public static ArrayList<String> getMTKeys(String schema) {
        ArrayList<String> keyList = new ArrayList<String>();
        try {
            JSONObject obj = (JSONObject) parser.parse(schema);
            if (obj.isEmpty()) {
                throw new ParseException(0);
            }
            else {
                Iterator<?> keys = obj.keySet().iterator();
                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    //only add table attributes into key array
                    if (!MTSchemaUtil.getMTFilter().contains(key)) {
                        keyList.add(key);
                    }
                }
            }
        } catch (ParseException e) {
            System.err.println("Schema is empty");
        }
        return keyList;
    }

    //testing
    public static void main(String[] argv) {
        String test = MTSchemaUtil.getMTSchema("output/mt.json");
        System.out.println(test);
        System.out.println(MTSchemaUtil.getMTTable(test));
        System.out.println(MTSchemaUtil.getMTAttribute(test, "region_id"));
        System.out.println(MTSchemaUtil.getMTKeys(test));
    }
}
//end of MTSchemaUtil
