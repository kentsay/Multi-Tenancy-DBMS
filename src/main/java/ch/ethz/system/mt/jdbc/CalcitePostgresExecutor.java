package ch.ethz.system.mt.jdbc;

import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by kentsay on 7/7/15.
 */
public class CalcitePostgresExecutor extends JdbcQueryExecutor {

    final Logger logger = LoggerFactory.getLogger(CalcitePostgresExecutor.class);
    String schemaName;

    public CalcitePostgresExecutor(BasicDataSource dataSource) {
        try {
            this.schemaName = dataSource.getDefaultCatalog();
            // creates a calcite driver so queries go through it
            Class.forName("org.apache.calcite.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:calcite:");
            this.calciteConnection = connection.unwrap(CalciteConnection.class);

            // creating mysql connection
            Class.forName("org.postgresql.Driver");
            this.jdbcSchema = JdbcSchema.create(calciteConnection.getRootSchema(), schemaName, dataSource, null, schemaName);
            // adding schema to connection
            this.calciteConnection.getRootSchema().add(schemaName, jdbcSchema);
            logger.debug("Connection build");

        } catch(ClassNotFoundException e) {
            System.err.println("Caught ClassNotFoundException: " +  e.getMessage());
        } catch(SQLException e) {
            System.err.println("Caught SQLException: " +  e.getMessage());
        }
    }
}