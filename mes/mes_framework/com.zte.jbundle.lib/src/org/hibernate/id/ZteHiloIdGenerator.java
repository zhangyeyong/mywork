package org.hibernate.id;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.exception.JDBCExceptionHelper;
import org.hibernate.mapping.Table;
import org.hibernate.type.Type;
import org.hibernate.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 步长、高地位Id产生器，适合主键为Long类型
 * 
 * @author PanJun
 * 
 */
public class ZteHiloIdGenerator implements IdentifierGenerator, Configurable {

    private static final Logger log = LoggerFactory.getLogger(IncrementGenerator.class);
    private String sql;

    private static long step = 1000;

    private static long seedId = 1;

    private long hiNext;

    public synchronized Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        if (sql != null) {
            initHiId(session);
        }
        hiNext++;
        return hiNext * step + seedId;
    }

    public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
        ObjectNameNormalizer normalizer = (ObjectNameNormalizer) params.get("identifier_normalizer");

        String column = params.getProperty("column");
        if (column == null) {
            column = params.getProperty("target_column");
        }
        column = dialect.quote(normalizer.normalizeIdentifierQuoting(column));

        String tableList = params.getProperty("tables");
        if (tableList == null) {
            tableList = params.getProperty("identity_tables");
        }
        String[] tables = StringHelper.split(", ", tableList);

        String schema = dialect.quote(normalizer.normalizeIdentifierQuoting(params.getProperty("schema")));
        String catalog = dialect.quote(normalizer.normalizeIdentifierQuoting(params.getProperty("catalog")));

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < tables.length; i++) {
            String tableName = dialect.quote(normalizer.normalizeIdentifierQuoting(tables[i]));
            if (tables.length > 1) {
                buf.append("select ").append(column).append(" from ");
            }
            buf.append(Table.qualify(catalog, schema, tableName));
            if (i < tables.length - 1) {
                buf.append(" union ");
            }
        }
        if (tables.length > 1) {
            buf.insert(0, "( ").append(" ) ids_");
            column = "ids_." + column;
        }

        this.sql = ("select max(" + column + ") from " + buf.toString());
    }

    private void initHiId(SessionImplementor session) {
        log.debug("fetching initial value: " + this.sql);
        try {
            PreparedStatement st = session.getBatcher().prepareSelectStatement(this.sql);
            try {
                ResultSet rs = st.executeQuery();
                try {
                    long maxValue = 0;
                    if (rs.next()) {
                        maxValue = rs.getLong(1);
                    }
                    hiNext = maxValue / 1000L;
                    sql = null;
                } finally {
                    rs.close();
                }
            } finally {
                session.getBatcher().closeStatement(st);
            }
        } catch (SQLException e) {
            throw JDBCExceptionHelper.convert(session.getFactory().getSQLExceptionConverter(), e,
                    "could not fetch initial value for ZTE HiLo id generator", this.sql);
        }
    }

    public static void setSeedId(long nodeId) {
        seedId = nodeId;
    }

}