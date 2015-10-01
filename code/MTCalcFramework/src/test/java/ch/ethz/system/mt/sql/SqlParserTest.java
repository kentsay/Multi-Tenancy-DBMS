package ch.ethz.system.mt.sql;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.junit.Test;

/**
 * Created by kentsay on 7/13/15.
 */
public class SqlParserTest {

    private SqlParser getSqlParser(String str) {
        return SqlParser.create(str);
    }

    @Test
    public void testSqlParser() throws SqlParseException {
        String statement = "SELECT salary, age, employee_name FROM Employees";
        SqlParser parser = getSqlParser(statement);

        SqlNode node = parser.parseQuery();
        System.out.println(node.getParserPosition().getColumnNum());

        System.out.println(node.getKind());
        System.out.println(node.toSqlString(null, true).getSql());

    }

    @Test
    public void testSqlWriter() {

    }
}
