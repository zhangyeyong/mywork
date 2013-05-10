package com.zte.mcore.ioc;

import java.lang.reflect.Field;

/**
 * 依赖注入事件
 * 
 * @author PanJun
 * 
 */
public interface OnDependencyInject {

    /**
     * 处理字段值的注入
     * 
     * @param field
     * @param resourceName
     * @param target
     * @return 返回true框架不再处理注入
     * @throws Exception
     */
    public boolean handle(Object target, Field field, String resourceName) throws Exception;

}
