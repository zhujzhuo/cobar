/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cobar.route;

import java.sql.SQLNonTransientException;

import com.alibaba.cobar.frontend.server.ServerConnection;
import com.alibaba.cobar.model.Schemas;
import com.alibaba.cobar.parser.SQLParser;
import com.alibaba.cobar.parser.ast.statement.SQLStatement;
import com.alibaba.cobar.parser.visitor.MySQLOutputASTVisitor;

/**
 * @author xianmao.hexm
 */
public final class ServerRouter {

    public static RouteResultset route(Schemas.Schema schema, String query, ServerConnection c)
            throws SQLNonTransientException {
        RouteResultset rrs = new RouteResultset(query);
        SQLStatement ast = SQLParser.parse(query, c.getCharset());

        // 检查是否分库
        if (!schema.isSharding()) {
            String genSQL = buildSQL(ast);
            String[] dataNodes = schema.getDataNodes();
            RouteResultsetNode[] nodes = new RouteResultsetNode[dataNodes.length];
            for (int i = 0; i < nodes.length; i++) {
                nodes[i] = new RouteResultsetNode(dataNodes[i], genSQL);
            }
            rrs.setNodes(nodes);
            return rrs;
        }

        //        // 展开AST
        //        PartitionKeyVisitor visitor = new PartitionKeyVisitor(schema.getTables());
        //        visitor.setTrimSchema(schema.isKeepSqlSchema() ? schema.getName() : null);
        //        ast.accept(visitor);
        //
        //        // 如果sql包含用户自定义的schema，则路由到default节点
        //        if (schema.isKeepSqlSchema() && visitor.isCustomedSchema()) {
        //            if (visitor.isSchemaTrimmed()) {
        //                query = buildSQL(ast);
        //            }
        //            RouteResultsetNode[] nodes = new RouteResultsetNode[1];
        //            nodes[0] = new RouteResultsetNode(schema.getDataNode(), query);
        //            rrs.setNodes(nodes);
        //            return rrs;
        //        }
        //
        //        // 元数据语句路由
        //        if (visitor.isTableMetaRead()) {
        //            MetaRouter.routeForTableMeta(rrs, schema, ast, visitor, query);
        //            if (visitor.isNeedRewriteField()) {
        //                rrs.setFlag(RouteResultset.REWRITE_FIELD);
        //            }
        //            return rrs;
        //        }
        //
        //        // 匹配规则
        //        TableConfig matchedTable = null;
        //        RuleConfig rule = null;
        //        Map<String, List<Object>> columnValues = null;
        //        Map<String, Map<String, List<Object>>> astExt = visitor.getColumnValue();
        //        Map<String, TableConfig> tables = schema.getTables();
        //        ft: for (Entry<String, Map<String, List<Object>>> e : astExt.entrySet()) {
        //            Map<String, List<Object>> col2Val = e.getValue();
        //            TableConfig tc = tables.get(e.getKey());
        //            if (tc == null) {
        //                continue;
        //            }
        //            if (matchedTable == null) {
        //                matchedTable = tc;
        //            }
        //            if (col2Val == null || col2Val.isEmpty()) {
        //                continue;
        //            }
        //            TableRuleConfig tr = tc.getRule();
        //            if (tr != null) {
        //                //                for (RuleConfig rc : tr.getRules()) {
        //                //                    boolean match = true;
        //                //                    for (String ruleColumn : rc.getColumns()) {
        //                //                        match &= col2Val.containsKey(ruleColumn);
        //                //                    }
        //                //                    if (match) {
        //                //                        columnValues = col2Val;
        //                //                        rule = rc;
        //                //                        matchedTable = tc;
        //                //                        break ft;
        //                //                    }
        //                //                }
        //            }
        //        }
        //
        //        // 规则匹配处理，表级别和列级别。
        //        if (matchedTable == null) {
        //            String sql = visitor.isSchemaTrimmed() ? buildSQL(ast) : query;
        //            RouteResultsetNode[] rn = new RouteResultsetNode[1];
        //            if ("".equals(schema.getDataNode()) && isSystemReadSQL(ast)) {
        //                rn[0] = new RouteResultsetNode(schema.getRandomDataNode(), sql);
        //            } else {
        //                rn[0] = new RouteResultsetNode(schema.getDataNode(), sql);
        //            }
        //            rrs.setNodes(rn);
        //            return rrs;
        //        }
        //        if (rule == null) {
        //            if (matchedTable.isRuleRequired()) {
        //                throw new IllegalArgumentException("route rule for table " + matchedTable.getName() + " is required: "
        //                        + query);
        //            }
        //            String[] dataNodes = matchedTable.getDataNodes();
        //            String sql = visitor.isSchemaTrimmed() ? buildSQL(ast) : query;
        //            RouteResultsetNode[] rn = new RouteResultsetNode[dataNodes.length];
        //            for (int i = 0; i < dataNodes.length; ++i) {
        //                rn[i] = new RouteResultsetNode(dataNodes[i], sql);
        //            }
        //            rrs.setNodes(rn);
        //            setGroupFlagAndLimit(rrs, visitor);
        //            return rrs;
        //        }
        //
        //        // 规则计算
        //        validateAST(ast, matchedTable, rule, visitor);
        //        Map<Integer, List<Object[]>> dnMap = ruleCalculate(matchedTable, rule, columnValues);
        //        if (dnMap == null || dnMap.isEmpty()) {
        //            throw new IllegalArgumentException("No target dataNode for rule " + rule);
        //        }
        //
        //        // 判断路由结果是单库还是多库
        //        if (dnMap.size() == 1) {
        //            String dataNode = matchedTable.getDataNodes()[dnMap.keySet().iterator().next()];
        //            String sql = visitor.isSchemaTrimmed() ? buildSQL(ast) : query;
        //            RouteResultsetNode[] rn = new RouteResultsetNode[1];
        //            rn[0] = new RouteResultsetNode(dataNode, sql);
        //            rrs.setNodes(rn);
        //        } else {
        //            RouteResultsetNode[] rn = new RouteResultsetNode[dnMap.size()];
        //            if (ast instanceof DMLInsertReplaceStatement) {
        //                DMLInsertReplaceStatement ir = (DMLInsertReplaceStatement) ast;
        //                dispatchInsertReplace(rn, ir, rule.getColumns(), dnMap, matchedTable, query, visitor);
        //            } else {
        //                dispatchWhereBasedStmt(rn, ast, rule.getColumns(), dnMap, matchedTable, query, visitor);
        //            }
        //            rrs.setNodes(rn);
        //            setGroupFlagAndLimit(rrs, visitor);
        //        }

        return rrs;
    }

