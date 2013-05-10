package com.zte.jbundle.home.thirdPartApi;

import java.io.InputStream;

import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;

import com.zte.jbundle.api.XmlNode;
import com.zte.jbundle.api.XmlParser.IXmlParser;

public class XmlParserDom4j implements IXmlParser {

    @Override
    public XmlNode parse(String xml) {
        try {
            return new XmlNodeDom4j(DocumentHelper.parseText(xml).getRootElement());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XmlNode parse(InputStream stream) {
        try {
            try {
                return new XmlNodeDom4j(new SAXReader().read(stream).getRootElement());
            } finally {
                stream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
