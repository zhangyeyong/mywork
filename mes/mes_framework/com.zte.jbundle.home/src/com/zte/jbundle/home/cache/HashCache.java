package com.zte.jbundle.home.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.zte.jbundle.api.ICache;
import com.zte.jbundle.api.ICacheCallback;

/**
 * 通过HashMap做为存储容器的缓存服务
 * 
 * @author PanJun
 * 
 */
public class HashCache implements ICache {

    private ConcurrentHashMap<String, CacheItem> dataMap = new ConcurrentHashMap<String, CacheItem>();

    @Override
    public void put(String key, Object val) {
        put(key, val, -1);
    }

    @Override
    public void put(String key, Object val, int seconds) {
        if (key == null || val == null) {
            return;
        }

        if (seconds <= 0) {
            seconds = (int) TimeUnit.MINUTES.toSeconds(10);
        }
        CacheItem item = new CacheItem();
        item.Data = val;
        item.key = key.toLowerCase();
        item.expired = System.currentTimeMillis() + seconds * 1000;
        item.keepSeconds = seconds;
        dataMap.put(key, item);
        MapClearThread.startup(dataMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) get(key, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, ICacheCallback<T> callback) {
        if (key == null) {
            return null;
        }
        MapClearThread.startup(dataMap);

        key = key.toLowerCase();
        CacheItem item = dataMap.get(key);
        if (item != null) {
            long now = System.currentTimeMillis();
            if (item.expired > now) {
                item.expired = now + item.keepSeconds * 1000;
                return (T) item.Data;
            } else {
                dataMap.remove(key);
            }
        }
        if (callback != null) {
            T t = (T) callback.get(key);
            if (t != null) {
                put(key, t);
            }
            return t;
        }
        return null;
    }

    @Override
    public void remove(String key) {
        dataMap.remove(key);
    }

}
