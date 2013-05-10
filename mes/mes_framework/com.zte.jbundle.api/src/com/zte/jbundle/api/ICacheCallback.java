package com.zte.jbundle.api;

/**
 * 缓存回调接口
 * 
 * @author PanJun
 * 
 */
public interface ICacheCallback<T> {

    T get(String key);

}
