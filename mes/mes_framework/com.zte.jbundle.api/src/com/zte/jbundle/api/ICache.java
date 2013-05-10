package com.zte.jbundle.api;

/**
 * 
 * 缓存接口
 * 
 * @author PanJun
 * 
 */
public interface ICache {

    /**
     * 
     * 把对象放入缓存，如果对象已存在则替换
     * 
     * @param key
     *            键
     * @param val
     *            要缓存的对象，须实现java.io.Serializable接口
     */
    void put(String key, Object val);

    /**
     * 
     * 把对象放入缓存，如果对象已存在则替换
     * 
     * @param key
     *            键
     * @param val
     *            要缓存的对象，须实现java.io.Serializable接口
     * @param seconds
     *            缓存过期秒数
     */
    void put(String key, Object val, int seconds);

    /**
     * 
     * 从缓存中获取对象
     * 
     * @param key
     *            键
     * @return 缓存对象
     */
    <T> T get(String key);

    /**
     * 
     * 从缓存中获取对象
     * 
     * @param key
     *            键
     * @param callback
     *            当缓存中不存在指定键对象时，回调获取对象
     * @return 缓存对象
     */
    <T> T get(String key, ICacheCallback<T> callback);

    /**
     * 
     * 根据键删除缓存对象
     * 
     * @param key
     *            键
     */
    void remove(String key);

}
