package com.zte.mcore.hibernate.impl;

import java.io.Serializable;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

import com.zte.mcore.hibernate.DaoHelper;
import com.zte.mcore.hibernate.IQuery;

@SuppressWarnings("unchecked")
public class DaoHelperImpl implements DaoHelper {

    private final String alias;
    private static Map<String, DaoHelper> instances = new HashMap<String, DaoHelper>();

    public static DaoHelper getInstance(String alias) {
        alias = alias == null ? "" : alias.trim();
        DaoHelper ret = instances.get(alias);
        if (ret != null) {
            return ret;
        }

        synchronized (instances) {
            ret = instances.get(alias);
            if (ret != null) {
                return ret;
            }
            final DaoHelper instance = new DaoHelperImpl(alias);
            instances.put(alias, instance);
            return instance;
        }
    }

    private DaoHelperImpl(String alias) {
        this.alias = alias;
    }

    Session getSession() {
        return SessionManager.instance.getSession(alias);
    }

    @Override
    public IQuery createQuery(CharSequence hql, Object... args) {
        IQuery query = new IQueryImpl(getSession().createQuery(hql.toString()));
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i, args[i]);
        }
        return query;
    }

    @Override
    public IQuery createQuery(CharSequence hql, List<Object> args) {
        IQuery query = new IQueryImpl(getSession().createQuery(hql.toString()));
        int i = 0;
        for (Object arg : args) {
            query.setParameter(i++, arg);
        }
        return query;
    }

    @Override
    public IQuery createSqlQuery(CharSequence sql) {
        IQuery query = new IQueryImpl(getSession().createSQLQuery(sql.toString()));
        return query;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Connection connection() {
        return getSession().connection();
    }

    @Override
    public <M> M getById(Class<M> clazz, Serializable id) {
        return (M) getSession().get(clazz, id);
    }

    @Override
    public <M> List<M> list(CharSequence hql, Object... args) {
        return createQuery(hql, args).list();
    }

    @Override
    public <M> List<M> list(CharSequence hql, List<Object> args) {
        return createQuery(hql, args).list();
    }

    private static boolean isAlpha(char c) {
        return c == '_' || ('0' <= c && c <= '9') || ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    private static boolean followWithWord(String s, String sub, int pos) {
        int i = 0;
        for (int subLen = sub.length(), len = s.length(); pos < len && i < subLen; pos++, i++) {
            char c1 = s.charAt(pos);
            char c2 = s.charAt(i);
            if (c1 != c2 && Character.toUpperCase(c1) != Character.toUpperCase(c2)) {
                return false;
            }
        }

        if (i < sub.length()) {
            return false;
        }

        if (pos >= s.length()) {
            return true;
        } else {
            return !isAlpha(s.charAt(pos));
        }
    }

    /** 解析select类型的hql/sql, 生成 select count(1) from xxx形式 */
    private static String parseSelectCount(String hql) {
        int noBlankStart = 0;
        for (int len = hql.length(); noBlankStart < len; noBlankStart++) {
            if (hql.charAt(noBlankStart) > ' ') {
                break;
            }
        }

        int pair = 0;
        // 如果hql直接以from开始，默认前面有select关键字
        if (!followWithWord(hql, "select", noBlankStart))
            pair = 1;

        int fromPos = -1;
        for (int i = noBlankStart; i < hql.length();) {
            if (followWithWord(hql, "select", i)) {
                pair++;
                i += "select".length();
                continue;
            }

            if (followWithWord(hql, "from", i)) {
                pair--;
                if (pair == 0) {
                    fromPos = i;
                    break;
                } else {
                    i += "from".length();
                }
                continue;
            }

            i++;
        }

        if (fromPos == -1) {
            throw new IllegalArgumentException("parse count sql error, check your sql/hql");
        }

        String countHql = "select count(*) " + hql.substring(fromPos);
        return countHql;
    }

    @Override
    public <M> List<M> listLimited(CharSequence hql, int max, Object... args) {
        return createQuery(hql, args).setMaxResults(max).list();
    }

    @Override
    public <M> List<M> listLimited(CharSequence hql, int max, List<Object> args) {
        return createQuery(hql, args).setMaxResults(max).list();
    }

    @Override
    public int count(CharSequence hql, Object... args) {
        Object o = get(parseSelectCount(hql.toString()), args);
        return Integer.parseInt(o.toString());
    }

    @Override
    public int count(CharSequence hql, List<Object> args) {
        Object o = get(parseSelectCount(hql.toString()), args);
        return Integer.parseInt(o.toString());
    }

    @Override
    public <M> List<M> paginate(CharSequence hql, int first, int max, Object... args) {
        return createQuery(hql, args).setFirstResult(first).setMaxResults(max).list();
    }

    @Override
    public <M> List<M> paginate(CharSequence hql, int first, int max, List<Object> args) {
        return createQuery(hql, args).setFirstResult(first).setMaxResults(max).list();
    }

    @Override
    public <M> M get(CharSequence hql, Object... args) {
        List<M> list = createQuery(hql, args).setMaxResults(1).list();
        return list.size() == 0 ? null : list.get(0);
    }

    @Override
    public <M> M get(CharSequence hql, List<Object> args) {
        List<M> list = createQuery(hql, args).setMaxResults(1).list();
        return list.size() == 0 ? null : list.get(0);
    }

    @Override
    public void execute(CharSequence hql, Object... args) {
        createQuery(hql, args).executeUpdate();
    }

    @Override
    public void execute(CharSequence hql, List<Object> args) {
        createQuery(hql, args).executeUpdate();
    }

    @Override
    public <M> M update(M model) {
        getSession().update(model);
        return model;
    }

    @Override
    public <M> void delete(M model) {
        getSession().delete(model);
    }

    @Override
    public <M> M save(M model) {
        getSession().save(model);
        return model;
    }

    @Override
    public <M> M saveOrUpdate(M model) {
        getSession().saveOrUpdate(model);
        return model;
    }

    @Override
    public void flush() {
        getSession().flush();
    }

    @Override
    public String getIdPropertyName(String entityName) {
        Configuration cfg = SessionManager.instance.getConfiguration(alias);
        return cfg.getClassMapping(entityName).getIdentifierProperty().getName();
    }

}
