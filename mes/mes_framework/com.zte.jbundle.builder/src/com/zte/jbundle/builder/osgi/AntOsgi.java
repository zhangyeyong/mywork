package com.zte.jbundle.builder.osgi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.zte.jbundle.builder.Config;
import com.zte.jbundle.builder.Project;

public class AntOsgi {

    static String unzipJar = "<unzip dest=\"${tempDir}\" src=\"%s\" />";
    static String dependJar = "<file name=\"%s\" />";

    static void replaceAll(StringBuilder sb, String sub, String rep) {
        for (int i = -1; (i = sb.indexOf(sub)) > -1;) {
            sb.delete(i, i + sub.length());
            sb.insert(i, rep);
        }
    }

    public static void execute(Bundle b) throws Exception {
        File antXml = new File(Config.workPath, "build-" + b.name + ".xml");
        antXml.getParentFile().mkdirs();
        antXml.delete();

        List<String> jarList = new ArrayList<String>();
        loadJars(b.folder, jarList);
        List<String> bundleJarList = new ArrayList<String>();
        for (Project depBundle : b.depends) {
            bundleJarList.add(depBundle.binFile);
        }

        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(antXml), "utf-8"));
        try {
            for (String line : Config.antOsgi) {
                StringBuilder sb = new StringBuilder(line);
                replaceAll(sb, "#{symbolic}", b.name);
                replaceAll(sb, "#{src}", b.src);
                replaceAll(sb, "#{bundleFolder}", b.folder.getAbsolutePath());
                replaceAll(sb, "#{distPath}", Config.distPath);
                pw.println(sb.toString());
                if (line.contains("#{dependJar}")) {
                    dependJar(pw, jarList);// 插件内置jar包
                    dependJar(pw, bundleJarList);// 依赖的插件
                    dependJar(pw, Config.listOsgiLibs("osgi"));// 仓库jar包
                }
                if (line.contains("#{unzipJar}")) {
                    for (String jar : jarList) {
                        pw.println(String.format(unzipJar, jar));
                    }
                }
            }
        } finally {
            pw.close();
        }
        b.binFile = new File(Config.distPath, b.name + ".jar").getAbsolutePath();
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

    static void dependJar(PrintWriter pw, List<String> jarList) {
        if (jarList != null) {
            for (String jar : jarList) {
                pw.println(String.format(dependJar, jar));
            }
        }
    }

}
