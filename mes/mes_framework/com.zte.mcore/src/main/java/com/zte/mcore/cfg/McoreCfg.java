package com.zte.mcore.cfg;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import com.zte.mcore.i18n.I18n;
import com.zte.mcore.log.DailyAppender;
import com.zte.mcore.utils.McoreU;

/**
 * jBundle系统级配置
 * 
 * @author PanJun
 * 
 */
public class McoreCfg {

    Map<String, String> log4j = new LinkedHashMap<String, String>();
    private String username = "admin";
    private String password = "admin";
    private String defaultLang = "";// 默认语言

    private static McoreCfg instance = null;
    private static boolean ignoreNextChange = false;

    public static McoreCfg getInstance() {
        return instance;
    };

    public McoreCfg() {
        instance = this;
        log4j.put("log4j.rootLogger", "WARN,stdout_r,file_r");
        log4j.put("log4j.appender.stdout_r", "org.apache.log4j.ConsoleAppender");
        log4j.put("log4j.appender.stdout_r.encoding", "UTF-8");
        log4j.put("log4j.appender.stdout_r.layout", "org.apache.log4j.PatternLayout");
        log4j.put("log4j.appender.stdout_r.layout.ConversionPattern", "%-5p %-d{HH:mm:ss} %m%n");

        log4j.put("log4j.appender.file_r", DailyAppender.class.getName());
        log4j.put("log4j.appender.file_r.encoding", "UTF-8");
        log4j.put("log4j.appender.file_r.File", "${logFolder}/mcore.log");
        log4j.put("log4j.appender.file_r.maxCount", "30");
        log4j.put("log4j.appender.file_r.layout", "org.apache.log4j.PatternLayout");
        log4j.put("log4j.appender.file_r.layout.ConversionPattern", "%-5p %-d{HH:mm:ss} %m%n");
    }

    /**
     * 配置改变时，修改log4j配置
     * 
     * @param oldCfg
     */
    public void valueChanged(McoreCfg oldCfg) {
        if (ignoreNextChange) {
            ignoreNextChange = false;
            return;
        }

        String logFolder = McoreU.getLogFolder().getAbsolutePath();
        Properties p = new Properties();
        for (String key : log4j.keySet()) {
            String value = log4j.get(key);
            for (int i; (i = value.indexOf("${logFolder}")) > -1;) {
                value = value.substring(0, i) + logFolder + value.substring(i + "${logFolder}".length());
            }
            p.put(key, value);
        }

        LogManager.shutdown();
        PropertyConfigurator.configure(p);
        LogFactory.getLog(getClass()).warn("[^_^] Log4j configuration changed");

        I18n.setDefaultLang(defaultLang);
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
        McoreCfg.ignoreNextChange = ignoreNextChange;
    }

    public String getDefaultLang() {
        return defaultLang;
    }

    public void setDefaultLang(String defaultLang) {
        this.defaultLang = defaultLang;
    }

}
