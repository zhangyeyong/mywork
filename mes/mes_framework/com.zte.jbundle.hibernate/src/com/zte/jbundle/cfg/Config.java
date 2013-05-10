package com.zte.jbundle.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.cfg.Environment;
import org.hibernate.connection.DataSourceManager;
import org.hibernate.connection.IPoolConsts;
import org.hibernate.connection.ZteC3p0ConnectionProvider;
import org.hibernate.id.ZteHiloIdGenerator;

import com.zte.jbundle.cfg.dbms.CfgDbmsType;
import com.zte.jbundle.hibernate.framework.SessionManager;
import com.zte.jbundle.hibernate.internal.MapperTracker;

public class Config implements IPoolConsts {

    private List<Map<String, String>> hibernates = new ArrayList<Map<String, String>>();
    private Long hiloSeedId = 1L;
    private String showSql = "true";
    private final static String DBMS_TYPE = "dbms.type";

    public Config() {
        Map<String, String> mySql = new LinkedHashMap<String, String>();
        mySql.put(POOL_ALIAS, "mysql");
        mySql.put(DBMS_TYPE, CfgDbmsType.mysql.name());
        mySql.put(POOL_JDBC, "jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=utf-8");
        mySql.put(POOL_USER, "-");
        mySql.put(POOL_PASSWORD, "-");
        mySql.put(POOL_COUNT, "1");
        hibernates.add(mySql);

        Map<String, String> postgres = new LinkedHashMap<String, String>();
        postgres.put(POOL_ALIAS, "postgres");
        postgres.put(DBMS_TYPE, CfgDbmsType.postgresql.name());
        postgres.put(POOL_JDBC, "jdbc:postgresql://10.5.80.75:8080/mes");
        postgres.put(POOL_USER, "-");
        postgres.put(POOL_PASSWORD, "-");
        postgres.put(POOL_COUNT, "1");
        hibernates.add(postgres);
    }

    public void valueChanged(Config oldCfg) throws Exception {
        ZteHiloIdGenerator.setSeedId(hiloSeedId);
        DataSourceManager.instance.clearAll();
        SessionManager.instance.clearAllMetas();

        for (Map<String, String> cfgMap : hibernates) {
            Map<String, String> hiberProperties = new HashMap<String, String>();
            for (String key : cfgMap.keySet()) {
                hiberProperties.put(key, cfgMap.get(key));
            }

            String strDbType = hiberProperties.get(DBMS_TYPE);
            CfgDbmsType dbType = CfgDbmsType.getDbmsType(strDbType);
            if (dbType == null) {
                throw new RuntimeException("Unsupported database type:" + strDbType + ", supported:"
                        + CfgDbmsType.SUPPORTED_DBMS_TYPES);
            }
            dbType.setHibernateConfig(hiberProperties);

            String alias = hiberProperties.get(POOL_ALIAS);
            alias = alias == null ? "" : alias;
            hiberProperties.put(Environment.CONNECTION_PROVIDER, ZteC3p0ConnectionProvider.class.getName());
            hiberProperties.put(Environment.SHOW_SQL, showSql);
            SessionManager.instance.addConfig(alias, hiberProperties);
        }

        MapperTracker.stop();
        MapperTracker.start();
    }

    public List<Map<String, String>> getHibernates() {
        return hibernates;
    }

    public void setHibernates(List<Map<String, String>> hibernates) {
        this.hibernates = hibernates;
    }

    public Long getHiloSeedId() {
        return hiloSeedId;
    }

    public void setHiloSeedId(Long hiloSeedId) {
        this.hiloSeedId = hiloSeedId;
    }

    public String getShowSql() {
        return showSql;
    }

    public void setShowSql(String showSql) {
        if ("true".equalsIgnoreCase(showSql)) {
            this.showSql = "true";
        } else {
            this.showSql = "false";
        }
    }

}
