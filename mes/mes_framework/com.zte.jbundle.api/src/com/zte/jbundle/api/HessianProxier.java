package com.zte.jbundle.api;

/**
 * Hessian远程调用代理类
 * 
 * @author PanJun
 * 
 */
public class HessianProxier {

    public static interface IHessianProxiable {
        public <T> T proxyIt(Class<T> clazz, String rsUrl);
    }

    private static IHessianProxiable proxiable;

    public <T> T proxyIt(Class<T> clazz, String rsUrl) {
        return proxiable.proxyIt(clazz, rsUrl);
    }

    public static IHessianProxiable getProxiable() {
        return proxiable;
    }

    public static void setProxiable(IHessianProxiable proxiable) {
        HessianProxier.proxiable = proxiable;
    }

}
