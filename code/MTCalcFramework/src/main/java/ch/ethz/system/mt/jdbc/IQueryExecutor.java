package ch.ethz.system.mt.jdbc;

import java.sql.ResultSet;

/**
 * Created by kentsay on 7/7/15.
 */
public interface IQueryExecutor {

    /**
     * Executes a SQL query.
     *
     * @param sql
     *          SQL query in string.
     * @return JDBC result set.
     */
    public ResultSet execute(String sql);
    /**
     * Closed the connection and statement used for executing query.
     */
    public void close();

}
