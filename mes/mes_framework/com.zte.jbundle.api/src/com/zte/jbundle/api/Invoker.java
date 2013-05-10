package com.zte.jbundle.api;

import java.lang.reflect.Method;

public interface Invoker {

    public Object getTarget();

    public Method getMethod();

    public Object[] getArgs();

    public Object proceed() throws Throwable;

}
