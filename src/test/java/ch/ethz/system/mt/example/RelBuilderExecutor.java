package ch.ethz.system.mt.example;


import ch.ethz.system.mt.rules.RelNodeRewrite;
import ch.ethz.system.mt.util.RelUtil;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.config.Lex;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.*;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RelBuilderExecutor {
    public static void main(String[] args) throws Exception {

        SchemaPlus rootSchema = Frameworks.createRootSchema(true);

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:mysql://localhost");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDefaultCatalog("research");

        final List<RelTraitDef> traitDefs = new ArrayList<>();
        traitDefs.add(ConventionTraitDef.INSTANCE);
        traitDefs.add(RelCollationTraitDef.INSTANCE);

        final FrameworkConfig config =
                Frameworks.newConfigBuilder()
                        //Lexical policy setting for mysql: this allow us avoid the quoting problem in test string
                        .parserConfig(SqlParser.configBuilder().setLex(Lex.MYSQL).build())
                        .defaultSchema(rootSchema.add("JDBC_MYSQL",
                                JdbcSchema.create(rootSchema, "JDBC_MYSQL_sub", dataSource, null, null)))
                        //.traitDefs(traitDefs)
                        .traitDefs((List<RelTraitDef>) null)
                        //.programs(Programs.heuristicJoinOrder(Programs.RULE_SET, true, 2))
                        .programs(Programs.ofRules(Programs.RULE_SET))
                        .build();

        RelUtil.setFrameworkConfig(config);
        final RelBuilder builder = RelUtil.getBuilder();

        // Testing 1: build relation algebra from scratch
        builder.scan("Employees")
//                .filter(builder.equals(builder.field("role_id"), builder.literal(1)))
                .filter(builder.call(SqlStdOperatorTable.IN, builder.field("ttid"), builder.literal(1)));
//        List<RexNode> projectFields = new ArrayList<>(2);
//        for (int i = 1; i < builder.fields().size(); ++i)
//            projectFields.add(builder.field(i));
//        builder.project(projectFields);
        RelNode node = builder.build();

        // Testing 2: convert a exist sql string to relation algebra with MT Scan
        //RelNode node2 = RelNodeRewrite.MTScan("select * from Employees where role_id = 1 and salary > 1000", "0,1");
        // Testing 3: convert a exist sql string to relation algebra with MT Project
        //RelNode node3 = RelNodeRewrite.MTProject("select * from Employees");

        //TODO: break a tree into node and replace it with our operators
        /* This doesn't works since Calcite throws - Different Planner error*/
        /* RelNode is a immutable structure so it cannot be replace*/
//        RelNode newNode = builder.scan("Employees").build();
//        node2.replaceInput(0, newNode);
//        node2.register(node2.getCluster().getPlanner());

        System.out.println("==========================");
        System.out.println(RelOptUtil.toString(node));
        System.out.println("==========================");
//        System.out.println(RelOptUtil.toString(node2));
        System.out.println("==========================");
//        System.out.println(RelOptUtil.toString(node3));


        ResultSet result = RelUtil.executeRel(node);
        RelUtil.prettyPrintResult(result, System.out);
    }
}
