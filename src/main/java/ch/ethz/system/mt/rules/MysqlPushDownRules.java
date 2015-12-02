package ch.ethz.system.mt.rules;

import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelOptRuleOperand;
import org.apache.calcite.plan.RelOptRuleOperandChildren;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kentsay on 7/7/15.
 */
public class MysqlPushDownRules extends RelOptRule {

    final Logger logger = LoggerFactory.getLogger(MysqlPushDownRules.class);

    public MysqlPushDownRules(RelOptRuleOperand operand, String desc) {
        super(operand, "MTPushDownRule: " + desc);
    }

    public MysqlPushDownRules (RelOptRuleOperand operand) {
        super(operand);
    }

    //Define our MT rules at here to do query rewrite and sql optimization



    @Override
    public void onMatch(RelOptRuleCall relOptRuleCall) {
        logger.info("Rule Match for: " + description);
    }
}
