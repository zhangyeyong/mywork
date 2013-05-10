package com.zte.jbundle.api;

/**
 * AOP拦截规则构造器
 * 
 * @author PanJun
 * 
 */
public interface AdviceBuilder {

    /**
     * 搭建切片作用的目标类的名称规则
     * 
     * @return
     */
    public Advice[] advices();

    /**
     * 判断给定的ClassName是否需要进行切片处理
     * 
     * @return
     */
    public boolean ifAdvised(String className);

}
