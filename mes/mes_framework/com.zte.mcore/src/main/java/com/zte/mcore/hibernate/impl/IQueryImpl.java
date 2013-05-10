package com.zte.mcore.hibernate.impl;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;

import com.zte.mcore.hibernate.IQuery;

@SuppressWarnings("unchecked")
public class IQueryImpl implements IQuery {

    private final Query query;

    public IQueryImpl(Query query) {
        this.query = query;
    }

    @Override
    public Iterator<Object[]> iterate() {
        return query.iterate();
    }

    @Override
    public <T> List<T> list() {
        return (List<T>)query.list();
    }

    @Override
    public void executeUpdate() {
        query.executeUpdate();
    }

    @Override
    public IQuery setMaxResults(int maxResults) {
        query.setMaxResults(maxResults);
        return this;
    }

    @Override
    public IQuery setFirstResult(int firstResult) {
        query.setFirstResult(firstResult);
        return this;
    }

    @Override
    public IQuery setParameter(int position, Object val) {
        query.setParameter(position, val);
        return this;
    }

    @Override
    public IQuery setParameter(String name, Object val) {
        query.setParameter(name, val);
        return this;
    }

}
