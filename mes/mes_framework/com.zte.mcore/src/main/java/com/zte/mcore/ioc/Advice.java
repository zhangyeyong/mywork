package com.zte.mcore.ioc;

/**
 * AOP拦截接口
 * 
 * @author PanJun
 * 
 */
public interface Advice {

    /**
     * 方法调用过滤
     * 
     * @param invoker
     * @return
     * @throws Throwable
     */
    public Object invoke(Invoker invoker) throws Throwable;

}
