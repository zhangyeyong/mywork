package com.zte.jbundle.api;

import java.util.List;

/**
 * OSGI上下文，用户获取Osgi服务
 * 
 * @author PanJun
 * 
 */
public class OsgiContext {

    /**
     * 支持OSGI上下文接口
     * 
     * @author PanJun
     * 
     */
    public static interface IOsgiContextable {
        public <T> List<T> getAllServices(Class<T> clazz);
    }

    private static IOsgiContextable osgiContextable;

    /**
     * 获取所有服务
     * 
     * @param clazz
     * @return
     */
    public static <T> List<T> getAllServices(Class<T> clazz) {
        return osgiContextable.getAllServices(clazz);
    }

    /**
     * 提供IOsgiContextable注入点
     * 
     * @param osgiContextable
     */
    public static void setOsgiContextable(IOsgiContextable osgiContextable) {
        OsgiContext.osgiContextable = osgiContextable;
    }

}
