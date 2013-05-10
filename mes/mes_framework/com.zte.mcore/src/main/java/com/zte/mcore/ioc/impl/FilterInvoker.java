package com.zte.mcore.ioc.impl;

import java.lang.reflect.Method;

import com.zte.mcore.ioc.Advice;
import com.zte.mcore.ioc.Invoker;

class FilterInvoker implements Invoker {

    private final Advice advice;
    private final Invoker invoker;

    public FilterInvoker(Advice advice, Invoker invoker) {
        this.advice = advice;
        this.invoker = invoker;
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
