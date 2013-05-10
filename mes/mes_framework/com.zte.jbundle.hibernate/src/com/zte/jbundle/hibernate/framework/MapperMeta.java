package com.zte.jbundle.hibernate.framework;

import org.osgi.framework.Bundle;

/**
 * 每个插件中.hbm.xml 文件
 */
public class MapperMeta {

    public final String xml;
    public final Bundle from;// 来源插件

    public MapperMeta(String xml, Bundle from) {
        this.xml = xml;
        this.from = from;
    }

}