package com.zte.jbundle.fre.test.rule;

import java.util.ArrayList;
import java.util.List;

public class PenTask {

    private int id;
    private Pen pen;
    private String role;
    private User user;
    private String status = "INIT";// DONE
    private int ruleNodeId;

    private static int taskId = 1;

    @Override
    public String toString() {
        return "id:" + id + ", Pen:{" + pen + "}";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Pen getPen() {
        return pen;
    }

    public void setPen(Pen pen) {
        this.pen = pen;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRuleNodeId() {
        return ruleNodeId;
    }

    public void setRuleNodeId(int ruleNodeId) {
        this.ruleNodeId = ruleNodeId;
    }

    private static List<PenTask> tasks = new ArrayList<PenTask>();

    public static void removeTask(Pen pen, int ruleNodeId) {
        for (int i = tasks.size() - 1; i > -1; i--) {
            if (tasks.get(i).pen == pen && tasks.get(i).ruleNodeId == ruleNodeId) {
                tasks.remove(i);
            }
        }
    }

    public static void addTask(Pen pen, String role, User user, int ruleNodeId) {
        PenTask pt = new PenTask();
        pt.id = taskId++;
        pt.user = user;
        pt.pen = pen;
        pt.role = role;
        pt.ruleNodeId = ruleNodeId;
        tasks.add(pt);
    }

    public static List<PenTask> getUserTasks(User user) {
        List<PenTask> ret = new ArrayList<PenTask>();
        for (PenTask pt : tasks) {
            if (user.getNum().equals(pt.user.getNum())) {
                ret.add(pt);
            }
        }
        return ret;
    }

}
