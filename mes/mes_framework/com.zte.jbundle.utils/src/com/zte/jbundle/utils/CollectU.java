package com.zte.jbundle.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 集合工具类
 * 
 * @author PanJun
 * 
 */
public final class CollectU {

    public static <T> boolean isEmpty(Collection<T> coll) {
        return (coll == null) || coll.isEmpty();
    }

    /**
     * 把array里的内容加入到coll中
     * 
     * @param <T>
     * @param coll
     * @param array
     * @return coll
     */
    public static <T> Collection<T> addAll(Collection<T> coll, T[] array) {
        if (array != null || coll != null) {
            for (T a : array) {
                coll.add(a);
            }
        }
        return coll;
    }

    /**
     * 把collection里的内容连接成字符串
     * 
     * @param <T>
     * @param coll
     * @param seperator
     *            分割符
     * @param quoter
     *            引号
     * @return
     */
    public static <T> String link(Collection<T> coll, String seperator, String quoter) {
        if (coll == null || coll.isEmpty())
            return "";

        seperator = seperator == null ? "" : seperator;
        quoter = quoter == null ? "" : quoter;

        String loopSep = "";
        StringBuilder sb = new StringBuilder();
        for (T t : coll) {
            sb.append(loopSep).append(quoter).append(t).append(quoter);
            loopSep = seperator;
        }
        return sb.toString();
    }

    /**
     * 把collection里的内容连接成字符串
     * 
     * @param <T>
     * @param coll
     * @param seperator
     * @return
     */
    public static <T> String link(Collection<T> coll, String seperator) {
        return link(coll, seperator, "");
    }

    /**
     * 把collection里的对象转移到指定类型的array返回
     * 
     * @param <T>
     * @param coll
     * @param resultClass
     *            数组类型
     * @return
     */
    public static <T> T[] toArray(Collection<T> coll, Class<T> resultClass) {
        if (coll == null)
            return null;

        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(resultClass, coll.size());

        int i = 0;
        for (Iterator<T> iter = coll.iterator(); iter.hasNext(); i++) {
            result[i] = iter.next();
        }
        return result;
    }

    /**
     * 把collection里的对象拷贝成新对象再转移到指定类型的array返回
     * 
     * @param <T>
     * @param <C>
     * @param coll
     * @param resultClass
     *            数组类型，此类型要有缺省构造函数
     * @return
     */
    public static <T, C> T[] toArrayWithNew(Collection<C> coll, Class<T> resultClass) {
        if (coll == null)
            return null;

        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(resultClass, coll.size());
        int i = 0;
        for (Iterator<C> iter = coll.iterator(); iter.hasNext(); i++) {
            try {
                result[i] = BeanU.copyProp(iter.next(), resultClass.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    /**
     * 从Map中获取值
     * 
     * @param map
     * @param k
     * @return
     */
    public static <K, V> V getMapValue(Map<K, V> map, K k) {
        if (map == null) {
            return null;
        } else {
            return map.get(k);
        }
    }

    /**
     * 从Map中获取值，如果没有使用缺省值返回
     * 
     * @param map
     * @param k
     * @return
     */
    public static <K, V> V getMapValue(Map<K, V> map, K k, V defaultValue) {
        if (map == null) {
            return null;
        } else {
            V ret = map.get(k);
            if (ret == null) {
                ret = defaultValue;
            }
            return ret;
        }
    }

    /**
     * 取列表中第一个元素
     * 
     * @param list
     * @return
     */
    public static <T> T firstElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 取列表中下标元素
     * 
     * @param list
     * @param index
     * @return
     */
    public static <T> T elementAt(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        } else {
            return list.get(index);
        }
    }

    /**
     * 取列表中下标元素，并转换成指定类型，无法转换，返回空值
     * 
     * @param list
     * @param index
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T elementAt(List list, int index, Class<T> clazz) {
        if (list == null || index < 0 || index >= list.size()) {
            return null;
        } else {
            Object ret = list.get(index);
            if (clazz.isInstance(ret)) {
                return (T) ret;
            } else {
                return null;
            }
        }
    }

}