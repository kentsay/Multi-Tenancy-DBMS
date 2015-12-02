package ch.ethz.system.mt.example;

import ch.ethz.system.mt.rules.RelNodeRewrite;
import ch.ethz.system.mt.util.MTProperties;
import ch.ethz.system.mt.util.RelUtil;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.config.Lex;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Programs;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by braunl on 25.09.15.
 */
public class MTExample {

    static {
        // set up data source
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:mysql://localhost");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDefaultCatalog("research");
        dataSource.setMaxActive(10);

        // set up root schema and general configurations for Rel Conversion
        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        final FrameworkConfig config =
                Frameworks.newConfigBuilder()
                        //Lexical policy setting for mysql: this allow us avoid the quoting problem in test string
                        .parserConfig(SqlParser.configBuilder().setLex(Lex.MYSQL).build())
                        .defaultSchema(rootSchema.add("JDBC_MYSQL",
                                JdbcSchema.create(rootSchema, "JDBC_MYSQL_sub", dataSource, null, null)))
                        .traitDefs((List<RelTraitDef>) null)
                        .programs(Programs.ofRules(Programs.RULE_SET))
                        .build();
        RelUtil.setFrameworkConfig(config);
    }

    private static void executeSession(MTProperties properties, Collection<String> queries)
    {
        System.out.println("### Starting a new session with client-id "
                + properties.getClientTenandID()
                + " and dataset {" + StringUtils.join(properties.getDataSet(), ",")
                + "} ###");

        for (String query : queries) {
            System.out.println("Original Query: " + query);
            try {
                RelRoot root = RelUtil.convertSqlToRel(query);
                System.out.println("Original plan of the query:\n" + RelOptUtil.toString(root.rel));
                RelNode rel =  RelNodeRewrite.rewriteRelRoot(root, properties);
                System.out.println("Plan of Rewritten query:\n" + RelOptUtil.toString(rel));
                ResultSet result = RelUtil.executeRel(rel);
                System.out.println("Result:");
                RelUtil.prettyPrintResult(result, System.out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws Exception {

        // create test queries
        List<String> queries = new ArrayList<>(4);
        queries.add("SELECT * FROM Employees");                          // test projection and scan
        queries.add("SELECT employee_name, age FROM Employees WHERE age > 40");   // test projection, comparable filter and scan
        queries.add("SELECT employee_name, age FROM Employees WHERE salary > 100 and age > 40"); // test projection, transformable filter, and scan

        // create test mt-properties (client session configurations)
        List<MTProperties> sessions = new ArrayList<>(3);
        sessions.add(new MTProperties(0, 0));                  // tenant ETH, interested in {ETH}
        sessions.add(new MTProperties(1, 1));                  // tenant MSR, interested in {MSR}
        sessions.add(new MTProperties(0, 0, 1));               // tenant ETH, interested in {ETH, MSR}

        // run the different sessions with the different queries
        for (MTProperties session: sessions) {
            executeSession(session, queries);
        }
    }

}
