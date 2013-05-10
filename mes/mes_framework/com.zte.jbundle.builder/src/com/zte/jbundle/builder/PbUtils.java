package com.zte.jbundle.builder;

import java.io.File;

public class PbUtils {

    public static void deleteFolder(File folder, boolean delFolder) {
        if (!folder.exists()) {
            return;
        }
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                deleteFolder(f, true);
            } else {
                f.delete();
            }
        }
        if (delFolder) {
            folder.delete();
        }
    }

    public static String nvl(String s) {
        return s == null ? "" : s;
    }

    public static boolean isBlank(String s) {
        return nvl(s).trim().length() == 0;
    }

    static boolean isFile(File file) {
        return file != null && file.exists() && !file.isDirectory();
    }

}
