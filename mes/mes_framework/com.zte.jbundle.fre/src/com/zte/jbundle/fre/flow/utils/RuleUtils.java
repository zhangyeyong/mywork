package com.zte.jbundle.fre.flow.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RuleUtils {

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String trim(String s) {
        return s == null ? null : s.trim();
    }

    public static <K, V> Set<V> getOrInitMap(Map<K, Set<V>> map, K key) {
        Set<V> ret = map.get(key);
        if (ret == null) {
            ret = new HashSet<V>();
            map.put(key, ret);
        }
        return ret;
    }

    public static List<String> split(String s, String sub) {
        List<String> ret = new ArrayList<String>();
        while (s.length() > 0) {
            int i = s.indexOf(sub);
            if (i == -1) {
                ret.add(s);
                break;
            } else {
                ret.add(s.substring(0, i));
                s = s.substring(i + sub.length());
            }
        }
        return ret;
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 != null) {
            return o1.equals(o2);
        } else {
            return o2.equals(o1);
        }
    }

}
