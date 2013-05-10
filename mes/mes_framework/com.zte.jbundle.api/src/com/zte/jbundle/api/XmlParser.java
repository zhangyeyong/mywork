package com.zte.jbundle.api;

import java.io.InputStream;

public class XmlParser {

    public static interface IXmlParser {

        XmlNode parse(String xml);

        XmlNode parse(InputStream stream);

    }

    private static Class<? extends IXmlParser> parserClazz;

    public static XmlNode parse(String xml) {
        try {
            return parserClazz.newInstance().parse(xml);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static XmlNode parse(InputStream stream) {
        try {
            return parserClazz.newInstance().parse(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<? extends IXmlParser> getParserClazz() {
        return parserClazz;
    }

    public static void setParserClazz(Class<? extends IXmlParser> parserClass) {
        XmlParser.parserClazz = parserClass;
    }

}
