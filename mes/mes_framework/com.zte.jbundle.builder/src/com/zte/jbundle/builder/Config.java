package com.zte.jbundle.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

public class Config {

    public static List<Jar> osgLibs = new ArrayList<Jar>();
    public static Map<String, File> varMap = new HashMap<String, File>();
    public static List<String> codePaths = new ArrayList<String>();
    public static Set<String> excludes = new HashSet<String>();// 排除编译的工程
    public static String distPath = "";
    public static String workPath = "";
    public static String cacheLibs = "";
    public static List<String> antOsgi = new ArrayList<String>();
    public static List<String> antWeb = new ArrayList<String>();

    public static List<String> listOsgiLibs(String repoName) {
        List<String> ret = new ArrayList<String>();
        for (Jar jar : osgLibs) {
            ret.add(jar.jarFile);
        }
        return ret;
    }

    public static String getVarlibPath(String varLib) {
        String ret = null;
        if (varLib != null) {
            int i = varLib.indexOf("/");
            if (i > -1) {
                String varName = varLib.substring(0, i);
                String varFile = varLib.substring(i + 1);
                File folder = varMap.get(varName);
                if (folder != null) {
                    ret = new File(folder, varFile).getAbsolutePath();
                }

            }
        }
        return ret;
    }

    private static List<String> extractAsTextLine(String resPath) throws IOException {
        List<String> ret = new ArrayList<String>();
        File textFile = new File(Config.class.getResource(resPath).getPath());
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(textFile), "utf-8"));
        try {
            for (String line; (line = br.readLine()) != null;) {
                ret.add(line);
            }
        } finally {
            br.close();
        }
        return ret;
    }

    private static void parseConfigXml() {
        try {
            InputStream is = Config.class.getResource("/cfg/config.xml").openStream();
            try {
                Element root = XmlU.parseXml(is);

                Element eCodePaths = XmlU.element(root, "codePaths");
                for (Element ePath : XmlU.elements(eCodePaths, "path")) {
                    codePaths.add(ePath.getTextContent().trim());
                }

                Element eExcludes = XmlU.element(root, "excludes");
                for (Element eProject : XmlU.elements(eExcludes, "project")) {
                    excludes.add(eProject.getTextContent().trim().toLowerCase());
                }

                distPath = XmlU.element(root, "distPath").getTextContent();
                File file = new File(Config.class.getResource("/cfg/config-template.xml").getPath());
                file = new File(file.getParentFile().getParentFile(), "cacheLibs");
                cacheLibs = file.getAbsolutePath();

                Element libList = XmlU.element(root, "libList");
                Element osgiLibs = XmlU.element(libList, "osgLibs");
                if (osgiLibs != null) {
                    for (Element eJar : XmlU.elements(osgiLibs, "jar")) {
                        String text = XmlU.elementText(eJar).trim();
                        if (text.startsWith("http://") || text.startsWith("ftp://")) {
                            osgLibs.add(new Jar("", text));
                        } else if (PbUtils.isFile(new File(text))) {
                            osgLibs.add(new Jar(new File(text).getAbsolutePath(), ""));
                        }
                    }
                }

                for (Element varElem : XmlU.elements(libList, "var")) {
                    String name = varElem.getAttributeNode("name").getTextContent();
                    String path = varElem.getAttributeNode("path").getTextContent();
                    if (name.length() > 0 && path.length() > 0) {
                        File folder = new File(path);
                        if (folder.isDirectory()) {
                            varMap.put(name, folder);
                        }
                    }
                }
            } finally {
                is.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("cfg/config.xml parse failed", e);
        }
    }

    static {
        try {
            antOsgi = extractAsTextLine("/cfg/antOsgi.xml");
            antWeb = extractAsTextLine("/cfg/antWeb.xml");
            parseConfigXml();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            distPath = new File(distPath, sdf.format(new Date())).getAbsolutePath();
            PbUtils.deleteFolder(new File(distPath), false);

            File tmpFile = new File(Config.class.getResource("/cfg/antOsgi.xml").getPath());
            tmpFile = new File(tmpFile.getParentFile().getParentFile(), "works");
            workPath = tmpFile.getAbsolutePath();
            PbUtils.deleteFolder(tmpFile, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
