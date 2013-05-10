package com.zte.jbundle.fre.flow.define;

/**
 * 节点路径，标识一个节点到另外一个节点转移条件
 * 
 * @author PanJun
 * 
 */
public class Edge {

    private int id;
    private String expr;
    private Node srcNode;
    private Node dstNode;
    private int order;

    public Edge(int id, String expr, Node srcNode, Node dstNode, int order) {
        this.id = id;
        this.expr = expr;
        this.srcNode = srcNode;
        this.dstNode = dstNode;
        this.order = order;
    }

    @Override
    public String toString() {
        return "{id=" + id + ",expr=" + expr + ",src=" + srcNode.getTitle() + ",dst=" + dstNode.getTitle() + "}";
    }

    public int getId() {
        return id;
    }

    public String getExpr() {
        return expr;
    }

    public Node getSrcNode() {
        return srcNode;
    }

    public Node getDstNode() {
        return dstNode;
    }

    public int getOrder() {
        return order;
    }

    public int getDstId() {
        return dstNode.getId();
    }

    public int getSrcId() {
        return srcNode.getId();
    }

}
