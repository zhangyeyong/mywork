package com.zte.jbundle.home.thirdPartApi;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import com.zte.jbundle.api.XmlNode;

@SuppressWarnings("unchecked")
public class XmlNodeDom4j implements XmlNode {

    private final Element elem;

    public XmlNodeDom4j(Element elem) {
        this.elem = elem;
    }

    @Override
    public List<XmlNode> elements(String name) {
        List<Element> srcList = elem.elements(name);
        List<XmlNode> retlist = new ArrayList<XmlNode>(srcList.size());
        for (Element src : srcList) {
            retlist.add(new XmlNodeDom4j(src));
        }
        return retlist;
    }

    @Override
    public String attributeValue(String name) {
        return elem.attributeValue(name);
    }

    @Override
    public String getText() {
        return elem.getText();
    }

    @Override
    public String attributeValue(String name, String defaultValue) {
        return elem.attributeValue(name, defaultValue);
    }

    @Override
    public XmlNode element(String name) {
        Element domElem = elem.element(name);
        if (domElem == null)
            return null;
        return new XmlNodeDom4j(domElem);
    }

}
