package com.zte.jbundle.fre.flow.define;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 工作节点，每个节点管理一个作业角色。工作节点分为开始工作、结束工作、正常工作
 * 
 * @author PanJun
 * 
 */
public class Node {

    /** 节点类型：开始节点 */
    public static int NTYPE_START = 1;
    /** 节点类型：工作节点 */
    public static int NTYPE_WORK = 0;
    /** 节点类型：结束节点 */
    public static int NTYPE_END = 2;
    /** 节点类型：同步节点 */
    public static int NTYPE_SYNC = 3;

    /** 规则引擎内部节点Id */
    private int id;
    /** 节点名称 */
    private String title;
    /** 节点类型 */
    private int ntype;
    /** 扩展信息 */
    private String ext;
    /** 角色，支持多个，以逗号分隔 */
    private String roles;
    /** 角色，解析roles产生 */
    private Set<String> roleSet = new HashSet<String>();

    final List<Edge> inputs = new ArrayList<Edge>();
    final List<Edge> outputs = new ArrayList<Edge>();

    public Node(int id, String title, int ntype, String roles, String ext) {
        this.id = id;
        this.title = title;
        this.ntype = ntype;
        this.roles = roles;
        if (roles != null) {
            for (String s : roles.split(",")) {
                roleSet.add(s.trim());
            }
        }
        roleSet = Collections.unmodifiableSet(roleSet);

        this.ext = ext;
    }

    @Override
    public String toString() {
        return "{id=" + id + ",ntype=" + ntype + ",title=" + title + ",ext=" + ext + ",roles=" + roles + "}";
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getExt() {
        return ext;
    }

    public String getRoles() {
        return roles;
    }

    public List<Edge> getInputs() {
        return inputs;
    }

    public List<Edge> getOutputs() {
        return outputs;
    }

    public Set<String> getRoleSet() {
        return roleSet;
    }

    public int getNtype() {
        return ntype;
    }

    public boolean isStartNode() {
        return ntype == Node.NTYPE_START;
    }

    public boolean isEndNode() {
        return ntype == Node.NTYPE_END;
    }

    public boolean isSyncNode() {
        return ntype == Node.NTYPE_SYNC;
    }

    public boolean isWorkNode() {
        return ntype == Node.NTYPE_WORK;
    }

}
