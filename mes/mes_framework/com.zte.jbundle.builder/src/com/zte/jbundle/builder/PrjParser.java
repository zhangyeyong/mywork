package com.zte.jbundle.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import org.w3c.dom.Element;

import com.zte.jbundle.builder.osgi.Bundle;
import com.zte.jbundle.builder.web.Web;

public class PrjParser {

    static String manifestVersion = "Bundle-manifestVersion:";
    static String shortname = "Bundle-Name:";
    static String symbolic = "Bundle-SymbolicName:";
    static String version = "Bundle-Version:";
    static String vender = "Bundle-Vendor:";
    static String reqEnv = "Bundle-RequiredExecutionEnvironment:";
    static String impPkg = "Import-Package:";
    static String dynPkg = "DynamicImport-Package:";
    static String expPkg = "Export-Package:";
    static String bundleCp = "Bundle-ClassPath:";
    static String requireBundle = "Require-Bundle:";
    static String[] keywords = { manifestVersion, shortname, symbolic, version, vender, reqEnv, impPkg, expPkg, dynPkg,
            bundleCp, requireBundle };

    static boolean parseClassPath(File folder, Project project) {
        File classPath = new File(folder, ".classpath");
        if (!PbUtils.isFile(classPath) || !PbUtils.isFile(new File(folder, ".project"))) {
            return false;
        }

        try {
            project.src = null;
            Element root = XmlU.parseXml(new FileInputStream(classPath));
            for (Element elem : XmlU.elements(root, "classpathentry")) {
                String kind = XmlU.elementAttr(elem, "kind");
                String path = XmlU.elementAttr(elem, "path");
                if ("src".equals(kind)) {
                    if (!path.startsWith("/") && !path.equals("src/test/java")) {
                        project.src = path;
                    }

                    // web project required project
                    if (path.startsWith("/") && project instanceof Web) {
                        project.requires.add(path.substring(1));
                    }
                } else if ("var".equals(kind)) {
                    if (project instanceof Web) {
                        ((Web) project).varLibs.add(path);
                    }
                }
            }
            return project.src != null;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean containsKeyword(String s) {
        if (s == null) {
            return false;
        }
        for (String k : keywords) {
            if (s.startsWith(k))
                return true;
        }
        return false;
    }

    static <T extends Collection<String>> void extractPkgAdd(String s, T targets) {
        int i = s.indexOf(";");
        if (i > -1) {
            s = s.substring(0, i);
        }
        s = s.trim();
        if (s.endsWith(",")) {
            s = s.substring(0, s.length() - 1).trim();
        }
        if (s.length() > 0)
            targets.add(s);
    }

    static boolean parseManifest(File folder, Bundle bundle) {
        File manifest = new File(folder, "META-INF/MANIFEST.MF");
        if (!manifest.exists() || manifest.isDirectory()) {
            return false;
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(manifest), "utf-8"));
            try {
                boolean impFlag = false;
                boolean expFlag = false;
                boolean reqFlag = false;
                bundle.name = null;
                for (String line; (line = br.readLine()) != null;) {
                    if (line.startsWith(symbolic)) {
                        impFlag = false;
                        expFlag = false;
                        reqFlag = false;
                        bundle.name = line.substring(symbolic.length()).trim();
                    } else if (line.startsWith(impPkg)) {
                        impFlag = true;
                        expFlag = false;
                        reqFlag = false;
                        String s = line.substring(impPkg.length()).trim();
                        extractPkgAdd(s, bundle.imports);
                    } else if (line.startsWith(expPkg)) {
                        impFlag = false;
                        expFlag = true;
                        reqFlag = false;
                        String s = line.substring(expPkg.length()).trim();
                        extractPkgAdd(s, bundle.exports);
                    } else if (line.startsWith(requireBundle)) {
                        impFlag = false;
                        expFlag = false;
                        reqFlag = true;
                        String s = line.substring(requireBundle.length()).trim();
                        extractPkgAdd(s, bundle.requires);
                    } else if (containsKeyword(line)) {
                        impFlag = false;
                        expFlag = false;
                        reqFlag = false;
                    } else {
                        if (impFlag) {
                            extractPkgAdd(line, bundle.imports);
                        }
                        if (expFlag) {
                            extractPkgAdd(line, bundle.exports);
                        }
                        if (reqFlag) {
                            extractPkgAdd(line, bundle.requires);
                        }
                    }

                }
                return bundle.name != null;
            } finally {
                br.close();
            }
        } catch (Exception e) {
            return false;
        }

    }

    public static Project parse(File folder) {
        Project ret = parseBundle(folder);
        if (ret != null) {
            return ret;
        }

        return parseWeb(folder);
    }

    private static boolean isWebRoot(File folder) {
        File WEB_INF_web_xml = new File(folder, "WEB-INF/web.xml");
        if (PbUtils.isFile(WEB_INF_web_xml)) {
            return true;
        }
        return false;
    }

    private static File findWebRoot(File folder) {
        if (isWebRoot(folder)) {
            return folder;
        }

        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                File ret = findWebRoot(f);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    private static Project parseWeb(File folder) {
        Web web = new Web();
        web.folder = folder;
        if (!parseClassPath(folder, web)) {
            return null;
        }

        web.webRoot = findWebRoot(folder);
        if (web.webRoot == null) {
            return null;
        }
        if (!parseWebName(web)) {
            return null;
        }
        return web;
    }

    static boolean parseWebName(Web web) {
        File classPath = new File(web.folder, ".project");
        web.name = "";
        try {
            Element root = XmlU.parseXml(new FileInputStream(classPath));
            Element name = XmlU.element(root, "name");
            if (name != null) {
                web.name = XmlU.elementText(name);
            }
            return web.name != null && web.name.length() > 0;
        } catch (Exception e) {
            return false;
        }

    }

    private static Project parseBundle(File folder) {
        Bundle bundle = new Bundle();
        if (!parseManifest(folder, bundle)) {
            return null;
        }
        bundle.folder = folder;
        if (!parseClassPath(folder, bundle)) {
            return null;
        }
        return bundle;
    }

}
