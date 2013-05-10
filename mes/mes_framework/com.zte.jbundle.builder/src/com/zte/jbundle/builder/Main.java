package com.zte.jbundle.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        cacheRemoteJar();

        List<Project> projects = listProjects();
        for (int i = projects.size() - 1; i > -1; i--) {
            Project p = projects.get(i);
            if (Config.excludes.contains(p.name.toLowerCase())) {
                projects.remove(i);
            }
        }

        for (Project p : projects) {
            p.analyzeDepends(projects);
        }

        for (int i = projects.size() - 1; i > -1; i--) {
            Project b = projects.get(i);
            if (!b.rootNode) {
                projects.remove(i);
            }
        }

        for (Project b : projects) {
            b.build();
        }
        generateBuildAll();
    }

    static StringBuilder replaceAll(StringBuilder sb, String sub, String rep) {
        for (int i = -1; (i = sb.indexOf(sub)) > -1;) {
            sb.delete(i, i + sub.length());
            sb.insert(i, rep);
        }
        return sb;
    }

    static void generateBuildAll() throws Exception {
        File antXml = new File(Config.workPath, "buildAll.xml");
        antXml.getParentFile().mkdirs();
        antXml.delete();

        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(antXml), "utf-8"));
        try {
            InputStream stream = Config.class.getResource("/cfg/buildAll.xml").openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            try {
                StringBuilder sbLine = new StringBuilder();
                for (String line; (line = br.readLine()) != null;) {
                    sbLine.setLength(0);
                    sbLine.append(line);
                    replaceAll(sbLine, "#{distPath}", Config.distPath);
                    if (line.indexOf("#{taskList}") > -1) {
                        for (String antFile : Project.allBuildXmls) {
                            pw.println("<ant antfile=\"" + antFile + "\" />");
                        }
                    } else {
                        pw.println(sbLine);
                    }
                }
            } finally {
                br.close();
            }
        } finally {
            pw.close();
        }
    }

    static void loadRecursive(File folder, List<Project> projects) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                Project prj = PrjParser.parse(file);
                if (prj == null) {
                    loadRecursive(file, projects);
                } else {
                    projects.add(prj);
                }
            }
        }
    }

    static List<Project> listProjects() {
        List<Project> ret = new ArrayList<Project>();
        for (String p : Config.codePaths) {
            loadRecursive(new File(p), ret);
        }
        return ret;
    }

    private static void cacheRemoteJar() throws Exception {
        File cachFolder = new File(Config.cacheLibs);
        PbUtils.deleteFolder(cachFolder, false);
        cachFolder.mkdirs();

        File libFolder = new File(cachFolder, "osgLibs");
        libFolder.mkdirs();
        for (Jar jar : Config.osgLibs) {
            cacheOneJar(libFolder, jar);
        }
    }

    private static void cacheOneJar(File libFolder, Jar jar) throws Exception {
        if (PbUtils.isBlank(jar.remoteUrl)) {
            return;
        }

        InputStream ins = openJarStream(jar.remoteUrl);
        try {
            String name = jar.remoteUrl;
            int i = name.indexOf("?");
            if (i > -1) {
                name = name.substring(0, i);
            }
            name = name.substring(name.lastIndexOf("/") + 1);
            jar.jarFile = new File(libFolder, name).getAbsolutePath();
            FileOutputStream fos = new FileOutputStream(jar.jarFile);
            try {
                byte[] buf = new byte[1024];
                for (int size = 0; (size = ins.read(buf)) > 0;) {
                    fos.write(buf, 0, size);
                }
            } finally {
                fos.close();
            }
        } finally {
            ins.close();
        }
    }

    private static InputStream openJarStream(String url) throws Exception {
        File f = new File(url);
        if (f.exists() && !f.isDirectory()) {
            return new FileInputStream(f);
        } else {
            return new URL(url).openStream();
        }
    }

}
