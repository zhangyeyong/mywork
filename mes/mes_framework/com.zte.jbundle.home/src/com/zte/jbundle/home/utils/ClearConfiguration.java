package com.zte.jbundle.home.utils;

import java.io.File;

public class ClearConfiguration {

    private static boolean deleteFile(File file) {
        if (!file.exists())
            return true;

        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                deleteFile(sub);
            }
        }
        return file.delete();
    }

    public static void main(String[] args) {
        StringBuilder clazzPath = new StringBuilder(ClearConfiguration.class.getSimpleName());
        for (int i = clazzPath.length() - 1; i > -1; i--) {
            char c = clazzPath.charAt(i);
            if (c == '.') {
                clazzPath.setCharAt(i, '/');
            }
        }
        clazzPath.append(".class");
        String path = ClearConfiguration.class.getResource(clazzPath.toString()).getPath();
        path = path.substring(0, path.toLowerCase().indexOf(".jar!") + 2);
        if (path.startsWith("file:")) {
            path = path.substring("file:".length());
        }

        File parentFile = new File(path).getParentFile().getParentFile();
        parentFile = new File(parentFile, "configuration");
        if (!parentFile.exists()) {
            return;
        }

        for (File f : parentFile.listFiles()) {
            String name = f.getName();
            if (name.endsWith(".log") || name.startsWith("org.eclipse.")) {
                deleteFile(f);
                System.out.println("deleted.... " + f);
            }
        }

    }
}
