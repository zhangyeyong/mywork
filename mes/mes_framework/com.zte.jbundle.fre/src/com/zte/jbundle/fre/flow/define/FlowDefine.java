package com.zte.jbundle.fre.flow.define;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.zte.jbundle.fre.flow.caze.FlowCase;
import com.zte.jbundle.fre.flow.utils.RuleUtils;

/**
 * 规则定义:节点+路径
 * 
 * @author PanJun
 * 
 */
public class FlowDefine {

    private String json;
    private List<Node> nodes = new ArrayList<Node>();
    private Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
    private List<Edge> edges = new ArrayList<Edge>();

    private FlowDefine() {
    }

    @Override
    public String toString() {
        return "nodes:" + nodes + ",edges:" + edges;
    }

    /**
     * 从json串转换成规则定义对象
     * 
     * @param jsonStr
     * @return
     */
    public static FlowDefine parse(String jsonStr) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            FlowDefine result = new FlowDefine();

            JsonNode root = mapper.readTree(jsonStr);
            JsonNode jsonNodes = root.get("nodes");
            int startCount = 0;
            for (Iterator<JsonNode> iter = jsonNodes.getElements(); iter.hasNext();) {
                JsonNode json = iter.next();
                Node node = new Node(num(json, "id"), text(json, "title"), num(json, "ntype"), text(json, "roles"),
                        text(json, "ext"));
                result.nodes.add(node);
                result.nodeMap.put(node.getId(), node);
                if (node.getNtype() == Node.NTYPE_START) {
                    startCount++;
                }
            }
            if (startCount != 1) {
                throw new FlowException("Need only one START node(START count is:" + startCount + ")!");
            }

            result.nodes = Collections.unmodifiableList(result.nodes);

            JsonNode jsonEdges = root.get("edges");
            for (Iterator<JsonNode> iter = jsonEdges.getElements(); iter.hasNext();) {
                JsonNode json = iter.next();
                Node src = result.nodeMap.get(num(json, "srcId"));
                Node dst = result.nodeMap.get(num(json, "dstId"));
                Edge edge = new Edge(num(json, "id"), text(json, "expr"), src, dst, num(json, "order"));
                result.edges.add(edge);
                src.outputs.add(edge);
                dst.inputs.add(edge);
            }
            result.edges = Collections.unmodifiableList(result.edges);

            result.json = jsonStr;
            return result;
        } catch (Exception e) {
            throw new FlowException("Flow Define Error:"+e.getMessage());
        }
    }

    private static String text(JsonNode json, String field) {
        return json.get(field).asText();
    }

    private static int num(JsonNode json, String field) {
        String s = json.get(field).asText();
        return Integer.parseInt(s);
    }

    /**
     * 开启流程实例，或从缓存中获取流程实例
     * 
     * @param caseStatus
     * @param contextMap
     *            k:pass v:true
     * @return
     */
    public FlowCase openCase(String caseStatus, Map<String, Object> contextMap) {
        return new FlowCase(this, caseStatus, contextMap);
    }

    public List<Node> getNodes() {
        return new ArrayList<Node>(nodes);
    }

    public List<Node> getWorkNodes() {
        List<Node> ret = new ArrayList<Node>();
        for (Node node : nodes) {
            if (node.isWorkNode()) {
                ret.add(node);
            }
        }

        return ret;
    }

    public Node getNodeByExt(String ext) {
        ext = RuleUtils.trim(ext);
        for (Node node : nodes) {
            if (RuleUtils.equals(ext, RuleUtils.trim(node.getExt()))) {
                return node;
            }
        }
        return null;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public String getJson() {
        return json;
    }

}