    //    private static Integer[] calcDataNodeIndexesByFunction(RuleAlgorithm algorithm, Map<String, Object> parameter) {
    //        Integer[] dataNodeIndexes;
    //        Object calRst = algorithm.calculate(parameter);
    //        if (calRst instanceof Number) {
    //            dataNodeIndexes = new Integer[1];
    //            dataNodeIndexes[0] = ((Number) calRst).intValue();
    //        } else if (calRst instanceof Integer[]) {
    //            dataNodeIndexes = (Integer[]) calRst;
    //        } else if (calRst instanceof int[]) {
    //            int[] intArray = (int[]) calRst;
    //            dataNodeIndexes = new Integer[intArray.length];
    //            for (int i = 0; i < intArray.length; ++i) {
    //                dataNodeIndexes[i] = intArray[i];
    //            }
    //        } else {
    //            throw new IllegalArgumentException("route err: result of route function is wrong type or null: " + calRst);
    //        }
    //        return dataNodeIndexes;
    //    }

    //    private static boolean equals(String str1, String str2) {
    //        if (str1 == null) {
    //            return str2 == null;
    //        }
    //        return str1.equals(str2);
    //    }

    //    private static void validateAST(SQLStatement ast, TableConfig tc, RuleConfig rule, PartitionKeyVisitor visitor)
    //            throws SQLNonTransientException {
    //        if (ast instanceof DMLUpdateStatement) {
    //            List<Identifier> columns = null;
    //            List<String> ruleCols = rule.getColumns();
    //            DMLUpdateStatement update = (DMLUpdateStatement) ast;
    //            for (Pair<Identifier, Expression> pair : update.getValues()) {
    //                for (String ruleCol : ruleCols) {
    //                    if (equals(pair.getKey().getIdTextUpUnescape(), ruleCol)) {
    //                        if (columns == null) {
    //                            columns = new ArrayList<Identifier>(ruleCols.size());
    //                        }
    //                        columns.add(pair.getKey());
    //                    }
    //                }
    //            }
    //            if (columns == null) {
    //                return;
    //            }
    //            Map<String, String> alias = visitor.getTableAlias();
    //            for (Identifier column : columns) {
    //                String table = column.getLevelUnescapeUpName(2);
    //                table = alias.get(table);
    //                if (table != null && table.equals(tc.getName())) {
    //                    throw new SQLFeatureNotSupportedException("partition key cannot be changed");
    //                }
    //            }
    //        }
    //    }

