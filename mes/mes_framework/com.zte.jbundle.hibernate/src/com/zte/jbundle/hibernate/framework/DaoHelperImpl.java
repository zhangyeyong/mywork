package com.zte.jbundle.hibernate.framework;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

import com.zte.jbundle.api.DaoHelper;
import com.zte.jbundle.api.IQuery;

@SuppressWarnings("unchecked")
public class DaoHelperImpl implements DaoHelper {

    private final String alias;

    public DaoHelperImpl(String alias) {
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
    public IQuery createQuery(CharSequence hql, List<? extends Object> args) {
        IQuery query = new IQueryImpl(getSession().createQuery(hql.toString()));
        int i = 0;
        for (Object arg : args) {
            query.setParameter(i++, arg);
        }
        return query;
    }

    @Override
    public <M> M getById(Class<M> clazz, Serializable id) {
        if (id == null) {
            return null;
        }
        return (M) getSession().get(clazz, id);
    }

    static Field getBeanFieldByName(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    @Override
    public <K, M> Map<K, M> mapByField(Class<M> clazz, String field, Collection<K> values) {
        List<M> list = listByField(clazz, field, values);
        Field beanField = getBeanFieldByName(clazz, field);
        beanField.setAccessible(true);

        Map<K, M> ret = new HashMap<K, M>();
        try {
            for (M m : list) {
                Object key = beanField.get(m);
                ret.put((K) key, m);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ret;
    }

    @Override
    public <M, V> M getByField(Class<M> clazz, String fieldName, V value) {
        String hql = "from " + clazz.getName() + " where " + fieldName + "=?";
        return get(hql, value);
    }

    @Override
    public <M> Map<Long, M> mapByIds(Class<M> clazz, Collection<Long> ids) {
        String idFieldName = getIdPropName(clazz.getName());
        return mapByField(clazz, idFieldName, ids);
    }

    @Override
    public <K, M> List<M> listByField(Class<M> clazz, String field, Collection<K> values) {
        List<M> ret = new ArrayList<M>();

        String hql = "from " + clazz.getName() + " where " + field + " in (?,?,?,?,?,?,?,?,?,?)";
        IQuery query = createQuery(hql);
        Set<Object> valueSet = new HashSet<Object>();
        for (Object val : values) {
            if (val != null) {
                valueSet.add(val);
            }
        }

        LinkedList<Object> valueList = new LinkedList<Object>(valueSet);
        try {
            while (!valueList.isEmpty()) {
                int count = 0;

                Object firstValue = valueList.getFirst();
                for (; count < 10 && !valueList.isEmpty(); count++) {
                    query.setParameter(count, valueList.removeFirst());
                }

                // 如果10个参数剩下部分没有填充，用第一个参数值填充
                for (; count < 10; count++) {
                    query.setParameter(count, firstValue);
                }

                List<M> batchModels = query.list();
                ret.addAll(batchModels);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ret;
    }

    @Override
    public <M> List<M> list(CharSequence hql, Object... args) {
        return createQuery(hql, args).list();
    }

    @Override
    public <M> List<M> list(CharSequence hql, List<? extends Object> args) {
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
    public <M> List<M> listLimited(CharSequence hql, int max, List<? extends Object> args) {
        return createQuery(hql, args).setMaxResults(max).list();
    }

    @Override
    public int count(CharSequence hql, Object... args) {
        Object o = get(parseSelectCount(hql.toString()), args);
        return Integer.parseInt(o.toString());
    }

    @Override
    public int count(CharSequence hql, List<? extends Object> args) {
        Object o = get(parseSelectCount(hql.toString()), args);
        return Integer.parseInt(o.toString());
    }

    @Override
    public <M> List<M> paginate(CharSequence hql, int first, int max, Object... args) {
        return createQuery(hql, args).setFirstResult(first).setMaxResults(max).list();
    }

    @Override
    public <M> List<M> paginate(CharSequence hql, int first, int max, List<? extends Object> args) {
        return createQuery(hql, args).setFirstResult(first).setMaxResults(max).list();
    }

    @Override
    public <M> M get(CharSequence hql, Object... args) {
        List<M> list = createQuery(hql, args).setMaxResults(1).list();
        return list.size() == 0 ? null : list.get(0);
    }

    @Override
    public <M> M get(CharSequence hql, List<? extends Object> args) {
        List<M> list = createQuery(hql, args).setMaxResults(1).list();
        return list.size() == 0 ? null : list.get(0);
    }

    @Override
    public void execute(CharSequence hql, Object... args) {
        createQuery(hql, args).executeUpdate();
    }

    @Override
    public void execute(CharSequence hql, List<? extends Object> args) {
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
    public <M> void deleteById(Class<M> clazz, Serializable id) {
        if (id != null && clazz != null) {
            String idField = getIdPropName(clazz.getName());
            String hql = "delete from " + clazz.getName() + " where " + idField + "=?";
            createQuery(hql, id).executeUpdate();
        }
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
    public <M> String getIdPropName(String entityName) {
        Configuration cfg = SessionManager.instance.getSessionMeta(alias).getCfg();
        return cfg.getClassMapping(entityName).getIdentifierProperty().getName();
    }

}
