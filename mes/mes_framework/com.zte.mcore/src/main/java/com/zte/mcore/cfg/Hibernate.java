package com.zte.mcore.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.cfg.Environment;
import org.hibernate.dialect.MySQLDialect;
import org.logicalcobwebs.proxool.ProxoolFacade;

import com.zte.mcore.hibernate.impl.ProxoolConnectionProvider;
import com.zte.mcore.hibernate.impl.SessionManager;
import com.zte.mcore.hibernate.impl.SessionMeta;
import com.zte.mcore.hibernate.impl.ZteHiloIdGenerator;
import com.zte.mcore.utils.NullU;

public class Hibernate {

    private List<Map<String, String>> connectionPools = new ArrayList<Map<String, String>>();
    private Long hiloSeedId = 1L;

    public Hibernate() {
        Map<String, String> mysqlMap = new LinkedHashMap<String, String>();
        mysqlMap.put(Environment.DIALECT, MySQLDialect.class.getName());
        mysqlMap.put(Environment.SHOW_SQL, "true");
        mysqlMap.put("proxool.alias", "");
        mysqlMap.put("proxool.driver", "com.mysql.jdbc.Driver");
        mysqlMap.put("proxool.url", "jdbc:mysql://10.17.82.33:3306/mes?useUnicode=true&characterEncoding=utf-8");
        mysqlMap.put("user", "root");
        mysqlMap.put("password", "mesdb");
        mysqlMap.put("proxool.minimum-connection-count", "1");
        mysqlMap.put("proxool.maximum-connection-count", "2");
        mysqlMap.put("proxool.prototype-count", "1");
        mysqlMap.put("proxool.house-keeping-test-sql", "select 1 from dual");
        mysqlMap.put("proxool.test-before-use", "true");
        mysqlMap.put("proxool.test-after-user", "false");
        connectionPools.add(mysqlMap);
    }

    public void valueChanged(Hibernate oldCfg) throws Exception {
        ZteHiloIdGenerator.setSeedId(hiloSeedId);
        Set<String> existedAliases = new HashSet<String>();
        for (String a : ProxoolFacade.getAliases()) {
            existedAliases.add("proxool." + a);
        }

        List<SessionMeta> sessionMetas = new ArrayList<SessionMeta>();
        for (Map<String, String> cfgMap : connectionPools) {
            SessionMeta meta = new SessionMeta();
            for (String key : cfgMap.keySet()) {
                String value = NullU.nvl(cfgMap.get(key)).trim();
                if ("proxool.alias".equals(key)) {
                    if (value.startsWith("proxool.")) {
                        value = value.substring("proxool.".length());
                    }
                    meta.setAlias(value);
                } else if ("proxool.driver".equals(key)) {
                    meta.setDriver(value);
                } else if ("proxool.url".equals(key)) {
                    meta.setUrl(value);
                } else {
                    meta.getProperties().put(key, value);
                }
            }

            sessionMetas.add(meta);
        }

        for (String alias : existedAliases) {
            ProxoolFacade.removeConnectionPool(alias.substring("proxool.".length()), 0);
        }

        for (SessionMeta meta : sessionMetas) {
            String alias = meta.getAlias().length() > 0 ? meta.getAlias() : "default";
            alias = "proxool." + alias;
            meta.getProperties().put(Environment.CONNECTION_PROVIDER, ProxoolConnectionProvider.class.getName());
            meta.getProperties().put(Environment.PROXOOL_POOL_ALIAS, alias);

            String url = alias + ":" + meta.getDriver() + ":" + meta.getUrl();
            Properties p = new Properties();
            for (String k : meta.getProperties().keySet()) {
                p.put(k, meta.getProperties().get(k));
            }
            ProxoolFacade.registerConnectionPool(url, p);
        }

        SessionManager.instance.initSessionMetas(sessionMetas);
    }

    public List<Map<String, String>> getConnectionPools() {
        return connectionPools;
    }

    public void setConnectionPools(List<Map<String, String>> hibernates) {
        this.connectionPools = hibernates;
    }

    public Long getHiloSeedId() {
        return hiloSeedId;
    }

    public void setHiloSeedId(Long hiloSeedId) {
        this.hiloSeedId = hiloSeedId;
    }

}
