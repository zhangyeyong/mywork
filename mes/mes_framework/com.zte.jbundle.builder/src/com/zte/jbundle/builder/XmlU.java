package com.zte.jbundle.builder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlU {

    public static String nvl(String s) {
        return s == null ? "" : s;
    }

    public static List<Element> elements(Node node) {
        return elements(node, "");
    }

    public static List<Element> elements(Node node, String name) {
        List<Element> ret = new ArrayList<Element>();
        if (node != null) {
            boolean isBlank = name == null || name.trim().length() == 0;
            NodeList nodeList = node.getChildNodes();
            for (int k = 0; k < nodeList.getLength(); k++) {
                Node n = nodeList.item(k);
                if (n instanceof Element) {
                    if (isBlank || name.equals(n.getNodeName())) {
                        ret.add((Element) n);
                    }
                }
            }
        }
        return ret;
    }

    public static Element element(Node node, String name) {
        if (node != null && name != null) {
            NodeList nodeList = node.getChildNodes();
            for (int k = 0; k < nodeList.getLength(); k++) {
                Node n = nodeList.item(k);
                if (n instanceof Element && name.equals(n.getNodeName())) {
                    return (Element) n;
                }
            }
        }
        return null;
    }

    public static Node xPath(Node node, String... names) {
        if (node != null) {
            for (String name : names) {
                node = element(node, name);
            }
        }
        return node;
    }

    public static String elementText(Node node) {
        return nvl(node.getTextContent()).trim();
    }

    public static String elementAttr(Node e, String attr) {
        if (e instanceof Element) {
            Attr eAttr = ((Element) e).getAttributeNode(attr);
            if (eAttr == null) {
                return "";
            } else {
                return nvl(eAttr.getTextContent());
            }
        }
        return "";
    }

    public static Element parseXml(InputStream in) {
        try {
            try {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                return doc.getDocumentElement();
            } finally {
                in.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
