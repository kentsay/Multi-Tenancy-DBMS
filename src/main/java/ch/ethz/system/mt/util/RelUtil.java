package ch.ethz.system.mt.util;

import org.apache.calcite.adapter.jdbc.JdbcTableScan;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.tools.*;

import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by kentsay on 9/21/15.
 */
public class RelUtil {

    static FrameworkConfig frameworkConfig;
    static RelBuilder builder;

    public static FrameworkConfig getFrameworkConfig() {
        return frameworkConfig;
    }

    public static void setFrameworkConfig(FrameworkConfig frameworkConfig) {
        RelUtil.frameworkConfig = frameworkConfig;
    }

    public static RelBuilder getBuilder() {
        //always return a new Builder when you call getBuilder, insure something else is still in the stack and crash the tree
        builder = RelBuilder.create(getFrameworkConfig());
        return builder;
    }

    public static RelRoot convertSqlToRel(String sql) throws SqlParseException, ValidationException, RelConversionException {
        Planner planner = Frameworks.getPlanner(getFrameworkConfig());
        SqlNode parsed = planner.parse(sql);
        SqlNode validated = planner.validate(parsed);
        RelRoot root = planner.rel(validated);
        return root;
    }

    public static ResultSet executeRel(RelNode rel) throws SQLException {
        PreparedStatement pd = RelRunners.run(rel);
        return pd.executeQuery();
    }

    public static void prettyPrintResult (ResultSet resultSet, PrintStream out) throws SQLException {
        final StringBuilder buf = new StringBuilder();
        final ResultSetMetaData metaData = resultSet.getMetaData();
        while (resultSet.next()) {
            rowToString(resultSet, buf, metaData).append("\n");
        }
        out.println(buf.toString());
        resultSet.close();
    }

    private static StringBuilder rowToString(ResultSet resultSet, StringBuilder buf,
                                     ResultSetMetaData metaData) throws SQLException {
        int n = metaData.getColumnCount();
        if (n > 0) {
            for (int i = 1;; i++) {
                buf.append(metaData.getColumnLabel(i))
                        .append("=")
                        .append(resultSet.getString(i));
                if (i == n) {
                    break;
                }
                buf.append("; ");
            }
        }
        return buf;
    }
}
