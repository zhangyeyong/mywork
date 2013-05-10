package com.zte.jbundle.hibernate.framework;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zte.jbundle.api.DaoHelper;
import com.zte.jbundle.hibernate.internal.Activator;

@SuppressWarnings("all")
final public class SessionManager {

    private static final String FILTER_JBUNDLE_ID = "jbundleId";
    public static final SessionManager instance = new SessionManager();

    private SessionManager() {
        // Singleton
    }

    private List<SessionMeta> metaList = new ArrayList<SessionMeta>();
    static Logger log = LoggerFactory.getLogger(SessionManager.class);

    static class SessionHolder {
        Transaction tx;
        Session session;
        boolean isDefault = false;
        String alias;
        private static AtomicLong ID_SEED = new AtomicLong(1);
        long innerId = ID_SEED.getAndIncrement();
    }

    /** 当前线程所用到的session */
    static ThreadLocal<List<SessionHolder>> sessions = new ThreadLocal<List<SessionHolder>>() {
        protected List<SessionHolder> initialValue() {
            return new ArrayList<SessionHolder>();
        }
    };

    /**
     * 别名相同session元数据，如果没有去第一个session元数据，作为缺省值返回
     * 
     * @param alias
     * @return
     */
    private SessionMeta getMeta(String alias) {
        for (SessionMeta wrapper : metaList) {
            if (HiberUtils.equalsAlias(wrapper.getAlias(), alias)) {
                return wrapper;
            }
        }

        return metaList.isEmpty() ? null : metaList.get(0);
    }

    public Session getSession(String alias) {
        Session result = null;
        for (SessionHolder holder : sessions.get()) {
            if (HiberUtils.equalsAlias(holder.alias, alias)) {
                return holder.session;
            }
            if (holder.isDefault) {
                result = holder.session;
            }
        }
        if (result != null) {
            return result;
        }

        SessionMeta meta = getMeta(alias);
        if (meta == null) {
            throw new HibernateException("[!_!]JBundle failed to get hibernate session!");
        }

        SessionHolder holder = new SessionHolder();
        holder.session = meta.getSessionFactory().openSession();
        holder.alias = alias;
        holder.tx = holder.session.beginTransaction();
        sessions.get().add(holder);
        log.info("---open connnection:" + holder.innerId);
        return holder.session;
    }

    public void commitTransactions() {
        List<SessionHolder> holders = sessions.get();
        for (int i = holders.size() - 1; i > -1; i--) {
            SessionHolder holder = holders.remove(i);
            try {
                try {
                    holder.tx.commit();
                } finally {
                    log.info("---closing connnection:" + holder.innerId);
                    holder.session.close();
                }
            } catch (Exception e) {
                log.error("[!_!]JBundle failed to commit transaction:" + holder.alias, e);
            }
        }
    }

    public void rollbackTransactions() {
        List<SessionHolder> holders = sessions.get();
        for (int i = holders.size() - 1; i > -1; i--) {
            SessionHolder holder = holders.remove(i);
            try {
                try {
                    holder.tx.rollback();
                } finally {
                    log.info("---closing connnection:" + holder.innerId);
                    holder.session.close();
                }
            } catch (Exception e) {
                log.error("[!_!]JBundle failed to rollback transaction:" + holder.alias, e);
            }
        }
    }

    public SessionMeta getSessionMeta(String alias) {
        for (SessionMeta wrapper : metaList) {
            if (HiberUtils.equalsAlias(wrapper.getAlias(), alias)) {
                return wrapper;
            }
        }
        return null;
    }

    public synchronized void clearAllMetas() {
        for (int i = metaList.size() - 1; i > -1; i--) {
            SessionMeta meta = metaList.remove(i);
            if (meta.getDaoHelperReg() != null) {
                meta.getDaoHelperReg().unregister();
            }
        }
    }

    public synchronized void addConfig(String alias, Map<String, String> cfgMap) {
        SessionMeta meta = getSessionMeta(alias);
        if (meta == null) {
            meta = new SessionMeta(alias);
            metaList.add(meta);
        }
        meta.setHibernateCfg(cfgMap);
    }

    public synchronized void publishDaoHelpers() {
        for (int i = 0; i < metaList.size(); i++) {
            SessionMeta meta = metaList.get(i);
            DaoHelperImpl dao = new DaoHelperImpl(meta.getAlias());
            Dictionary<String, String> dict = new Hashtable<String, String>();
            dict.put(FILTER_JBUNDLE_ID, meta.getAlias());

            meta.setDaoHelper(dao);
            meta.setDaoHelperReg(Activator.getContext().registerService(DaoHelper.class.getName(), dao, dict));
        }
    }
}
