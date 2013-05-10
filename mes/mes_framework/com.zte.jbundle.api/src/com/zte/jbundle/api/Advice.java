package com.zte.jbundle.api;


public interface Advice {

    public Object invoke(Invoker invoker) throws Throwable;

}
