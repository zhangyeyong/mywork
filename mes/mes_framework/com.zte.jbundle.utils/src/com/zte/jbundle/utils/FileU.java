package com.zte.jbundle.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件相关的操作类
 * 
 * @author PanJun
 * 
 */
public final class FileU {

    /**
     * 从路径中提取文件名 D:\1\a.txt ==> a.txt
     * 
     * @param fname
     * @return
     */
    public static String extractFileName(String fname) {
        if (fname == null)
            return null;

        for (int i = fname.length() - 1; i > -1; i--) {
            char c = fname.charAt(i);
            if (c == '/' || c == '\\')
                return fname.substring(i + 1);
        }

        return fname;
    }

    /**
     * 创建文件夹
     * 
     * @param folder
     */
    public static void mkdirs(File folder) {
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
    }

    /**
     * 改变文件名的后缀
     * 
     * @param fileName
     * @param ext
     * @return
     */
    public static String fileExt(String fileName, String ext) {
        if (fileName == null)
            return null;

        int i = fileName.lastIndexOf(".");
        String result = fileName;
        if (i > -1) {
            result = fileName.substring(0, i);
        }

        if (ext != null) {
            ext = ext.trim();
            if (ext.startsWith("."))
                ext = ext.substring(1);
        } else {
            ext = "";
        }

        if ("".equals(ext))
            return result;
        else
            return result + "." + ext;
    }

    /**
     * 从路径中提取文件后缀名:<br>
     * D:\1\a.txt ==> .txt<br>
     * D:\1\a_txt ==> 空串
     * 
     * @param fname
     * @return
     */
    public static String fileExt(String fname) {
        if (fname == null)
            return null;

        int i = fname.lastIndexOf(".");
        if (i == -1)
            return "";
        return fname.substring(i);
    }

    /**
     * 从路径中提取目录 D:\1\a.txt ==> D:/1/
     * 
     * @param fname
     * @return
     */
    public static String extractFileDir(String fname) {
        if (fname == null)
            return null;

        for (int i = fname.length() - 1; i > -1; i--) {
            char c = fname.charAt(i);
            if (c == '/' || c == '\\')
                return fname.substring(0, i);
        }

        return "";
    }

    /**
     * 整理目录，把所有的"\"转换成"/",返回结束加上"/"，以方便拼接成文件名；如果是空串，返回当期路径
     * 
     * @param dir
     * @return
     */
    public static String decorateDir(String dir) {
        if (dir == null)
            return null;

        if (dir.length() == 0)
            return "./";

        StringBuilder ret = new StringBuilder(dir.length() + 1);
        for (int i = 0, len = dir.length(); i < len; i++) {
            char c = dir.charAt(i);
            if (c == '\\')
                ret.append("/");
            else
                ret.append(c);
        }
        if (ret.charAt(ret.length() - 1) != '/')
            ret.append("/");

        return ret.toString();
    }

    /**
     * 整理文件名，把所有的"\"转换成"/",
     * 
     * @param fName
     * @return
     */
    public static String decorateFileName(String fName) {
        if (fName == null)
            return null;

        StringBuilder ret = new StringBuilder(fName);
        for (int i = 0, len = ret.length(); i < len; i++) {
            char c = fName.charAt(i);
            if (c == '\\')
                ret.setCharAt(i, '/');
        }

        return ret.toString();
    }

    public static boolean deleteFile(String fileName) {
        return deleteFile(new File(fileName));
    }

    public static boolean deleteFile(File file) {
        if (!file.exists())
            return true;

        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                deleteFile(sub);
            }
        }
        return file.delete();
    }

    public static boolean copyFile(String source, String target) {
        return doCopyFile(new File(source), new File(target), new byte[1024 * 1024]);
    }

    public static boolean copyFile(File source, File target) {
        return doCopyFile(source, target, new byte[1024 * 1024]);
    }

    public static boolean copyFileToDir(String file, String directory) {
        File srcFile = new File(file);
        if (!srcFile.exists())
            return false;

        File dirFile = new File(directory);
        return doCopyFile(srcFile, new File(dirFile, srcFile.getName()), new byte[1024 * 1024]);
    }

    protected static boolean doCopyFile(File srcFile, File destFile, byte[] buffer) {
        if (!srcFile.exists())
            return false;

        if (!srcFile.isDirectory()) {
            try {
                destFile.getParentFile().mkdirs();

                FileInputStream src = new FileInputStream(srcFile);
                FileOutputStream dest = new FileOutputStream(destFile);
                try {
                    for (int size; (size = src.read(buffer)) > 0;) {
                        dest.write(buffer, 0, size);
                    }
                } finally {
                    closeIO(src);
                    closeIO(dest);
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            if (destFile.exists() && destFile.isFile()) {
                if (!destFile.delete())
                    return false;
            }

            destFile.mkdirs();
            for (File srcSubFile : srcFile.listFiles()) {
                File destSubFile = new File(destFile, srcSubFile.getName());
                if (!doCopyFile(srcSubFile, destSubFile, buffer))
                    return false;
            }
        }

        return true;
    }

    public static void closeIO(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            // NULL;
        }
    }

}