package ch.ethz.system.mt.rules;

import ch.ethz.system.mt.util.MTSchemaUtil;
import ch.ethz.system.mt.util.ParserUtil;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by kentsay on 8/27/15.
 */
public class QueryRewrite {

    /**
     * Overwrite SQL query when match with Multi-tenancy Scan
     * Example:
     * select * from Employees --> select * from Employees where ttid in (0,1)
     *
     * @param
     * @return String
     */
    public static String MTScan(SqlSelect node, Collection<Integer> ttids) {
        /** In order to create a SqlNode, you have to use meaningful sql statement.
         *  That's why we need a sql statement that select from a any table, and we only need the where condition
         */

        String mtIdentify = "select * from \"any\" where \"ttid\" in (" + StringUtils.join(ttids, ",") + ")";
        SqlSelect node2;
        if (checkTableMatch(node)) {
            try {
                //keep the original where statement in the sql node and append new where statement behind
                if (node.hasWhere()) {
                    mtIdentify += " and " + node.getWhere().toSqlString(SqlDialect.CALCITE);
                }
                System.out.println(mtIdentify);
                node2 = (SqlSelect) SqlParser.create(mtIdentify).parseStmt();
                node.setWhere(node2.getWhere());

            } catch (SqlParseException e) {
                e.printStackTrace();
            }
        }
        return node.toSqlString(SqlDialect.CALCITE).toString();
    }

    /**
     * Overwrite SQL query when match with Multi-tenancy Projects
     * Example:
     * select * from Employees --> select name, role_id, reg_id, salary, age from Employees, but not ttid
     *
     * @param
     * @return
     *
     */
    public static String MTProject(SqlSelect node) {
        SqlSelect node2;
        ArrayList<String> sqlList = new ArrayList<String>();
        ArrayList<String> keys = MTSchemaUtil.getMTKeys(MTSchemaUtil.getMTSchema("output/mt.json"));

        if (checkTableMatch(node)) {
            //select list need quoting to be recognize by Calcite
            for(String key: keys) {
                sqlList.add("\"" +key + "\"");
            }
            try {
                node2 = (SqlSelect) SqlParser.create(
                        "select" + String.join(",", sqlList) + "from \"any\""
                ).parseStmt();
                node.setSelectList(node2.getSelectList());
            } catch (SqlParseException e) {
                e.printStackTrace();
            }
        } //else return same sql, no overwrite
        return node.toSqlString(SqlDialect.CALCITE).toString();
    }

    public static boolean checkTableMatch(SqlSelect node) {
        boolean match = false;
        String sqlTable = node.getFrom().toString().split("\\.")[1];
        String jsonTable = MTSchemaUtil.getMTTable(MTSchemaUtil.getMTSchema("output/mt.json"));
        if (sqlTable.equals(jsonTable)) match = true;
        return match;
    }

    public static String MTComparisions(SqlSelect node) {

        String[] whereList = ParserUtil.getWhereCondition(node);
        for(String where: whereList) {
            System.out.println(where.trim());
            //TODO: check where list and get their mt-attribute to identify
            //1. tenant-specific: where role_id > 1
            //2. comparable: where region_id = 2
            //3. transformable: where salary < 100k
            //4. partially-comparable:

        }
        return node.toSqlString(SqlDialect.CALCITE).toString();
    }

    public static String MTJoins() {
        //TODO
        return null;
    }

    public static String MTGroupBy() {
        //TODO
        return null;
    }

    public static String MTOrderBy() {
        //TODO
        return null;
    }

}