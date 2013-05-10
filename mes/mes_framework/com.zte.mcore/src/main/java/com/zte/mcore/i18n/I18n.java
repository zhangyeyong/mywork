package com.zte.mcore.i18n;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.zte.mcore.hibernate.impl.MapperManager;
import com.zte.mcore.utils.McoreU;
import com.zte.mcore.utils.NullU;
import com.zte.mcore.utils.StringU;

/**
 * 多语言支持类
 * 
 * @author PanJun
 * 
 */
public class I18n {

    /** 存储reses Map容器 */
    static volatile Map<String, Map<String, String>> resMap = new HashMap<String, Map<String, String>>();

    /** 国际化资源文件后缀规范 */
    private static final String SUFFIX_MSG_XML = ".i18n.xml";

    /** 当前语言 */
    private static ThreadLocal<String> langEnv = new ThreadLocal<String>();

    /** 默认语言 */
    private static String defaultLang = "";

    /**
     * 提供get方法根据msgCode获取res
     * 
     * @return
     */
    public static String get(String msgCode) {
        Map<String, String> map = getLangResMap(getLang());
        msgCode = NullU.nvl(msgCode).trim();
        String ret = map.get(msgCode);
        return ret == null ? "" : ret;
    }

    public static String fmt(String msgCode, String lang, Object... args) {
        String ret = get(msgCode);
        if (ret.length() > 0) {
            ret = String.format(ret, args);
        }
        return ret;
    }

    /**
     * 获取某种语言环境下的，资源Map
     * 
     * @param lang
     * @return
     */
    public static Map<String, String> getLangResMap(String lang) {
        lang = StringU.lower(lang);
        Map<String, String> map = resMap.get(lang);
        if (map == null) {
            map = resMap.get("");
        }
        return map;
    }

    /**
     * 截取String 获取语言
     * 
     * @param s
     * @return
     */
    private static String parseLang(String s) {
        int i = s.lastIndexOf("/");
        if (i > -1) {
            s = s.substring(i);
        }

        i = s.lastIndexOf(SUFFIX_MSG_XML);
        if (i > -1) {
            s = s.substring(0, i);
        } else {
            return null;
        }

        i = s.lastIndexOf("-");
        if (i > -1) {
            return s.substring(i + 1).trim().toLowerCase();
        } else {
            return "";
        }
    }

    /**
     * 获取当前语言
     * 
     * @return
     */
    private static String getLang() {
        String ret = langEnv.get();
        if (StringU.isBlank(ret)) {
            ret = defaultLang;
        }
        return ret;
    }

    /**
     * 设置当前线程所处语言环境
     * 
     * @param lang
     */
    public static void setLang(String lang) {
        langEnv.set(lang);
    }

    /**
     * 获取默认语言
     * 
     * @return
     */
    public static String getDefaultLang() {
        return defaultLang;
    }

    /**
     * 设置默认语言
     * 
     * @param defaultLang
     */
    public static void setDefaultLang(String defaultLang) {
        I18n.defaultLang = defaultLang;
    }

    static class I18nHolder {
        public InputStream in;
        public String lang;
    }

    static {
        try {
            Set<String> i18nNames = new HashSet<String>();
            List<String> pkgs = Arrays.asList("mcore.i18n");
            McoreU.loadFileResources(pkgs, SUFFIX_MSG_XML, false, i18nNames);
            McoreU.loadJarResources(pkgs, SUFFIX_MSG_XML, false, i18nNames);

            for (String i18nFile : i18nNames) {
                String lang = parseLang(i18nFile);
                i18nFile = "/" + McoreU.toFilePath(i18nFile) + SUFFIX_MSG_XML;
                URL url = MapperManager.class.getResource(i18nFile);

                Map<String, String> langResMap = resMap.get(lang);
                if (langResMap == null) {
                    langResMap = new HashMap<String, String>();
                    resMap.put(lang, langResMap);
                }

                // 2. 循环资源url put多语言资源到resMap中
                InputStream in = new BufferedInputStream(url.openStream());
                try {
                    Element root = new SAXReader().read(in).getRootElement();
                    @SuppressWarnings("unchecked")
                    List<Element> msgElements = root.elements("msg");
                    for (Element msgElement : msgElements) {
                        String code = NullU.nvl(msgElement.attributeValue("code")).trim();
                        String res = msgElement.getText();
                        if (code.length() > 0) {
                            langResMap.put(code, res);
                        }
                    }
                } finally {
                    in.close();
                }
            }

            Map<String, String> defaultLangMap = resMap.get("");
            if (defaultLangMap == null) {
                defaultLangMap = new HashMap<String, String>();
                resMap.put("", defaultLangMap);
            }

            for (String lang : resMap.keySet()) {
                Map<String, String> currentLangMap = resMap.get(lang);
                if (StringU.isBlank(lang)) {// 此处需注意
                    continue;
                }

                for (String key : defaultLangMap.keySet()) {
                    if (!currentLangMap.containsKey(key)) {
                        currentLangMap.put(key, defaultLangMap.get(key));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}
