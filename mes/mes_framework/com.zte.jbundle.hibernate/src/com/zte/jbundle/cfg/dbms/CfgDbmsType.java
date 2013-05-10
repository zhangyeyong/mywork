package com.zte.jbundle.cfg.dbms;

import java.util.Map;

import org.hibernate.cfg.Environment;
import org.hibernate.connection.IPoolConsts;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.PostgresPlusDialect;

/**
 * 可选配置数据库类型
 * 
 * @author PanJun
 * 
 */
public enum CfgDbmsType {

    mysql {

        @Override
        String getJdbcDriver() {
            return com.mysql.jdbc.Driver.class.getName();
        }

        @Override
        String getDialect() {
            return MySQLDialect.class.getName();
        }

    },

    postgresql {

        @Override
        String getJdbcDriver() {
            return org.postgresql.Driver.class.getName();
        }

        @Override
        String getDialect() {
            return PostgresPlusDialect.class.getName();
        }

        @Override
        String getTestSql() {
            return "select 1";
        }
    };

    abstract String getJdbcDriver();

    abstract String getDialect();

    public static final String SUPPORTED_DBMS_TYPES = getSupportedDbmsTypes();

    String getTestSql() {
        return "select 1 from dual";
    }

    private static String getSupportedDbmsTypes() {
        StringBuilder sbRet = new StringBuilder();
        for (CfgDbmsType ret : CfgDbmsType.values()) {
            if (sbRet.length() > 0) {
                sbRet.append(",");
            }
            sbRet.append(ret.name());
        }
        return sbRet.toString();
    }

    private static CfgDbmsType[] types = values();

    public static CfgDbmsType getDbmsType(String dbmsName) {
        for (CfgDbmsType ret : types) {
            if (ret.name().equalsIgnoreCase(dbmsName)) {
                return ret;
            }
        }
        return null;
    }

    public void setHibernateConfig(Map<String, String> hiberProperties) {
        hiberProperties.put(Environment.DIALECT, getDialect());
        hiberProperties.put(IPoolConsts.POOL_DRIVER, getJdbcDriver());
        hiberProperties.put(IPoolConsts.POOL_TEST_SQL, getTestSql());
    }
}
