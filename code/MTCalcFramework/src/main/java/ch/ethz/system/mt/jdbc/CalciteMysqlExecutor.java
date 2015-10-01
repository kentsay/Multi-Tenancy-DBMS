package ch.ethz.system.mt.jdbc;

import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by kentsay on 7/7/15.
 */
public class CalciteMysqlExecutor extends JdbcQueryExecutor {

    final Logger logger = LoggerFactory.getLogger(CalciteMysqlExecutor.class);
    String schemaName;
    
    public CalciteMysqlExecutor(BasicDataSource dataSource) {
    	this(dataSource, null);
    }

    public CalciteMysqlExecutor(BasicDataSource dataSource, Properties properties) {
        try {
            // creates a calcite driver so queries go through it
            Class.forName("org.apache.calcite.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:calcite:", properties);
            this.calciteConnection = connection.unwrap(CalciteConnection.class);
            
            Properties props = this.calciteConnection.getProperties();
            //test code for retrieval properties from calcite connection
//            System.out.println(props == null ? "no properties file found" : String.valueOf(props.size()));
//            System.out.println(props.getProperty("ttid"));
//            System.out.println(props.getProperty("dataset_ttids"));

            // creating mysql connection
            this.schemaName = dataSource.getDefaultCatalog();
            Class.forName("com.mysql.jdbc.Driver");
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
