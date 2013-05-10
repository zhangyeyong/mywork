package com.zte.mcore.hibernate.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final public class SessionManager {

    public static final SessionManager instance = new SessionManager();

    private SessionManager() {
        // Singleton
    }

    private List<SessionMeta> metaList = new ArrayList<SessionMeta>();
    static Logger log = LoggerFactory.getLogger(SessionManager.class);

    static class SessionHolder {
        Transaction tx;
        Session session;
        String alias;
    }

    /** 当前线程所用到的session */
    static ThreadLocal<List<SessionHolder>> sessions = new ThreadLocal<List<SessionHolder>>() {
        protected List<SessionHolder> initialValue() {
            return new ArrayList<SessionHolder>();
        }
    };

    /**
     * 一次初始化所有的SessionMeta
     * 
     * @param sessionMetas
     */
    public synchronized void initSessionMetas(List<SessionMeta> sessionMetas) {
        this.metaList.clear();
        this.metaList.addAll(sessionMetas);
    }

    /**
     * 别名相同session元数据
     * 
     * @param alias
     * @return
     */
    private SessionMeta getMeta(String alias) {
        for (SessionMeta meta : metaList) {
            if (HiberUtils.equalsAlias(meta.getAlias(), alias)) {
                return meta;
            }
        }

        return null;
    }

    public Session getSession(String alias) {
        for (SessionHolder holder : sessions.get()) {
            if (HiberUtils.equalsAlias(holder.alias, alias)) {
                return holder.session;
            }
        }

        SessionMeta meta = getMeta(alias);
        if (meta == null) {
            throw new HibernateException("[!_!]Mcore failed to get hibernate (alias=" + alias + ") session!");
        }

        SessionHolder holder = new SessionHolder();
        holder.session = meta.getSessionFactory().openSession();
        holder.alias = alias;
        holder.tx = holder.session.beginTransaction();
        sessions.get().add(holder);
        return holder.session;
    }

    public Configuration getConfiguration(String alias) {
        SessionMeta meta = getMeta(alias);
        return meta.getConfiguration();
    }

    public void commitTransactions() {
        List<SessionHolder> holders = sessions.get();
        for (int i = holders.size() - 1; i > -1; i--) {
            SessionHolder holder = holders.remove(i);
            try {
                try {
                    holder.tx.commit();
                } finally {
                    holder.session.close();
                }
            } catch (Exception e) {
                log.error("[!_!]Failed to commit transaction:" + holder.alias, e);
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
                    holder.session.close();
                }
            } catch (Exception e) {
                log.error("[!_!]Failed to rollback transaction:" + holder.alias, e);
            }
        }
    }

}
