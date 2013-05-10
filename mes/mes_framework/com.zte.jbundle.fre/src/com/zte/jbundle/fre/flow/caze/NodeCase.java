package com.zte.jbundle.fre.flow.caze;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zte.jbundle.fre.expr.ExprException;
import com.zte.jbundle.fre.flow.define.Edge;
import com.zte.jbundle.fre.flow.define.Node;

/**
 * 节点工作项：节点实例
 * 
 * @author PanJun
 * 
 */
public class NodeCase {

    /** 初始化未开始 ，缺省状态 */
    public static final int STATUS_INIT = 0;
    /** 进行中 */
    public static final int STATUS_DOING = 1;
    /** 已完成 */
    public static final int STATUS_DONE = 2;
    /** 关闭节点成功，进入下一节点或流程正常结束 */
    public static final int CLOSE_SUCCESS = 1;
    /** 关闭失败，角色无权关闭节点，流程实例状态不改变 */
    public static final int CLOSE_ROLE_ERR = 2;
    /** 关闭失败，不能进入下一节点 ，流程实例状态不改变 */
    public static final int CLOSE_NO_NEXT = 3;

    /**
     * 处理节点状态，使之符合给定的值
     * 
     * @param s
     * @return
     */
    public static final int toStatus(String strStatus) {
        try {
            int i = Integer.parseInt(strStatus);
            if (i != STATUS_INIT && i != STATUS_DOING && i != STATUS_DONE) {
                return STATUS_INIT;
            } else {
                return i;
            }
        } catch (Exception e) {
            return STATUS_INIT;
        }
    }

    private final FlowCase flowCase;
    private final Node node;
    private int status = STATUS_INIT;
    private Set<Integer> passedNodes = new HashSet<Integer>();

    public NodeCase(FlowCase flowCase, Node node) {
        this.flowCase = flowCase;
        this.node = node;

    }

    /**
     * 
     * 尝试结束当前工作节点，并进入下一工作节点。
     * 
     * @param role
     *            执行角色
     * @param contextMap
     *            上下文,键值对属性
     * @return 如果成功进入下一节点或流程已经结束，返回true；否则返回false
     * @throws ExprException
     */
    public int close(String role, Map<String, Object> contextMap) throws ExprException {
        if (node.getRoleSet().isEmpty() || node.getRoleSet().contains(role)) {
            return flowCase.closeNode(this, contextMap);
        } else {
            return CLOSE_ROLE_ERR;
        }
    }

    public int getStatus() {
        return status;
    }

    void setStatus(int status) {
        this.status = status;
    }

    void addPassedNode(int nodeId) {
        passedNodes.add(nodeId);
    }

    /**
     * 连接经过的节点Id
     * 
     * @param sb
     * @param splitter
     */
    public void linkPassedNodeIds(StringBuilder sb, String splitter) {
        for (Integer i : passedNodes) {
            if (sb.length() > 0) {
                sb.append(splitter);
            }
            sb.append(i);
        }
    }

    public Set<Integer> getPassedNodes() {
        return Collections.unmodifiableSet(passedNodes);
    }

    public boolean isAllPassed(List<Edge> edges) {
        for (Edge e : edges) {
            if (!passedNodes.contains(e.getSrcId())) {
                return false;
            }
        }
        return true;
    }

    public Node getNode() {
        return node;
    }

    public int getId() {
        return node.getId();
    }

    public List<Edge> getInputs() {
        return node.getInputs();
    }

    public List<Edge> getOutputs() {
        return node.getOutputs();
    }

    public int getNtype() {
        return node.getNtype();
    }

    public boolean isStartNode() {
        return getNtype() == Node.NTYPE_START;
    }

    public boolean isEndNode() {
        return getNtype() == Node.NTYPE_END;
    }

    public boolean isSyncNode() {
        return getNtype() == Node.NTYPE_SYNC;
    }

    public boolean isWorkNode() {
        return getNtype() == Node.NTYPE_WORK;
    }

}
