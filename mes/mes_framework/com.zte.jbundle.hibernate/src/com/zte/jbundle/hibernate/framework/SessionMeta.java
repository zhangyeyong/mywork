package com.zte.jbundle.hibernate.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.osgi.framework.ServiceRegistration;

import com.zte.jbundle.api.DaoHelper;

/**
 * 创建 hibernate session所需的元数据
 * 
 * @author PanJun
 * 
 */
@SuppressWarnings("all")
public class SessionMeta {

    /** 多数据库时，一个数据库连接的别名 */
    final private String alias;
    private SessionFactory factory;
    private Map<String, String> hiberCfgMap = new HashMap<String, String>();
    private AtomicBoolean changed = new AtomicBoolean(true);
    private DaoHelper daoHelper;
    private ServiceRegistration daoHelperReg;
    private Configuration cfg;

    public SessionMeta(String alias) {
        this.alias = alias == null ? "" : alias.trim();
    }

    public synchronized void setHibernateCfg(Map<String, String> cfgMap) {
        hiberCfgMap.clear();
        hiberCfgMap.putAll(cfgMap);
        changed.set(true);
    }

    public SessionFactory getSessionFactory() {
        if (!MapperManager.instance.isChanged() && !changed.get() && factory != null) {
            return factory;
        }

        synchronized (this) {
            cfg = new Configuration();
            for (String k : hiberCfgMap.keySet()) {
                cfg.setProperty(k, hiberCfgMap.get(k));
            }

            for (MapperMeta mapper : MapperManager.instance.getAllMetas()) {
                cfg.addXML(mapper.xml);
            }
            factory = cfg.buildSessionFactory();
            MapperManager.instance.finishChange();
            changed.set(false);
            return factory;
        }
    }

    public String getAlias() {
        return alias;
    }

    public DaoHelper getDaoHelper() {
        return daoHelper;
    }

    public void setDaoHelper(DaoHelper daoHelper) {
        this.daoHelper = daoHelper;
    }

    public ServiceRegistration getDaoHelperReg() {
        return daoHelperReg;
    }

    public void setDaoHelperReg(ServiceRegistration daoHelperReg) {
        this.daoHelperReg = daoHelperReg;
    }

    public Configuration getCfg() {
        return cfg;
    }

}
