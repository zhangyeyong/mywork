package com.zte.jbundle.home.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapClearThread extends Thread {

    private final Map<String, CacheItem> dataMap;

    private static MapClearThread instance = null;
    private static int MAX_ITEMS = 1000;

    public synchronized static void startup(Map<String, CacheItem> dataMap) {
        if (instance == null) {
            instance = new MapClearThread(dataMap);
            instance.start();
        }
    }

    private synchronized static void clearInstance() {
        instance = null;
    }

    private MapClearThread(Map<String, CacheItem> dataMap) {
        this.dataMap = dataMap;
    }

    @Override
    public void run() {
        try {
            // 内置10秒钟进行一次内存整理
            sleep(TimeUnit.SECONDS.toMillis(10));
        } catch (InterruptedException nothing) {
        }

        long now = System.currentTimeMillis();
        List<String> expiredKeys = new ArrayList<String>();
        for (String key : dataMap.keySet()) {
            CacheItem item = dataMap.get(key);
            if (now >= item.expired) {
                expiredKeys.add(key);
            }
        }

        for (String key : expiredKeys) {
            dataMap.remove(key);
        }

        List<CacheItem> allItems = new ArrayList<CacheItem>(dataMap.values());
        if (allItems.size() > MAX_ITEMS) {
            Collections.sort(allItems, new Comparator<CacheItem>() {

                @Override
                public int compare(CacheItem o1, CacheItem o2) {
                    if (o1.expired > o2.expired) {
                        return -1;
                    } else if (o1.expired < o2.expired) {
                        return 1;
                    }
                    return 0;
                }
            });

            int remained = MAX_ITEMS * 3 / 4;
            for (int i = allItems.size() - 1; i >= remained; i--) {
                dataMap.remove(allItems.get(i).key);
            }
        }
        clearInstance();
    }
}
