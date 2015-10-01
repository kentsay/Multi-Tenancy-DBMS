package ch.ethz.system.mt.jdbc;

import ch.ethz.system.mt.util.MTProperties;
import ch.ethz.system.mt.util.ParserUtil;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by kentsay on 7/7/15.
 */
public class CalciteMysqlExecutorTest {
    static final Logger logger = LoggerFactory.getLogger(CalciteMysqlExecutor.class);

    static MTProperties mtProperties;
    static CalciteMysqlExecutor sql;

    @BeforeClass
    public static void initialize() {
        logger.info("Init Calcite connection with MySQL JDBC");

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:mysql://localhost");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDefaultCatalog("research");

        mtProperties = new MTProperties();
        mtProperties.setClientTenantID(1);
        mtProperties.setDataSet(0,1);
        sql = new CalciteMysqlExecutor(dataSource, mtProperties);
    }

    @Test
    public void testMTScan_MTProject() throws SQLException, SqlParseException {
        String SQL_MTSCAN = "select * from \"research\".\"Employees\"";
        String sqlString = ParserUtil.queryMatch(ParserUtil.getSqlParser(SQL_MTSCAN).parseStmt(), mtProperties);
        logger.info("### After SQL Overwrite:\n" + sqlString);

        ResultSet resultSet = sql.execute(sqlString);
        final StringBuilder buf = new StringBuilder();
        if (null != resultSet) {
            while (resultSet.next()) {
                int n = resultSet.getMetaData().getColumnCount();
                for (int i = 1; i <= n; i++) {
                    buf.append(i > 1 ? "; " : "")
                            .append(resultSet.getMetaData().getColumnLabel(i))
                            .append("=")
                            .append(resultSet.getObject(i));
                }
                System.out.println(buf.toString());
                buf.setLength(0);
            }
        }
        resultSet.close();
        sql.close();
    }

    @Test
    public void testMTComparision_Specific() throws SqlParseException, SQLException {
        String SQL_MTCompare_Specific = "select * from \"research\".\"Roles\" where \"role_id\" > 1";
        String sqlString = ParserUtil.queryMatch(ParserUtil.getSqlParser(SQL_MTCompare_Specific).parseStmt(),mtProperties);
        logger.info("### After SQL Overwrite: " + sqlString);

        ResultSet resultSet = sql.execute(sqlString);
        final StringBuilder buf = new StringBuilder();
        if (null != resultSet) {
            while (resultSet.next()) {
                int n = resultSet.getMetaData().getColumnCount();
                for (int i = 1; i <= n; i++) {
                    buf.append(i > 1 ? "; " : "")
                            .append(resultSet.getMetaData().getColumnLabel(i))
                            .append("=")
                            .append(resultSet.getObject(i));
                }
                System.out.println(buf.toString());
                buf.setLength(0);
            }
        }
        resultSet.close();
    }

    @AfterClass
    public static void cleanUp() {
        logger.info("Calcite connection close");
        sql.close();
    }
}
//end of CalciteMysqlExecutorTest