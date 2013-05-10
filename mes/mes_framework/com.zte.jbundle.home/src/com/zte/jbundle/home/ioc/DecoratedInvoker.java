package com.zte.jbundle.home.ioc;

import java.lang.reflect.Method;

import com.zte.jbundle.api.Advice;
import com.zte.jbundle.api.Invoker;

public class DecoratedInvoker implements Invoker {

    private final Advice advice;
    private final Invoker invoker;

    public DecoratedInvoker(Advice advice, Invoker invoker) {
        this.advice = advice;
        this.invoker = invoker;
    }

    @Override
    public String toString() {
        return invoker.toString();
    }

    @Override
    public Object getTarget() {
        return invoker.getTarget();
    }

    @Override
    public Method getMethod() {
        return invoker.getMethod();
    }

    @Override
    public Object[] getArgs() {
        return invoker.getArgs();
    }

    @Override
    public Object proceed() throws Throwable {
        return advice.invoke(invoker);
    }

}
