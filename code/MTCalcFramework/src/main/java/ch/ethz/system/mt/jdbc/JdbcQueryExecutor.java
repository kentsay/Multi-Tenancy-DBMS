package ch.ethz.system.mt.jdbc;

import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by kentsay on 7/7/15.
 */
public class JdbcQueryExecutor implements IQueryExecutor {

    final Logger logger = LoggerFactory.getLogger(JdbcQueryExecutor.class);
    protected CalciteConnection calciteConnection;
    protected Connection connection;
    protected JdbcSchema jdbcSchema;
    public Statement statement;

    @Override
    public ResultSet execute(String sql) {
        ResultSet results = null;
        try {
            logger.debug("Creating a statement");
            statement = calciteConnection.createStatement();
            logger.debug("Going to execute query: " + sql);
            results = statement.executeQuery(sql);
            logger.debug("Execution complete");
        } catch (SQLException e) {
            logger.error("Could not create a statement.  " + e);
        }
        return results;
    }

    @Override
    public void close() {
        if (calciteConnection != null) {
            try {
                calciteConnection.close();
            } catch (SQLException e) {
                logger.error("Could not close Calcite connection");
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error("Could not close Calcite statement");
                }
            }
        }
    }
}
