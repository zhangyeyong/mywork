package com.zte.jbundle.fre.test.rule;

import java.util.ArrayList;
import java.util.List;

public class Pen {

    private String num;
    private String ruleStatus;
    private String status;

    @Override
    public String toString() {
        return "num=" + num + ",status=" + status + ",rule=" + ruleStatus;
    }

    public Pen(String num) {
        this.num = num;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRuleStatus() {
        return ruleStatus;
    }

    public void setRuleStatus(String ruleStatus) {
        this.ruleStatus = ruleStatus;
    }

    public static final List<Pen> pens = initPens();

    private static List<Pen> initPens() {
        List<Pen> ret = new ArrayList<Pen>();
        ret.add(new Pen("p1"));
        ret.add(new Pen("p2"));
        return ret;
    }

}
