package ch.ethz.system.mt.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.SelectUtils;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

/**
 * Created by kentsay on 9/9/15.
 */
public class JSQLParserTest {

    @Test
    public void testAddColumn() throws JSQLParserException {
        Select select = (Select) CCJSqlParserUtil.parse("select a from mytable where a < 10 and b>1");
        SelectUtils.addExpression(select, new Column("b"));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        System.out.println(plainSelect.getWhere());
        Select select2= (Select) CCJSqlParserUtil.parse("select * from test where a < 10 and b > 5 and c >1");

        PlainSelect test = (PlainSelect) select2.getSelectBody();
        plainSelect.setWhere(test.getWhere());
        System.out.println(plainSelect.getWhere());
        System.out.println(plainSelect);
    }
}
