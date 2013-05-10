package com.zte.jbundle.utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Java语言相关工具
 * 
 * @author PanJun
 * 
 */
public final class LangU {

    static class FilterPrintStream extends PrintStream {

        private final String[] maskWords;

        public FilterPrintStream(OutputStream out, String... maskWords) {
            super(out, false);
            this.maskWords = maskWords;
        }

        @Override
        public void println(String s) {
            if (s == null) {
                return;
            }

            boolean containsMaksWord = false;
            for (String word : maskWords) {
                if (s.toLowerCase().contains(word)) {
                    containsMaksWord = true;
                    break;
                }
            }

            if (!containsMaksWord) {
                super.println(s);
            }
        }
    }

    /**
     * 获取异常堆栈信息,可根据屏蔽包含关键字的行,并对最终返回结果进行最大长度限制
     * 
     * @param e
     * @param maxLen
     * @param maskWords
     * @return
     */
    public static String getStackMsg(Throwable e, int maxLen, String... maskWords) {
        return StringU.limit(getStackMsg(e, maskWords), maxLen);
    }

    /**
     * 获取异常堆栈信息,可根据屏蔽包含关键字的行
     * 
     * @param e
     * @param maskWords
     *            进行行屏蔽关键字
     * @return
     */
    public static String getStackMsg(Throwable e, String... maskWords) {
        String result = "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new FilterPrintStream(bos, maskWords);
        e.printStackTrace(ps);
        try {
            result = bos.toString("utf-8");
        } catch (UnsupportedEncodingException nothing) {
        }
        return result;
    }

    /**
     * 获取异常堆栈信息,进行缺省关键字屏蔽(spring框架、java反射堆栈行)，并对输出长度限制
     * 
     * @param e
     * @param maxLen
     * @return
     */
    public static String getStackMaskedMsg(Throwable e, int maxLen) {
        return StringU.limit(getStackMaskedMsg(e), maxLen);
    }

    /**
     * 获取异常堆栈信息,进行缺省关键字屏蔽(spring框架、java反射、代理堆栈行)
     * 
     * @param e
     * @return
     */
    public static String getStackMaskedMsg(Throwable e) {
        return getStackMsg(e, "sun.reflect.", "java.lang.reflect.", ".springframework.", " $Proxy");
    }

    /**
     * Add all fields of clazz to set object, contains private/protected/package
     * visibility fields
     * 
     * @param clazz
     * @return
     */
    public static Set<Field> findAllFields(Class<?> clazz) {
        Set<Field> ret = new HashSet<Field>();
        if (clazz != null) {
            while (clazz != null) {
                ret.addAll(Arrays.asList(clazz.getDeclaredFields()));
                clazz = clazz.getSuperclass();
            }
        }
        return ret;
    }

    /**
     * Add all fields of clazz to set object, which marked "annClazz"
     * 
     * @param clazz
     * @param annClazz
     * @return
     */
    public static List<Field> findAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annClazz) {
        List<Field> ret = new ArrayList<Field>();
        for (Field f : LangU.findAllFields(clazz)) {
            for (Annotation a : f.getAnnotations()) {
                if (annClazz.isAssignableFrom(a.getClass())) {
                    ret.add(f);
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * get annotation which marked "annClazz"
     * 
     * @param clazz
     * @param annClazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annClazz) {
        for (Annotation ann : clazz.getAnnotations()) {
            if (annClazz.isAssignableFrom(ann.getClass())) {
                return (T) ann;
            }
        }
        return null;
    }

    /**
     * get field which marked "name"
     * 
     * @param clazz
     * @param name
     * @return
     */
    public static Field getFieldByName(Class<?> clazz, String name) {
        for (Field f : LangU.findAllFields(clazz)) {
            if (f.getName().equals(name)) {
                return f;
            }

        }
        return null;
    }

}