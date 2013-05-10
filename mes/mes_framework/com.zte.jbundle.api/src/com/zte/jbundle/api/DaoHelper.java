package com.zte.jbundle.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Hibernate Query类更高层次的封装，为业务DAO层提供开发效率更高的API
 * 
 * @author PanJun
 * 
 */
public interface DaoHelper {

    /**
     * 以给定的hql和参数列表，创建一个IQuery实例
     * 
     * @param hql
     * @param args
     * @return
     */
    public IQuery createQuery(CharSequence hql, Object... args);

    /**
     * 以给定的hql和参数列表，创建一个IQuery实例
     * 
     * @param hql
     * @param args
     * @return
     */
    public IQuery createQuery(CharSequence hql, List<? extends Object> args);

    /**
     * 获取实体的主键属性名称
     * 
     * @param entityName
     *            实体类名或实体别名
     * @return
     */
    public <M> String getIdPropName(String entityName);

    /**
     * 主键查询
     * 
     * @param id
     * @return
     */
    public <M> M getById(Class<M> clazz, Serializable id);

    /**
     * 列表查询
     * 
     * @param hql
     * @param args
     * @return
     */
    public <M> List<M> list(CharSequence hql, Object... args);

    /**
     * 列表查询
     * 
     * @param hql
     * @param args
     * @return
     */
    public <M> List<M> list(CharSequence hql, List<? extends Object> args);

    /**
     * 限制最大记录数列表查询
     * 
     * @param hql
     * @param args
     * @return
     */
    public <M> List<M> listLimited(CharSequence hql, int max, Object... args);

    /**
     * 限制最大记录数列表查询
     * 
     * @param hql
     * @param args
     * @return
     */
    public <M> List<M> listLimited(CharSequence hql, int max, List<? extends Object> args);

    /**
     * 查询符合条件记录总数
     * 
     * @param hql
     * @param args
     * @return
     */
    public int count(CharSequence hql, Object... args);

    /**
     * 查询符合条件记录总数
     * 
     * @param hql
     * @param args
     * @return
     */
    public int count(CharSequence hql, List<? extends Object> args);

    /**
     * 分页列表查询，通常与 <br>
     * public int count(String hql, Object... args) <br>
     * 结合，返回一个完整的分页对象供UI层调用
     * 
     * @param hql
     * @param first
     * @param max
     * @param args
     * @return
     */
    public <M> List<M> paginate(CharSequence hql, int first, int max, Object... args);

    /**
     * 分页列表查询，通常与 <br>
     * public int count(String hql, Object... args) <br>
     * 结合，返回一个完整的分页对象供UI层调用
     * 
     * @param hql
     * @param first
     * @param max
     * @param args
     * @return
     */
    public <M> List<M> paginate(CharSequence hql, int first, int max, List<? extends Object> args);

    /**
     * 从满足条件记录列表中，取第一条
     * 
     * @param hql
     * @param args
     * @return
     */
    public <M> M get(CharSequence hql, Object... args);

    /**
     * 从满足条件记录列表中，取第一条
     * 
     * @param hql
     * @param args
     * @return
     */
    public <M> M get(CharSequence hql, List<? extends Object> args);

    /**
     * 执行更新或插入语句的hql
     * 
     * @param hql
     * @param args
     */
    public void execute(CharSequence hql, Object... args);

    /**
     * 执行更新或插入语句的hql
     * 
     * @param hql
     * @param args
     */
    public void execute(CharSequence hql, List<? extends Object> args);

    /**
     * 更新
     * 
     * @param model
     */
    public <M> M update(M model);

    /**
     * 删除
     * 
     * @param model
     */
    public <M> void delete(M model);

    /**
     * 通过主键删除对象
     * 
     * @param clazz
     * @param id
     */
    public <M> void deleteById(Class<M> clazz, Serializable id);

    /**
     * 新增
     * 
     * @param model
     */
    public <M> M save(M model);

    /**
     * 新增
     * 
     * @param model
     */
    public <M> M saveOrUpdate(M model);

    /**
     * 清空缓存
     */
    public void flush();

    /**
     * 通过唯一性字段的多个值查询，结果放入map，下标就是唯一字段值
     * 
     * @param clazz
     * @param field
     * @param values
     * @return
     */
    public <K, M> Map<K, M> mapByField(Class<M> clazz, String field, Collection<K> values);

    /**
     * 通过唯一性字段值查询对象
     * 
     * @param clazz
     * @param fieldName
     * @param value
     * @return
     */
    public <M, V> M getByField(Class<M> clazz, String fieldName, V value);

    /**
     * 通过多个Id查询
     * 
     * @param clazz
     * @param ids
     * @return
     */
    public <M> Map<Long, M> mapByIds(Class<M> clazz, Collection<Long> ids);

    /**
     * 通过某个字段值查询对象
     * 
     * @param clazz
     * @param field
     * @param values
     * @return
     */
    public <K, M> List<M> listByField(Class<M> clazz, String field, Collection<K> values);

}
