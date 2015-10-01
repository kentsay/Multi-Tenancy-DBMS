package ch.ethz.system.mt.jdbc;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by kentsay on 7/7/15.
 */
public class CalcitePostgresExecutorTest {
    final Logger logger = LoggerFactory.getLogger(CalcitePostgresExecutorTest.class);

    @Test
    public void testSimplyQuery() throws SQLException {
        logger.info("Init Calcite connection with PostgreSQL JDBC");
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:postgresql://127.0.0.1/demo");
        dataSource.setUsername("kentsay");
        dataSource.setPassword("kentsay");
        dataSource.setDefaultCatalog("demo");
        CalcitePostgresExecutor sql = new CalcitePostgresExecutor(dataSource);

        ResultSet resultSet =
                sql.execute("select *\n" + "from \"demo\".\"tenants\"");
        final StringBuilder buf = new StringBuilder();
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
        resultSet.close();
        sql.close();
    }
}