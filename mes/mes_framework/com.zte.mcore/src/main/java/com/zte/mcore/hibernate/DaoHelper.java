package com.zte.mcore.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;

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
    public IQuery createQuery(CharSequence hql, List<Object> args);

    /**
     * 以给定的原生sql和参数列表，创建一个IQuery实例<br>
     * <b>多数情况请不要使用此方法</b>
     * 
     * @param hql
     * @param args
     * @return
     */
    public IQuery createSqlQuery(CharSequence sql);

    /**
     * 获取数据库连接
     * 
     * @return
     */
    public Connection connection();

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
    public <M> List<M> list(CharSequence hql, List<Object> args);

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
    public <M> List<M> listLimited(CharSequence hql, int max, List<Object> args);

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
    public int count(CharSequence hql, List<Object> args);

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
    public <M> List<M> paginate(CharSequence hql, int first, int max, List<Object> args);

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
    public <M> M get(CharSequence hql, List<Object> args);

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
    public void execute(CharSequence hql, List<Object> args);

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
     * 获取一个实体的Id JavaBean属性名
     * 
     * @param entityName
     * @return
     */
    public String getIdPropertyName(String entityName);

}
