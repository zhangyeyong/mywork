package com.zte.jbundle.home.cache;

public class CacheItem {

    public String key;
    public Object Data;
    public long expired;
    public int keepSeconds;

    @Override
    public String toString() {
        return key + "," + expired;
    }
}