    //    private static boolean isSystemReadSQL(SQLStatement ast) {
    //        if (ast instanceof DALShowStatement) {
    //            return true;
    //        }
    //        DMLSelectStatement select = null;
    //        if (ast instanceof DMLSelectStatement) {
    //            select = (DMLSelectStatement) ast;
    //        } else if (ast instanceof DMLSelectUnionStatement) {
    //            DMLSelectUnionStatement union = (DMLSelectUnionStatement) ast;
    //            if (union.getSelectStmtList().size() == 1) {
    //                select = union.getSelectStmtList().get(0);
    //            } else {
    //                return false;
    //            }
    //        } else {
    //            return false;
    //        }
    //        return select.getTables() == null;
    //    }
    //
    //    private static void setGroupFlagAndLimit(RouteResultset rrs, PartitionKeyVisitor visitor) {
    //        rrs.setLimitSize(visitor.getLimitSize());
    //        switch (visitor.getGroupFuncType()) {
    //        case PartitionKeyVisitor.GROUP_SUM:
    //            rrs.setFlag(RouteResultset.SUM_FLAG);
    //            break;
    //        case PartitionKeyVisitor.GROUP_MAX:
    //            rrs.setFlag(RouteResultset.MAX_FLAG);
    //            break;
    //        case PartitionKeyVisitor.GROUP_MIN:
    //            rrs.setFlag(RouteResultset.MIN_FLAG);
    //            break;
    //        }
    //    }

    //    private static Map<Integer, List<Object[]>> ruleCalculate(TableConfig matchedTable, RuleConfig rule,
    //                                                              Map<String, List<Object>> columnValues) {
    //        Map<Integer, List<Object[]>> map = new HashMap<Integer, List<Object[]>>(1, 1);
    //        RuleAlgorithm algorithm = rule.getRuleAlgorithm();
    //        List<String> cols = rule.getColumns();
    //
    //        Map<String, Object> parameter = new HashMap<String, Object>(cols.size(), 1);
    //        ArrayList<Iterator<Object>> colsValIter = new ArrayList<Iterator<Object>>(columnValues.size());
    //        for (String rc : cols) {
    //            List<Object> list = columnValues.get(rc);
    //            if (list == null) {
    //                String msg = "route err: rule column " + rc + " dosn't exist in extract: " + columnValues;
    //                throw new IllegalArgumentException(msg);
    //            }
    //            colsValIter.add(list.iterator());
    //        }
    //
    //        try {
    //            for (Iterator<Object> mainIter = colsValIter.get(0); mainIter.hasNext();) {
    //                Object[] tuple = new Object[cols.size()];
    //                for (int i = 0, len = cols.size(); i < len; ++i) {
    //                    Object value = colsValIter.get(i).next();
    //                    tuple[i] = value;
    //                    parameter.put(cols.get(i), value);
    //                }
    //
    //                Integer[] dataNodeIndexes = calcDataNodeIndexesByFunction(algorithm, parameter);
    //
    //                for (int i = 0; i < dataNodeIndexes.length; ++i) {
    //                    Integer dataNodeIndex = dataNodeIndexes[i];
    //                    List<Object[]> list = map.get(dataNodeIndex);
    //                    if (list == null) {
    //                        list = new LinkedList<Object[]>();
    //                        map.put(dataNodeIndex, list);
    //                    }
    //                    list.add(tuple);
    //                }
    //            }
    //        } catch (NoSuchElementException e) {
    //            String msg = "route err: different rule columns should have same value number:  " + columnValues;
    //            throw new IllegalArgumentException(msg, e);
    //        }
    //
    //        return map;
    //    }

