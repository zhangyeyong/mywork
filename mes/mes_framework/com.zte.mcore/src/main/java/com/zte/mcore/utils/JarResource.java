package com.zte.mcore.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * jar包中资源路径信息
 * 
 * @author PanJun
 * 
 */
public class JarResource {

    private final JarFile jarFile;
    private final List<String> paths;

    public JarResource(JarFile jarFile) {
        this.jarFile = jarFile;

        List<String> list = new ArrayList<String>();
        Enumeration<JarEntry> entries = jarFile.entries();
        for (; entries.hasMoreElements();) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                list.add(entry.getName());
            }
        }
        paths = Collections.unmodifiableList(list);
    }

    public JarFile getJarFile() {
        return jarFile;
    }

    public List<String> getPaths() {
        return paths;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JarResource) {
            JarResource that = (JarResource) obj;
            return that.jarFile.getName().equals(jarFile.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return jarFile.getName().hashCode();
    }

}
