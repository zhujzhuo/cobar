package com.alibaba.cobar.server.route.hint;

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alibaba.cobar.server.frontend.ServerConnection;
import com.alibaba.cobar.server.model.Schemas;
import com.alibaba.cobar.server.route.RouteResultset;
import com.alibaba.cobar.server.route.RouteResultsetNode;
import com.alibaba.cobar.server.util.ArrayUtil;
import com.alibaba.cobar.server.util.Pair;

/**
 * @author xianmao.hexm
 */
public class HintRouter {

    private static final Logger LOGGER = Logger.getLogger(HintRouter.class);

    public static int indexOfPrefix(String sql) {
        int i = 0;
        for (; i < sql.length(); ++i) {
            switch (sql.charAt(i)) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                continue;
            }
            break;
        }
        if (sql.startsWith(CobarHint.COBAR_HINT_PREFIX, i)) {
            return i;
        } else {
            return -1;
        }
    }

    public static void routeFromHint(ServerConnection c, Schemas.Schema schema, RouteResultset rrs,
                                     int prefixIndex, final String sql) throws SQLSyntaxErrorException {
        CobarHint hint = CobarHint.parserCobarHint(sql, prefixIndex);
        final String outputSql = hint.getOutputSql();
        final int replica = hint.getReplica();
        final String table = hint.getTable();
        final List<Pair<Integer, Integer>> dataNodes = hint.getDataNodes();
        final Pair<String[], Object[][]> partitionOperand = hint.getPartitionOperand();

        TableConfig tableConfig = null;
        if (table == null || schema.getTables() == null || (tableConfig = schema.getTables().get(table)) == null) {
            // table not indicated
            RouteResultsetNode[] nodes = new RouteResultsetNode[1];
            rrs.setNodes(nodes);
            if (dataNodes != null && !dataNodes.isEmpty()) {
                Integer replicaIndex = dataNodes.get(0).getValue();
                if (replicaIndex != null
                        && RouteResultsetNode.DEFAULT_REPLICA_INDEX.intValue() != replicaIndex.intValue()) {
                    // replica index indicated in dataNodes references
                    nodes[0] = new RouteResultsetNode(schema.getDataNode(), replicaIndex, outputSql);
                    logExplicitReplicaSet(c, sql, rrs);
                    return;
                }
            }
            nodes[0] = new RouteResultsetNode(schema.getDataNode(), replica, outputSql);
            if (replica != RouteResultsetNode.DEFAULT_REPLICA_INDEX) {
                logExplicitReplicaSet(c, sql, rrs);
            }
            return;
        }

        if (dataNodes != null && !dataNodes.isEmpty()) {
            RouteResultsetNode[] nodes = new RouteResultsetNode[dataNodes.size()];
            rrs.setNodes(nodes);
            int i = 0;
            boolean replicaSet = false;
            for (Pair<Integer, Integer> pair : dataNodes) {
                String dataNodeName = tableConfig.getDataNodes()[pair.getKey()];
                Integer replicaIndex = dataNodes.get(i).getValue();
                if (replicaIndex != null
                        && RouteResultsetNode.DEFAULT_REPLICA_INDEX.intValue() != replicaIndex.intValue()) {
                    replicaSet = true;
                    nodes[i] = new RouteResultsetNode(dataNodeName, replicaIndex, outputSql);
                } else {
                    replicaSet = replicaSet || (replica != RouteResultsetNode.DEFAULT_REPLICA_INDEX);
                    nodes[i] = new RouteResultsetNode(dataNodeName, replica, outputSql);
                }
                ++i;
            }
            if (replicaSet) {
                logExplicitReplicaSet(c, sql, rrs);
            }
            return;
        }

        if (partitionOperand == null) {
            String[] tableDataNodes = tableConfig.getDataNodes();
            RouteResultsetNode[] nodes = new RouteResultsetNode[tableDataNodes.length];
            rrs.setNodes(nodes);
            for (int i = 0; i < nodes.length; ++i) {
                nodes[i] = new RouteResultsetNode(tableDataNodes[i], replica, outputSql);
            }
            return;
        }

        String[] cols = partitionOperand.getKey();
        Object[][] vals = partitionOperand.getValue();
        if (cols == null || vals == null) {
            throw new SQLSyntaxErrorException("${partitionOperand} is invalid: " + sql);
        }
        RuleConfig rule = null;
        TableRuleConfig tr = tableConfig.getRule();
        List<RuleConfig> rules = null;// (tr == null) ? null : tr.getRules();
        if (rules != null) {
            for (RuleConfig r : rules) {
                List<String> ruleCols = r.getColumns();
                boolean match = true;
                for (String ruleCol : ruleCols) {
                    match &= ArrayUtil.contains(cols, ruleCol);
                }
                if (match) {
                    rule = r;
                    break;
                }
            }
        }

        String[] tableDataNodes = tableConfig.getDataNodes();
        if (rule == null) {
            RouteResultsetNode[] nodes = new RouteResultsetNode[tableDataNodes.length];
            rrs.setNodes(nodes);
            boolean replicaSet = false;
            for (int i = 0; i < tableDataNodes.length; ++i) {
                replicaSet = replicaSet || (replica != RouteResultsetNode.DEFAULT_REPLICA_INDEX);
                nodes[i] = new RouteResultsetNode(tableDataNodes[i], replica, outputSql);
            }
            if (replicaSet) {
                logExplicitReplicaSet(c, sql, rrs);
            }
            return;
        }

        Set<String> destDataNodes = calcHintDataNodes(rule, cols, vals, tableDataNodes);
        RouteResultsetNode[] nodes = new RouteResultsetNode[destDataNodes.size()];
        rrs.setNodes(nodes);
        int i = 0;
        boolean replicaSet = false;
        for (String dataNode : destDataNodes) {
            replicaSet = replicaSet || (replica != RouteResultsetNode.DEFAULT_REPLICA_INDEX);
            nodes[i++] = new RouteResultsetNode(dataNode, replica, outputSql);
        }
        if (replicaSet) {
            logExplicitReplicaSet(c, sql, rrs);
        }
    }

    private static Set<String> calcHintDataNodes(RuleConfig rule, String[] cols, Object[][] vals, String[] dataNodes) {
        Set<String> destDataNodes = new HashSet<String>(2, 1);
        Map<String, Object> parameter = new HashMap<String, Object>(cols.length, 1);
        for (Object[] val : vals) {
            for (int i = 0; i < cols.length; ++i) {
                parameter.put(cols[i], val[i]);
            }
            Integer[] dataNodeIndexes = calcDataNodeIndexesByFunction(rule.getRuleAlgorithm(), parameter);
            for (Integer index : dataNodeIndexes) {
                destDataNodes.add(dataNodes[index]);
            }
        }
        return destDataNodes;
    }

    private static void logExplicitReplicaSet(Object frontConn, String sql, RouteResultset rrs) {
        if (frontConn != null && LOGGER.isInfoEnabled()) {
            StringBuilder s = new StringBuilder();
            s.append(frontConn).append("Explicit data node replica set from, sql=[");
            s.append(sql).append(']');
            LOGGER.info(s.toString());
        }
    }

    private static Integer[] calcDataNodeIndexesByFunction(RuleAlgorithm algorithm, Map<String, Object> parameter) {
        Integer[] dataNodeIndexes;
        Object calRst = algorithm.calculate(parameter);
        if (calRst instanceof Number) {
            dataNodeIndexes = new Integer[1];
            dataNodeIndexes[0] = ((Number) calRst).intValue();
        } else if (calRst instanceof Integer[]) {
            dataNodeIndexes = (Integer[]) calRst;
        } else if (calRst instanceof int[]) {
            int[] intArray = (int[]) calRst;
            dataNodeIndexes = new Integer[intArray.length];
            for (int i = 0; i < intArray.length; ++i) {
                dataNodeIndexes[i] = intArray[i];
            }
        } else {
            throw new IllegalArgumentException("route err: result of route function is wrong type or null: " + calRst);
        }
        return dataNodeIndexes;
    }

}
