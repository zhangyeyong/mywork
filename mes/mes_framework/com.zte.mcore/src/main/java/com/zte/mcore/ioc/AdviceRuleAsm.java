package com.zte.mcore.ioc;

import java.util.List;

/**
 * AOP拦截规则装配器
 * 
 * @author PanJun
 * 
 */
public interface AdviceRuleAsm {

    /**
     * 搭建切片作用的目标类的名称规则
     * 
     * @return
     */
    public List<AdviceRule> buildAdviceRules();

}
