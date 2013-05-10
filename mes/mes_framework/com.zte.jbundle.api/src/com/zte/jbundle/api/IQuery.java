package com.zte.jbundle.api;

import java.util.Iterator;
import java.util.List;

/**
 * hibernate IQuery 的facade接口
 * 
 * @author PanJun
 */
public interface IQuery {

    /**
     * Return the query results as an <tt>Iterator</tt>. If the query contains
     * multiple results pre row, the results are returned in an instance of
     * <tt>Object[]</tt>.<br>
     * <br>
     * Entities returned as results are initialized on demand. The first SQL
     * query returns identifiers only.<br>
     * 
     * @return the result iterator @
     */
    public Iterator<Object[]> iterate();

    /**
     * Return the query results as a <tt>List</tt>. If the query contains
     * multiple results pre row, the results are returned in an instance of
     * <tt>Object[]</tt>.
     * 
     * @return the result list @
     */
    public <T> List<T> list();

    /**
     * Execute the update or delete statement. </p> The semantics are compliant
     * with the ejb3 IQuery.executeUpdate() method.
     * 
     * @return The number of entities updated or deleted. @
     */
    public void executeUpdate();

    /**
     * Set the maximum number of rows to retrieve. If not set, there is no limit
     * to the number of rows retrieved.
     * 
     * @param maxResults
     *            the maximum number of rows
     */
    public IQuery setMaxResults(int maxResults);

    /**
     * Set the first row to retrieve. If not set, rows will be retrieved
     * beginnning from row <tt>0</tt>.
     * 
     * @param firstResult
     *            a row number, numbered from <tt>0</tt>
     */
    public IQuery setFirstResult(int firstResult);

    /**
     * Bind a value to a JDBC-style query parameter. The Hibernate type of the
     * parameter is first detected via the usage/position in the query and if
     * not sufficient secondly guessed from the class of the given object.
     * 
     * @param position
     *            the position of the parameter in the query string, numbered
     *            from <tt>0</tt>.
     * @param val
     *            the non-null parameter value
     */
    public IQuery setParameter(int position, Object val);

    /**
     * Bind a value to a named query parameter. The Hibernate type of the
     * parameter is first detected via the usage/position in the query and if
     * not sufficient secondly guessed from the class of the given object.
     * 
     * @param name
     *            the name of the parameter
     * @param val
     *            the non-null parameter value
     */
    public IQuery setParameter(String name, Object val);

}
