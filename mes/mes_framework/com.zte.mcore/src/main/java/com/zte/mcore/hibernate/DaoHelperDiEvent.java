package com.zte.mcore.hibernate;

import java.lang.reflect.Field;

import com.zte.mcore.hibernate.impl.DaoHelperImpl;
import com.zte.mcore.ioc.OnDependencyInject;

/**
 * DaoHelper字段注入事件
 * 
 * @author PanJun
 * 
 */
public class DaoHelperDiEvent implements OnDependencyInject {

    @Override
    public boolean handle(Object target, Field field, String resourceName) throws Exception {
        if (field.getType().isAssignableFrom(DaoHelper.class)) {
            DaoHelper daoHelper = DaoHelperImpl.getInstance(resourceName);
            field.setAccessible(true);
            field.set(target, daoHelper);
            return true;
        }
        return false;
    }

}
