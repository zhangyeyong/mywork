package com.zte.jbundle.home.ioc;

import java.lang.reflect.Method;

import com.zte.jbundle.api.Invoker;

public class FinalInvoker implements Invoker {

    private Object target;
    private Method method;
    private Object[] args;

    public FinalInvoker(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    @Override
    public String toString() {
        return target.getClass().getName() + "->" + method.getName();
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
