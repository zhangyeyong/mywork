package com.zte.jbundle.api;

import java.util.List;

/**
 * Xml节点
 * 
 * @author PanJun
 * 
 */
public interface XmlNode {

    /**
     * 获取子节点
     * 
     * @param name
     * @return
     */
    List<XmlNode> elements(String name);

    /**
     * 获取属性值
     * 
     * @param name
     * @return
     */
    String attributeValue(String name);

    /**
     * 获取节点内容
     * 
     * @return
     */
    String getText();

    /**
     * 获取属性值，代缺省值
     * 
     * @param name
     * @param defaultValue
     * @return
     */
    String attributeValue(String name, String defaultValue);

    /**
     * 获取第一个子节点
     * 
     * @param name
     * @return
     */
    XmlNode element(String name);

}
