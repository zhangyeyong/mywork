package com.zte.jbundle.api;

import java.util.Collections;
import java.util.List;

/**
 * 多语言支持类
 * 
 * @author PanJun
 * 
 */
public class I18n {

    public static interface II18nable {

        public String get(String lang, String msgCode);

    }

    /**
     * 语言环境
     * 
     * @author PanJun
     * 
     */
    public static class LangEnv {

        private String code;
        private String name;

        public LangEnv() {
        }

        public LangEnv(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    private static II18nable i18nable = null;

    /** 当前语言 */
    private static ThreadLocal<String> langEnv = new ThreadLocal<String>();

    /** 默认语言 */
    private static List<LangEnv> langEnvList = Collections.emptyList();

    /**
     * 获取当前语言的资源，如果没有，找不带语言标识中的资源
     * 
     * @param msgCode
     * @param args
     * @return
     */
    public static String get(String msgCode, Object... args) {
        String lang = getLang();
        String msg = i18nable.get(lang, msgCode);
        if (msg == null) {
            msg = i18nable.get("", msgCode);
        }

        if (msg == null) {
            return null;
        }

        if (args.length == 0) {
            return msg;
        }
        return String.format(msg, args);
    }

    public static II18nable getI18nable() {
        return i18nable;
    }

    public static void setI18nable(II18nable i18nable) {
        I18n.i18nable = i18nable;
    }

    /**
     * 获取当前语言标识
     * 
     */
    public static String getLang() {
        String ret = langEnv.get();
        if (ret == null || ret.trim().length() == 0) {
            ret = getDefaultLang();
        }
        return ret;
    }

    /**
     * 设置当前语言标识
     * 
     * @param lang
     */
    public static void setLang(String lang) {
        I18n.langEnv.set(lang);
    }

    /**
     * 获取缺省语言
     * 
     * @return
     */
    public static String getDefaultLang() {
        if (langEnvList != null && !langEnvList.isEmpty()) {
            return langEnvList.get(0).code;
        }
        return "";
    }

    public static List<LangEnv> getLangEnvList() {
        return langEnvList;
    }

    public static void setLangEnvList(List<LangEnv> langEnvList) {
        if (langEnvList == null || langEnvList.isEmpty()) {
            I18n.langEnvList = Collections.emptyList();
        } else {
            I18n.langEnvList = Collections.unmodifiableList(langEnvList);
        }
    }

}
