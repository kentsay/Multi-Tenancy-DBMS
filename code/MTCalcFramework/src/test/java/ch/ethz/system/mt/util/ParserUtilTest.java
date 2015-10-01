package ch.ethz.system.mt.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;


/**
 * Created by kentsay on 7/9/15.
 */
public class ParserUtilTest {
    CCJSqlParserManager parserManager = new CCJSqlParserManager();
    String statement;

    @Before
    public void init() {
        statement = "CREATE TABLE Employees /*@specific*/(\n" +
                "\tttid int /*@comparable*/,\n" +
                "\temployee_name varchar(50) /*@comparable*/,\n" +
                "\trole_id int /*@specific*/,\n" +
                "\tregion_id int /*@*/,\n" +
                "\tsalary int /*@transformable@f*/,\n" +
                "\tage int /*@comparable*/,\n" +
                "\tPRIMARY KEY (ttid, employee_name),\n" +
                "\tFOREIGN KEY (ttid) REFERENCES Tenants (ttid),\n" +
                "\tFOREIGN KEY (ttid, role_id) REFERENCES Roles (ttid, role_id),\n" +
                "\tFOREIGN KEY (region_id) REFERENCES Regions (region_id)\n" +
                ");";
    }

    @Test
    public void parseTest() throws JSQLParserException {
        CreateTable createTable = (CreateTable) parserManager.parse(new StringReader(statement));
        System.out.println(createTable.getColumnDefinitions().size());
        System.out.println(createTable.getColumnDefinitions().get(0).getColumnName());
        System.out.println(createTable.getColumnDefinitions().get(0).getColDataType());
        System.out.println(createTable.getColumnDefinitions().get(0).toString());
    }

    @Test
    public void annotationTest() {
        String testComparable = "\tttid int /*@comparable*/,\n";
        String testSpecific = "role_id int /*@specific*/,\n";
        String testTransformable = "\tsalary int /*@transformable@f*/,\n";

        assertEquals("comparable", ParserUtil.getMTAnnotation(testComparable));
        assertEquals("specific", ParserUtil.getMTAnnotation(testSpecific));
        assertEquals("transformable@f", ParserUtil.getMTAnnotation(testTransformable));
    }


}
