package com.zte.jbundle.home.thirdPartApi;

import com.zte.jbundle.api.Log;
import com.zte.jbundle.api.LogFactory.ILogFactorable;

/**
 * 日志记录服务的工厂接口，为LogFactory支持
 * 
 * @author PanJun
 * 
 */
public class LogFactorableSlf4j implements ILogFactorable {

    @Override
    public <T> Log getLog(Class<T> clazz) {
        return new LogSlf4j(clazz);
    }

    @Override
    public <T> Log getLog(String catagory) {
        return new LogSlf4j(catagory);
    }

}