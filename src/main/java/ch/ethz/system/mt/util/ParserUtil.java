package ch.ethz.system.mt.util;

import ch.ethz.system.mt.rules.QueryRewrite;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtil {

    static Quoting quoting = Quoting.DOUBLE_QUOTE;
    static Casing unquotedCasing = Casing.TO_UPPER;
    static Casing quotedCasing = Casing.UNCHANGED;

    public static String getMTAnnotation(String str) {
        String annotation = "";
        Pattern pattern = Pattern.compile("/\\*@(.*?)\\*/");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            annotation = matcher.group(1);
        }
        return annotation;
    }

    public static String[] getWhereCondition(SqlSelect node) {
        if (node.hasWhere()) {
            return node.getWhere().toString().split("AND");
        } else
            return null;
    }

    public static SqlParser getSqlParser (String sql) {
        return SqlParser.create(sql,
                SqlParser.configBuilder()
                        .setQuoting(quoting)
                        .setUnquotedCasing(unquotedCasing)
                        .setQuotedCasing(quotedCasing)
                        .build());
    }

    public static String queryMatch (SqlNode node, MTProperties metadata) {
        String result = "";
        switch (node.getKind()) {
            case SELECT:
                //TODO: Efficient way to distinguish sql operation.
                //Because everything that we want to rewrite looks like a select statement
                SqlSelect select = (SqlSelect) node;
                result = QueryRewrite.MTScan(select, metadata.getDataSet()); //MTScan
                if (select.getSelectList().toString().equals("*"))
                    result = QueryRewrite.MTProject(select); //MTProject
                if (select.hasWhere()) {
                    result = QueryRewrite.MTComparisions(select); //MTComparisions
                }
                break;
            default:
                System.out.println("SQL Syntax Error");
                break;
        }
        return result;
    }
}
