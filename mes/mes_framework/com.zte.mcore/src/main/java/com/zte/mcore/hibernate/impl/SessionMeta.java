package com.zte.mcore.hibernate.impl;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * 创建 hibernate session所需的元数据
 * 
 * @author PanJun
 * 
 */
public class SessionMeta {

    /** 多数据库时，一个数据库连接的别名 */
    private String alias;
    private String driver;
    private String url;
    private Map<String, String> properties = new HashMap<String, String>();
    private SessionFactory factory;
    private Configuration cfg;

    public synchronized void reset() {
        factory = null;
    }

    public synchronized SessionFactory getSessionFactory() {
        if (factory != null && cfg != null) {
            return factory;
        }

        cfg = new Configuration();
        for (String k : properties.keySet()) {
            cfg.setProperty(k, properties.get(k));
        }

        for (String hbm : MapperManager.allMappers()) {
            cfg.addXML(hbm);
        }
        final SessionFactory factory = cfg.buildSessionFactory();
        this.factory = factory;
        return factory;
    }

    public Configuration getConfiguration() {
        getSessionFactory();
        return cfg;
    }

    public String getAlias() {
        return alias;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
