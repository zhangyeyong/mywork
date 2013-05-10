package com.zte.jbundle.fre.flow.caze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zte.jbundle.fre.expr.ExprEngine;
import com.zte.jbundle.fre.flow.define.Edge;
import com.zte.jbundle.fre.flow.define.FlowDefine;
import com.zte.jbundle.fre.flow.define.Node;
import com.zte.jbundle.fre.flow.utils.RuleUtils;

/**
 * 规则实例
 * 
 * @author PanJun
 * 
 */
public class FlowCase {

    /** 规制实例包含的节点实例 */
    private Map<Integer, NodeCase> nodeCases = new HashMap<Integer, NodeCase>();

    public FlowCase(FlowDefine flowDefine, String caseStatus, Map<String, Object> contextMap) {
        NodeCase startNode = null;
        for (Node node : flowDefine.getNodes()) {
            NodeCase instance = new NodeCase(this, node);
            if (node.getNtype() == Node.NTYPE_START) {
                startNode = instance;
            }

            nodeCases.put(node.getId(), instance);
        }

        if (!restoreCaseStatus(caseStatus)) {
            // 如果没有成功恢复流程实例状态，则自动完成开始节点
            closeNode(startNode, contextMap);
        }
    }

    private boolean restoreCaseStatus(String caseStatus) {
        if (caseStatus == null || caseStatus.trim().length() == 0) {
            return false;
        }

        boolean ret = false;
        synchronized (nodeCases) {
            List<String> statusPairs = RuleUtils.split(caseStatus, ",");
            for (String pair : statusPairs) {
                int i = pair.indexOf("=");
                int nodeId = Integer.parseInt(pair.substring(0, i));
                List<String> statusStr = RuleUtils.split(pair.substring(i + 1), "|");
                if (!statusStr.isEmpty()) {
                    NodeCase nodeCase = nodeCases.get(nodeId);
                    if (nodeCase != null) {
                        nodeCase.setStatus(NodeCase.toStatus(statusStr.get(0)));
                        ret = ret || nodeCase.getStatus() != NodeCase.STATUS_INIT;

                        for (int k = 1, size = statusStr.size(); k < size; k++) {
                            nodeCase.addPassedNode(Integer.parseInt(statusStr.get(k)));
                        }
                    }
                }
            }
        }
        return ret;
    }

    public String getStatus() {
        synchronized (nodeCases) {
            String split = "";
            StringBuilder sbRet = new StringBuilder();
            for (NodeCase nodeCase : nodeCases.values()) {
                sbRet.append(split).append(nodeCase.getId()).append("=").append(nodeCase.getStatus());
                nodeCase.linkPassedNodeIds(sbRet, "|");
                split = ",";
            }
            return sbRet.toString();
        }
    }

    /**
     * 罗列出当前规则实例可执行的工作节点
     */
    public List<NodeCase> listWorkingNodes() {
        List<NodeCase> result = new ArrayList<NodeCase>();
        for (NodeCase work : nodeCases.values()) {
            if (work.getNode().isWorkNode() && work.getStatus() == NodeCase.STATUS_DOING) {
                result.add(work);
            }
        }
        return result;
    }

    /**
     * 罗列出当前节点所有孩子节点
     */
    public List<NodeCase> listChildNodes(String role) {
        List<NodeCase> result = new ArrayList<NodeCase>();
        Node node = null;
        for (NodeCase all : nodeCases.values()) {
            if (all.getNode().isWorkNode() && all.getNode().getRoles().equals(role)) {
                node = all.getNode();
                break;
            }
        }
        if (null != node) {
            List<Edge> output = node.getOutputs();
            for (NodeCase child : nodeCases.values()) {
                for (Edge edge : output) {
                    if (child.getNode().getId() == edge.getDstNode().getId()) {
                        result.add(child);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 取出一个可执行的工作节点
     */
    public NodeCase getWorkingNode() {
        for (NodeCase work : nodeCases.values()) {
            if (work.getNode().isWorkNode() && work.getStatus() == NodeCase.STATUS_DOING) {
                return work;
            }
        }
        return null;
    }

    /**
     * 如果流程结束，调用此方法罗列出结束节点
     */
    public List<NodeCase> listEndingNodes() {
        List<NodeCase> result = new ArrayList<NodeCase>();
        for (NodeCase nodeCaze : nodeCases.values()) {
            if (nodeCaze.getNode().isEndNode() && nodeCaze.getStatus() != NodeCase.STATUS_INIT) {
                result.add(nodeCaze);
            }
        }
        return result;
    }

    /**
     * 规制是否已经结束，如果结束节点状态为已经完成，返回真
     * 
     * @return
     */
    public boolean isFinished() {
        for (NodeCase caze : nodeCases.values()) {
            if (caze.getNode().isEndNode() && caze.getStatus() != NodeCase.STATUS_INIT) {
                return true;
            }
        }
        return false;
    }

    private boolean isPassable(Edge p, Map<String, Object> contextMap) {
        if (RuleUtils.isBlank(p.getExpr())) {
            return true;
        }
        ExprEngine expr = new ExprEngine(p.getExpr());
        expr.setParam(contextMap);
        return expr.asBoolean();
    }

    int closeNode(NodeCase node, Map<String, Object> contextMap) {
        synchronized (nodeCases) {
            String savedCaseStatus = getStatus();

            int ret = innerCloseNode(node, contextMap);
            if (ret != NodeCase.CLOSE_SUCCESS) {
                restoreCaseStatus(savedCaseStatus);
            } else if (isFinished()) {
                for (NodeCase nodeCaze : nodeCases.values()) {
                    if (!nodeCaze.isEndNode() && nodeCaze.getStatus() == NodeCase.STATUS_DOING) {
                        nodeCaze.setStatus(NodeCase.STATUS_INIT);
                    }
                }
            }
            return ret;
        }
    }

    private int innerCloseNode(NodeCase node, Map<String, Object> contextMap) {
        boolean noNextNode = true;
        boolean passingEnd = false;
        List<NodeCase> syncNodes = new ArrayList<NodeCase>();
        for (Edge p : node.getOutputs()) {
            if (isPassable(p, contextMap)) {
                noNextNode = false;
                NodeCase outputCase = nodeCases.get(p.getDstId());
                outputCase.setStatus(NodeCase.STATUS_DOING);// 下一节点设置成进行中
                if (outputCase.isSyncNode()) {
                    outputCase.addPassedNode(node.getId());
                    syncNodes.add(outputCase);
                }
                passingEnd = passingEnd || p.getDstNode().isEndNode();
            }
        }

        // 找不到下一节点
        if (noNextNode) {
            return NodeCase.CLOSE_NO_NEXT;
        }

        if (!passingEnd) {// 流程没有结束，自动关闭同步节点
            for (NodeCase n : syncNodes) {
                if ("or".equals(n.getNode().getExt()) || n.isAllPassed(n.getInputs())) {
                    if (innerCloseNode(n, contextMap) != NodeCase.CLOSE_SUCCESS) {
                        return NodeCase.CLOSE_NO_NEXT;
                    }
                }
            }
        }
        node.setStatus(NodeCase.STATUS_DONE);// 完成当前节点
        return NodeCase.CLOSE_SUCCESS;
    }

}
