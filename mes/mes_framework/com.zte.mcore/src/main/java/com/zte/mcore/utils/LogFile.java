package com.zte.mcore.utils;

import java.io.File;

public class LogFile {

    private String name;
    private String size;
    private long modified;

    public LogFile(File f) {
        name = f.getName();
        size = fileSize(f);
        modified = f.lastModified();
    }

    private static String fileSize(File f) {
        String k = Long.toString((f.length() / 1024));
        String dot = "";
        StringBuilder ret = new StringBuilder();
        while (k.length() > 0) {
            ret.insert(0, dot);
            if (k.length() > 3) {
                ret.insert(0, k.substring(k.length() - 4));
                k = k.substring(0, k.length() - 3);
            } else {
                ret.insert(0, k);
                k = "";
            }
            dot = ",";
        }
        return ret.append(" kb").toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

}
