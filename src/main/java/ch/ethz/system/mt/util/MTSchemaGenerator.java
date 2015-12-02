package ch.ethz.system.mt.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by kentsay on 7/9/15.
 *
 * Purpose:
 * We need to inform the DBMS to specify the table type(shared or tenant-specific). This can be done by using annotations in sql comments.
 * By parsing the sql comments, we can then translate the MT rules into a mt.json file. For example:
 *
 * {"table" : "Employee",
    "ttid" : "comparable",
    "employee_name" : "comparable",
    "salary" : "transformable",
    "function" : "public int salary(ttid, salary) {if (ttid == 0) return salary*1.05}"
   }
 *
 * The input sql script should be place under resources/input
 * The output json file will be place under resources/output
 *
 */

//TODO: add multiple table for the json file
    //TODO: change from JSONObject into JSONArray
    //TODO: modify the parsing rule, need a separator(e.g: #, newline, etc) between tables

public class MTSchemaGenerator {

    private ArrayList<String> sqlList = new ArrayList<>();
    private boolean mt_aware = false;

    public String readSqlFileToLine(String fileName) throws IOException {
        String line, statement = "";
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        while ((line = br.readLine()) != null) {
            if (ParserUtil.getMTAnnotation(line) != null) mt_aware = true;
            sqlList.add(line);
            statement += line.trim();
        }
        br.close();
        return statement;
    }

    public JSONObject parseCreateStatement(String statement) {

        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        CreateTable createTable;
        HashMap<String, String> attributeMap = new LinkedHashMap<>();
        JSONObject mtJson = new JSONObject();
        String colName, colDataType;

        try {
            createTable = (CreateTable) parserManager.parse(new StringReader(statement));
            for(int i =0; i < createTable.getColumnDefinitions().size(); i++) {
                colName = createTable.getColumnDefinitions().get(i).getColumnName();
                System.out.println(colName);
                //handle the extra space for JSQLParser of varchar, varchar (50) instead of varchar(50)
                colDataType = createTable.getColumnDefinitions().get(i).getColDataType().toString().replaceAll("\\s+","");
                for(String str: sqlList) {
                    if (str.contains(colName + " " + colDataType)) {
                        attributeMap.put(createTable.getColumnDefinitions().get(i).getColumnName(), ParserUtil.getMTAnnotation(str));
                    }
                }
            }
            //put in table information
            mtJson.put("table_name", createTable.getTable().toString());
            //put in column(attributes) information
            for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
                mtJson.put(entry.getKey(), entry.getValue());
            }
            //list.add(mtJson);
            //System.out.println(list.toJSONString());
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

        return mtJson;
    }

    public void writeJsonFile(JSONObject jsonObj, String fileName) throws IOException {
        //write json file into resources folder
        Writer out = new OutputStreamWriter(new FileOutputStream(fileName));
        try {
            out.write(jsonObj.toJSONString());
        }
        finally {
            out.close();
        }
    }

    public static void main(String[] args) throws IOException {
        MTSchemaGenerator generator = new MTSchemaGenerator();
        String schemaFile = "src/main/resources/input/test.sql";
        String sqlString = generator.readSqlFileToLine(schemaFile);
        if (generator.mt_aware) {
            System.out.println("MT Type Schema, generating mt.json ...");
            generator.writeJsonFile(generator.parseCreateStatement(sqlString), "src/main/resources/output/mt.json");
            System.out.println("Done!");
        } else {
            System.out.println("Not MT Type Schema, process end!");
        }
    }
}