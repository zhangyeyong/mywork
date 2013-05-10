package com.zte.mcore.timer;

import java.lang.reflect.Method;

public class TimerCfgVo {

    private String identifier;
    private Class<?> clazz;
    private String methodName;
    private Method method;
    private Integer second;
    private String cron;
    private Object target;
    private Object[] args = new Object[0];

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

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String method) {
        this.methodName = method;
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

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

}
