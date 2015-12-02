package ch.ethz.system.mt.rules;

import ch.ethz.system.mt.util.MTProperties;
import ch.ethz.system.mt.util.MTSchema;
import ch.ethz.system.mt.util.MTSchemaUtil;
import ch.ethz.system.mt.util.RelUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.core.Filter;
import org.apache.calcite.rel.core.Project;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rex.*;
import org.apache.calcite.tools.RelBuilder;
import org.apache.calcite.util.Util;
import org.apache.calcite.util.trace.CalciteLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by kentsay on 9/23/15.
 */
public class RelNodeRewrite {

    protected static final CalciteLogger logger =
            new CalciteLogger(
                    Logger.getLogger(RelNodeRewrite.class.getName()));

    //TODO: add schema as a an additional parameter as soon as we have something like a schema class...
    public static RelNode rewriteRelRoot(RelRoot root, MTProperties properties)
    {
        RelBuilder builder = RelUtil.getBuilder();
        traverseAndBuild(root.rel, builder, properties);
        return builder.build();
    }

    //TODO: possibly add schema class here too...
    private static void traverseAndBuild(RelNode node, RelBuilder builder, MTProperties properties)
    {

        if (node instanceof Project) {
            assert (node.getInputs().size() == 1);
            traverseAndBuild(node.getInput(0), builder, properties);
            mtProject((Project) node, builder);
        } else if (node instanceof Filter) {
            assert (node.getInputs().size() == 1);
            traverseAndBuild(node.getInput(0), builder, properties);
            mtFilter((Filter) node, builder, properties);
        } else if (node instanceof TableScan) {
            assert (node.getInputs().size() == 0);
            mtScan((TableScan) node, builder, properties);
        } else {
            throw new RuntimeException("Sql-Operator " + node.getClass() + "not supported yet!");
        }
    }

    /**
     * Overwrite SQL query when match with Multi-tenancy Projects
     * Example:
     * select * from Employees --> select name, role_id, reg_id, salary, age from Employees, but not ttid
     *
     * @param
     * @return
     *
     */
    private static void mtProject(Project node, RelBuilder builder) {
        builder.project(
                node.getNamedProjects().stream()
                        .filter(p -> !(p.getValue().equals("ttid")))
                        .map(p -> p.getKey())
                   .collect(Collectors.toList())
        );
        // TODO: add multi-tenancy relevant stuff
    }

    private static void mtFilter(Filter node, RelBuilder builder, MTProperties properties) {
        assert (node.getInputs().size() == 1);
        RexCall conditionNode = (RexCall)node.getCondition();
        List<RexNode> listRexNode = new ArrayList<>();
        // check if there is more then one condition in the filter node
        if (conditionNode.getOperands().size() > 1 && !(conditionNode.getOperands().get(0) instanceof RexInputRef)) {
            // more the one condition
            for (RexNode set: conditionNode.getOperands()) {
                RexCall tmp = (RexCall)set;
                List<RexNode> cons = mtFilterRewrite(builder, node, tmp, properties);
                listRexNode.add(builder.call(tmp.getOperator(), cons));
            }
            builder.filter(listRexNode);
        } else {
            // only one condition
            List<RexNode> cons = mtFilterRewrite(builder, node, conditionNode, properties);
            builder.filter(
                    builder.call(conditionNode.getOperator(), cons)
            );
        }
    }

    private static List<RexNode> mtFilterRewrite(RelBuilder builder, Filter node, RexCall conditionNode, MTProperties properties) {
        //extract the attribute name and constance value from condition node operands
        RexVariable tmp = (RexVariable)conditionNode.getOperands().get(0);
        RexLiteral cons = (RexLiteral)conditionNode.getOperands() .get(1);
        String attribute = node.getRowType().getFieldNames().get(tmp.hashCode());

        logger.info("att: " + attribute + ", operator: " + conditionNode.getOperator().getKind() + ", value:" + cons.getValue2());

        //get the type of the attributes
        MTSchema.Attributes attValue = MTSchemaUtil.getMTAttribute(MTSchemaUtil.getMTSchema("output/mt.json"), attribute);

        /* Assume we have our transformable function as follow:
           f(ttid, salary) --> ttid = 0, salary = salary
                           --> ttid = 1, salary = salary*1.5

            Example
            for transformable, apply a function to the attributes and constance value
                if (attributes is transformable && transformable function exists) {
                    condition operands --> f(ttid, attributes)
                }
         */
        List<RexNode> modifyRexNode = new ArrayList<>();
        modifyRexNode.add(tmp);
        switch (attValue) {
            case specific:
                logger.info("### attribute is specific");
                modifyRexNode.add(cons);
                break;
            case comparable:
                logger.info("### attribute is comparable");
                modifyRexNode.add(cons);
                break;
            case transformable:
                logger.info("### attribute is transformable");
                Long value = (Long)cons.getValue2();
                Double transformValue;
                if (properties.getClientTenandID() == 0) {
                    transformValue = Double.valueOf(value.intValue());
                } else {
                    transformValue = Double.valueOf(value.intValue()*0.9);
                }
                RexNode mod = builder.literal(transformValue);
                modifyRexNode.add(mod);
                break;
        }
        return modifyRexNode;
    }

    /**
     * Overwrite SQL query when match with Multi-tenancy Scan
     * Example:
     * select * from Employees --> select * from Employees where ttid in (0,1)
     *
     * @param
     * @return String
     */
    private static void mtScan(TableScan node, RelBuilder builder, MTProperties properties) {
        String tableName = Util.last(node.getTable().getQualifiedName());
        builder.scan(tableName);
        // We use OR operator because Calcite does not support IN operator in RelBuilder (confirmed by Julian https://goo.gl/8qWRL8)
        List<RexNode> listRexNode = new ArrayList<>();
        int size = properties.getDataSet().size();
        if (size > 1) {
            for (int i = 0; i < size; i++) {
                listRexNode.add(builder.equals(builder.field("ttid"), builder.literal(properties.getDataSet().get(i))));
            }
            builder.filter(builder.or(listRexNode));
        } else {
            builder.filter(builder.equals(builder.field("ttid"), builder.literal(properties.getDataSet().get(0))));
        }
    }
}