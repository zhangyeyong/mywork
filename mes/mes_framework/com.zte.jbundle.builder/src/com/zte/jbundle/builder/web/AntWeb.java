package com.zte.jbundle.builder.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.zte.jbundle.builder.Config;
import com.zte.jbundle.builder.Project;

public class AntWeb {

    static String javaCode = "<fileset dir=\"%s\" />";
    static String dependJar = "<fileset file=\"%s\" />";

    static void replaceAll(StringBuilder sb, String sub, String rep) {
        for (int i = -1; (i = sb.indexOf(sub)) > -1;) {
            sb.delete(i, i + sub.length());
            sb.insert(i, rep);
        }
    }

    static void loadSource(Project p, Set<File> srcFolderList) {
        srcFolderList.add(new File(p.folder, p.src));
        for (Project dp : p.depends) {
            loadSource(dp, srcFolderList);
        }

    }

    public static void execute(Web web) throws Exception {
        File antXml = new File(Config.workPath, "build-" + web.name + ".xml");
        antXml.getParentFile().mkdirs();
        antXml.delete();

        Set<File> codeList = new HashSet<File>();
        loadSource(web, codeList);

        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(antXml), "utf-8"));
        try {
            for (String line : Config.antWeb) {
                StringBuilder sb = new StringBuilder(line);
                replaceAll(sb, "#{PrjName}", web.name);
                replaceAll(sb, "#{WarName}", web.name);
                replaceAll(sb, "#{PrjFolder}", web.folder.getAbsolutePath());
                replaceAll(sb, "#{distPath}", Config.distPath);
                replaceAll(sb, "#{WebRoot}", web.webRoot.getAbsolutePath());
                replaceAll(sb, "#{WebRoot}", web.webRoot.getAbsolutePath());
                pw.println(sb.toString());
                if (line.contains("#{dependJar}")) {
                    for (String jar : web.depJars) {
                        pw.println(String.format(dependJar, jar));
                    }
                }
                if (line.contains("#{copyJar}")) {
                    for (String var : web.varLibs) {
                        String varLibPath = Config.getVarlibPath(var);
                        if (varLibPath != null && !var.startsWith("TOMCAT_HOME")) {
                            pw.println(String.format(dependJar, varLibPath));
                        }
                    }
                }
                if (line.contains("#{JavaCode}")) {
                    for (File f : codeList) {
                        pw.println(String.format(javaCode, f.getAbsolutePath()));
                    }
                }
            }
        } finally {
            pw.close();
        }
        web.binFile = new File(Config.distPath, web.name + ".jar").getAbsolutePath();
        Project.allBuildXmls.add(antXml.getAbsolutePath());
    }

    static void loadJars(File folder, List<String> jarList) {
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                loadJars(f, jarList);
            } else if (f.getName().toLowerCase().endsWith(".jar")) {
                jarList.add(f.getAbsolutePath());
            }
        }
    }

}
