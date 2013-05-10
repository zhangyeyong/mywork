package com.zte.jbundle.cfg;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import com.zte.jbundle.api.I18n;
import com.zte.jbundle.api.LogFactory;
import com.zte.jbundle.api.I18n.LangEnv;
import com.zte.jbundle.home.utils.JBundleUitls;
import com.zte.jbundle.log.DailyAppender;

/**
 * jBundle系统级配置
 * 
 * @author PanJun
 * 
 */
public class HomeCfg {

    Map<String, String> log4j = new LinkedHashMap<String, String>();
    private String username = "admin";
    private String password = "jbundle";
    private LangEnv[] langEnvs = { new LangEnv("zh", "中文"), new LangEnv("en_US", "English"),
            new LangEnv("es", "Español") };

    private static HomeCfg instance = null;
    private static boolean ignoreNextChange = false;

    public static HomeCfg getInstance() {
        return instance;
    };

    public HomeCfg() {
        instance = this;
        log4j.put("log4j.rootLogger", "WARN,stdout_r,file_r");
        log4j.put("log4j.appender.stdout_r", "org.apache.log4j.ConsoleAppender");
        log4j.put("log4j.appender.stdout_r.encoding", "UTF-8");
        log4j.put("log4j.appender.stdout_r.layout", "org.apache.log4j.PatternLayout");
        log4j.put("log4j.appender.stdout_r.layout.ConversionPattern", "%-5p %-d{HH:mm:ss} %m%n");

        log4j.put("log4j.appender.file_r", DailyAppender.class.getName());
        log4j.put("log4j.appender.file_r.encoding", "UTF-8");
        log4j.put("log4j.appender.file_r.File", "${logFolder}/jbundle.log");
        log4j.put("log4j.appender.file_r.maxCount", "30");
        log4j.put("log4j.appender.file_r.layout", "org.apache.log4j.PatternLayout");
        log4j.put("log4j.appender.file_r.layout.ConversionPattern", "%-5p %-d{HH:mm:ss} %m%n");
    }

    /**
     * 配置改变时，修改log4j配置
     * 
     * @param oldCfg
     */
    public void valueChanged(HomeCfg oldCfg) {
        // 设置i18n的语言列表
        I18n.setLangEnvList(langEnvs == null ? null : Arrays.asList(langEnvs));

        if (ignoreNextChange) {
            ignoreNextChange = false;
            return;
        }

        String logFolder = JBundleUitls.getLogFolder().getAbsolutePath();
        Properties p = new Properties();
        for (String key : log4j.keySet()) {
            String value = log4j.get(key);
            for (int i; (i = value.indexOf("${logFolder}")) > -1;) {
                value = value.substring(0, i) + logFolder + value.substring(i + "${logFolder}".length());
            }
            p.put(key, value);
        }

        LogManager.shutdown();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(DailyAppender.class.getClassLoader());
            PropertyConfigurator.configure(p);
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }

        LogFactory.getLog(getClass()).warn("[^_^] Log4j configuration changed");
    }

    public Map<String, String> getLog4j() {
        return log4j;
    }

    public void setLog4j(Map<String, String> log4j) {
        this.log4j = log4j;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static void setIgnoreNextChange(boolean ignoreNextChange) {
        HomeCfg.ignoreNextChange = ignoreNextChange;
    }

    public LangEnv[] getLangEnvs() {
        return langEnvs;
    }

    public void setLangEnvs(LangEnv[] langEnvs) {
        this.langEnvs = langEnvs;
    }

}