    //    private static void dispatchWhereBasedStmt(RouteResultsetNode[] rn, SQLStatement stmtAST, List<String> ruleColumns,
    //                                               Map<Integer, List<Object[]>> dataNodeMap, TableConfig matchedTable,
    //                                               String originalSQL, PartitionKeyVisitor visitor) {
    //        // [perf tag] 11.617 us: sharding multivalue
    //        if (ruleColumns.size() > 1) {
    //            String sql;
    //            if (visitor.isSchemaTrimmed()) {
    //                sql = buildSQL(stmtAST);
    //            } else {
    //                sql = originalSQL;
    //            }
    //            int i = -1;
    //            for (Integer dataNodeId : dataNodeMap.keySet()) {
    //                String dataNode = matchedTable.getDataNodes()[dataNodeId];
    //                rn[++i] = new RouteResultsetNode(dataNode, sql);
    //            }
    //            return;
    //        }
    //
    //        final String table = matchedTable.getName();
    //        Map<String, Map<Object, Set<Pair<Expression, ASTNode>>>> columnIndex = visitor.getColumnIndex(table);
    //        Map<Object, Set<Pair<Expression, ASTNode>>> valueMap = columnIndex.get(ruleColumns.get(0));
    //        replacePartitionKeyOperand(columnIndex, ruleColumns);
    //
    //        Map<InExpression, Set<Expression>> unreplacedInExpr = new HashMap<InExpression, Set<Expression>>(1, 1);
    //        Set<ReplacableExpression> unreplacedSingleExprs = new HashSet<ReplacableExpression>();
    //        // [perf tag] 12.2755 us: sharding multivalue
    //
    //        int nodeId = -1;
    //        for (Entry<Integer, List<Object[]>> en : dataNodeMap.entrySet()) {
    //            List<Object[]> tuples = en.getValue();
    //
    //            unreplacedSingleExprs.clear();
    //            unreplacedInExpr.clear();
    //            for (Object[] tuple : tuples) {
    //                Object value = tuple[0];
    //                Set<Pair<Expression, ASTNode>> indexedExpressionPair = getExpressionSet(valueMap, value);
    //                for (Pair<Expression, ASTNode> pair : indexedExpressionPair) {
    //                    Expression expr = pair.getKey();
    //                    ASTNode parent = pair.getValue();
    //                    if (PartitionKeyVisitor.isPartitionKeyOperandSingle(expr, parent)) {
    //                        unreplacedSingleExprs.add((ReplacableExpression) expr);
    //                    } else if (PartitionKeyVisitor.isPartitionKeyOperandIn(expr, parent)) {
    //                        Set<Expression> newInSet = unreplacedInExpr.get(parent);
    //                        if (newInSet == null) {
    //                            newInSet = new HashSet<Expression>(indexedExpressionPair.size(), 1);
    //                            unreplacedInExpr.put((InExpression) parent, newInSet);
    //                        }
    //                        newInSet.add(expr);
    //                    }
    //                }
    //            }
    //            // [perf tag] 15.3745 us: sharding multivalue
    //
    //            for (ReplacableExpression expr : unreplacedSingleExprs) {
    //                expr.clearReplaceExpr();
    //            }
    //            for (Entry<InExpression, Set<Expression>> entemp : unreplacedInExpr.entrySet()) {
    //                InExpression in = entemp.getKey();
    //                Set<Expression> set = entemp.getValue();
    //                if (set == null || set.isEmpty()) {
    //                    in.setReplaceExpr(ReplacableExpression.BOOL_FALSE);
    //                } else {
    //                    in.clearReplaceExpr();
    //                    InExpressionList inlist = in.getInExpressionList();
    //                    if (inlist != null)
    //                        inlist.setReplaceExpr(new ArrayList<Expression>(set));
    //                }
    //            }
    //            // [perf tag] 16.506 us: sharding multivalue
    //
    //            String sql = buildSQL(stmtAST);
    //            // [perf tag] 21.3425 us: sharding multivalue
    //
    //            String dataNodeName = matchedTable.getDataNodes()[en.getKey()];
    //            rn[++nodeId] = new RouteResultsetNode(dataNodeName, sql);
    //
    //            for (ReplacableExpression expr : unreplacedSingleExprs) {
    //                expr.setReplaceExpr(ReplacableExpression.BOOL_FALSE);
    //            }
    //            for (InExpression in : unreplacedInExpr.keySet()) {
    //                in.setReplaceExpr(ReplacableExpression.BOOL_FALSE);
    //                InExpressionList list = in.getInExpressionList();
    //                if (list != null)
    //                    list.clearReplaceExpr();
    //            }
    //            // [perf tag] 22.0965 us: sharding multivalue
    //        }
    //    }

