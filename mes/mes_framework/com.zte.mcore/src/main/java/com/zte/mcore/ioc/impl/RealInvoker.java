package com.zte.mcore.ioc.impl;

import java.lang.reflect.Method;

import com.zte.mcore.ioc.Invoker;

class RealInvoker implements Invoker {

    private Object target;
    private Method method;
    private Object[] args;

    public RealInvoker(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object proceed() throws Throwable {
        return method.invoke(target, args);
    }

}
