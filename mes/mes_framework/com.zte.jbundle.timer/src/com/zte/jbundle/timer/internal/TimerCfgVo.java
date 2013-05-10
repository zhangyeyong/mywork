package com.zte.jbundle.timer.internal;

import java.util.ArrayList;
import java.util.List;

public class TimerCfgVo {

    private String identifier;
    private long bundleId;
    private String service;
    private String method;
    private Integer second;
    private String cron;
    private List<String> args = new ArrayList<String>();

    @Override
    public String toString() {
        return "[" + identifier + "] second=" + second + ",cron=" + cron + ",args=" + args;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public long getBundleId() {
        return bundleId;
    }

    public void setBundleId(long bundleId) {
        this.bundleId = bundleId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getSecond() {
        return second;
    }

    public void setSecond(Integer interval) {
        this.second = interval;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

}