    //    private static void replacePartitionKeyOperand(Map<String, Map<Object, Set<Pair<Expression, ASTNode>>>> index,
    //                                                   List<String> cols) {
    //        if (cols == null) {
    //            return;
    //        }
    //        for (String col : cols) {
    //            Map<Object, Set<Pair<Expression, ASTNode>>> map = index.get(col);
    //            if (map == null) {
    //                continue;
    //            }
    //            for (Set<Pair<Expression, ASTNode>> set : map.values()) {
    //                if (set == null) {
    //                    continue;
    //                }
    //                for (Pair<Expression, ASTNode> p : set) {
    //                    Expression expr = p.getKey();
    //                    ASTNode parent = p.getValue();
    //                    if (PartitionKeyVisitor.isPartitionKeyOperandSingle(expr, parent)) {
    //                        ((ReplacableExpression) expr).setReplaceExpr(ReplacableExpression.BOOL_FALSE);
    //                    } else if (PartitionKeyVisitor.isPartitionKeyOperandIn(expr, parent)) {
    //                        ((ReplacableExpression) parent).setReplaceExpr(ReplacableExpression.BOOL_FALSE);
    //                    }
    //                }
    //            }
    //        }
    //    }

    //    private static void dispatchInsertReplace(RouteResultsetNode[] rn, DMLInsertReplaceStatement stmt,
    //                                              List<String> ruleColumns, Map<Integer, List<Object[]>> dataNodeMap,
    //                                              TableConfig matchedTable, String originalSQL, PartitionKeyVisitor visitor) {
    //        if (stmt.getSelect() != null) {
    //            dispatchWhereBasedStmt(rn, stmt, ruleColumns, dataNodeMap, matchedTable, originalSQL, visitor);
    //            return;
    //        }
    //        Map<String, Map<Object, Set<Pair<Expression, ASTNode>>>> colsIndex = visitor.getColumnIndex(stmt.getTable()
    //                                                                                                        .getIdTextUpUnescape());
    //        if (colsIndex == null || colsIndex.isEmpty()) {
    //            throw new IllegalArgumentException("columns index is empty: " + originalSQL);
    //        }
    //        ArrayList<Map<Object, Set<Pair<Expression, ASTNode>>>> colsIndexList = new ArrayList<Map<Object, Set<Pair<Expression, ASTNode>>>>(
    //                ruleColumns.size());
    //        for (int i = 0, len = ruleColumns.size(); i < len; ++i) {
    //            colsIndexList.add(colsIndex.get(ruleColumns.get(i)));
    //        }
    //        int dataNodeId = -1;
    //        for (Entry<Integer, List<Object[]>> en : dataNodeMap.entrySet()) {
    //            List<Object[]> tuples = en.getValue();
    //            HashSet<RowExpression> replaceRowList = new HashSet<RowExpression>(tuples.size());
    //            for (Object[] tuple : tuples) {
    //                Set<Pair<Expression, ASTNode>> tupleExprs = null;
    //                for (int i = 0; i < tuple.length; ++i) {
    //                    Map<Object, Set<Pair<Expression, ASTNode>>> map = colsIndexList.get(i);
    //                    Object value = tuple[i];
    //                    Set<Pair<Expression, ASTNode>> set = getExpressionSet(map, value);
    //                    tupleExprs = CollectionUtil.intersectSet(tupleExprs, set);
    //                }
    //                if (tupleExprs == null || tupleExprs.isEmpty()) {
    //                    throw new IllegalArgumentException("route: empty expression list for insertReplace stmt: "
    //                            + originalSQL);
    //                }
    //                for (Pair<Expression, ASTNode> p : tupleExprs) {
    //                    if (p.getValue() == stmt && p.getKey() instanceof RowExpression) {
    //                        replaceRowList.add((RowExpression) p.getKey());
    //                    }
    //                }
    //            }
    //
    //            stmt.setReplaceRowList(new ArrayList<RowExpression>(replaceRowList));
    //            String sql = buildSQL(stmt);
    //            stmt.clearReplaceRowList();
    //            String dataNodeName = matchedTable.getDataNodes()[en.getKey()];
    //            rn[++dataNodeId] = new RouteResultsetNode(dataNodeName, sql);
    //        }
    //    }
    //
    //    private static Set<Pair<Expression, ASTNode>> getExpressionSet(Map<Object, Set<Pair<Expression, ASTNode>>> map,
    //                                                                   Object value) {
    //        if (map == null || map.isEmpty()) {
    //            return Collections.emptySet();
    //        }
    //        Set<Pair<Expression, ASTNode>> set = map.get(value);
    //        if (set == null) {
    //            return Collections.emptySet();
    //        }
    //        return set;
    //    }

    private static String buildSQL(SQLStatement ast) {
        StringBuilder sb = new StringBuilder();
        ast.accept(new MySQLOutputASTVisitor(sb));
        return sb.toString();
    }

}
