package com.zte.jbundle.home.i18n;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.framework.Bundle;

import com.zte.jbundle.api.I18n;
import com.zte.jbundle.api.I18n.II18nable;
import com.zte.jbundle.api.Log;
import com.zte.jbundle.api.LogFactory;
import com.zte.jbundle.api.XmlNode;
import com.zte.jbundle.api.XmlParser;

public class I18nManager implements II18nable {

    static Log log = LogFactory.getLog(I18nManager.class);
    private static final String I18N_PATH = "META-INF/i18n/";
    private static final String SUFFIX_MSG_XML = ".i18n.xml";
    private final Lock lock = new ReentrantLock();

    /**
     * 资源Map,Key为语言标识，Value为资源代号和资源串映射Map
     */
    private final Map<String, Map<String, String>> msgMap = new HashMap<String, Map<String, String>>();

    public static final I18nManager instance = new I18nManager();

    private I18nManager() {
        I18n.setI18nable(this);
    }

    /**
     * 取得某一语言code对应的资源，不区分大小写
     * 
     * @param lang
     * @param code
     * @return
     */
    public String getMessage(String lang, String code) {
        lang = lang == null ? "" : lang.toLowerCase().trim();
        code = code == null ? "" : code.toLowerCase().trim();
        if (code.length() == 0) {
            return null;
        }

        lock.lock();
        try {
            Map<String, String> langResMap = msgMap.get(lang.toLowerCase());
            if (langResMap == null) {
                return null;
            }

            return langResMap.get(code);
        } finally {
            lock.unlock();
        }
    }

    public void loadBundleI18n(Bundle bundle) {
        lock.lock();
        URL url = null;
        try {
            Enumeration<?> enumRes = bundle.findEntries(I18N_PATH, "*" + SUFFIX_MSG_XML, true);
            while (enumRes != null && enumRes.hasMoreElements()) {
                url = (URL) enumRes.nextElement();
                if (url == null) {
                    continue;
                }

                String lang = getLang(url.getFile()).toLowerCase();
                Map<String, String> langResMap = msgMap.get(lang);
                if (langResMap == null) {
                    langResMap = new HashMap<String, String>();
                    msgMap.put(lang, langResMap);
                }

                XmlNode root = XmlParser.parse(url.openStream());
                List<XmlNode> resList = root.elements("msg");
                for (XmlNode elemRes : resList) {
                    String code = elemRes.attributeValue("code");
                    String res = elemRes.getText();
                    code = code == null ? "" : code.trim().toLowerCase();
                    if (code.length() == 0) {
                        continue;
                    }

                    code = code.trim().toLowerCase();
                    langResMap.put(code, res);
                }
            }
        } catch (Exception e) {
            log.error("load lang res from bundle:" + bundle.getSymbolicName() + " error,url=" + url, e);
        } finally {
            lock.unlock();
        }
    }

    private static String getLang(String path) {
        String s = path;
        int i = s.indexOf(I18N_PATH);
        if (i > -1) {
            s = s.substring(i + I18N_PATH.length());
        }

        i = s.indexOf("/");
        if (i > -1) {
            return s.substring(0, i);
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return msgMap.toString();
    }

    @Override
    public String get(String lang, String msgCode) {
        lang = lang == null ? "" : lang.toLowerCase().trim();
        msgCode = msgCode == null ? "" : msgCode.toLowerCase().trim();
        if (msgCode.length() == 0) {
            return null;
        }

        lock.lock();
        try {
            Map<String, String> langResMap = msgMap.get(lang);
            String ret = null;
            if (langResMap != null) {
                ret = langResMap.get(msgCode);
            } else {
                langResMap = msgMap.get("");
                if (langResMap != null) {
                    ret = langResMap.get(msgCode);
                }
            }

            return ret;
        } finally {
            lock.unlock();
        }
    }

}
