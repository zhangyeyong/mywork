package com.zte.jbundle.home.thirdPartApi;

import com.zte.jbundle.api.HessianProxier;
import com.zte.jbundle.api.LogFactory;
import com.zte.jbundle.api.OsgiContext;
import com.zte.jbundle.api.XmlParser;

/**
 * 第三方jar包适配器驱动
 * 
 * @author PanJun
 * 
 */
public class ThirdPartApiAdapterDriver {

    /**
     * 执行适配器装配
     */
    public static void execute() {
        LogFactory.setFactorable(new LogFactorableSlf4j());
        XmlParser.setParserClazz(XmlParserDom4j.class);
        HessianProxier.setProxiable(new HessianProxiable());
        OsgiContext.setOsgiContextable(new OsgiContextable());
    }

}
