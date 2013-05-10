package com.zte;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

/**
 * Bundle内部使用的jar包，扁平输出Bundle类
 * 
 * @author PanJun
 * 
 */
public class FlatJarInBundleTools {

    private static void addZip(byte[] buf, File file, ZipArchiveOutputStream zof, String parentName) throws IOException {
        String curName = file.getName();
        if (parentName != null && parentName.trim().length() > 0)
            curName = parentName + "/" + curName;

        if (file.isDirectory()) {
            zof.putArchiveEntry(new ZipArchiveEntry(curName + "/"));
            for (File child : file.listFiles()) {
                addZip(buf, child, zof, curName);
            }
        } else {
            zof.putArchiveEntry(new ZipArchiveEntry(curName));
            FileInputStream fis = new FileInputStream(file);
            try {
                for (int size; (size = fis.read(buf)) > 0;) {
                    zof.write(buf, 0, size);
                }
            } finally {
                fis.close();
            }
        }
    }

    private static void addZipWithoutTopFolder(byte[] buf, File file, ZipArchiveOutputStream zof, String parentName)
            throws IOException {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                addZip(buf, child, zof, null);
            }
        } else
            addZip(buf, file, zof, parentName);
    }

    static void zip(File file, OutputStream zipStream) throws IOException {
        try {
            ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(zipStream);
            try {
                zaos.setLevel(1);// 最低压缩率，最快的速度
                addZipWithoutTopFolder(buf, file, zaos, null);
            } finally {
                zaos.closeArchiveEntry();
                zaos.close();
            }
        } finally {
            zipStream.close();
        }
    }

    static byte[] buf = new byte[64 * 1024];

    static void unzip(InputStream zipStream, File folder) throws IOException {
        if (!folder.exists())
            folder.mkdirs();
        if (!folder.isDirectory())
            throw new IllegalArgumentException("unzip: Arg folder isn't directory(" + folder.getAbsolutePath() + ")");

        ZipArchiveInputStream zis = new ZipArchiveInputStream(zipStream);
        try {
            for (ZipArchiveEntry e; (e = zis.getNextZipEntry()) != null;) {
                if (e.isDirectory())
                    new File(folder, e.getName()).mkdirs();
                else {
                    File targetFile = new File(folder, e.getName());
                    if (targetFile.exists()) {
                        continue;
                    }
                    targetFile.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    try {
                        for (int size; (size = zis.read(buf)) > 0;) {
                            fos.write(buf, 0, size);
                        }
                    } finally {
                        fos.close();
                    }
                }
            }
        } finally {
            zis.close();
        }
    }

    static boolean deleteFile(File file) {
        if (!file.exists())
            return true;

        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                deleteFile(sub);
            }
        }
        return file.delete();
    }

    private static String getSimpleName(String sname) {
        int i = sname.lastIndexOf("_");
        if (i > -1) {
            sname = sname.substring(0, i);
        }
        return sname;
    }

    static void loadJars(File folder, List<File> jarList) {
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                loadJars(f, jarList);
            } else if (f.getName().toLowerCase().endsWith(".jar")) {
                jarList.add(f);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.showOpenDialog(null);
        File file = fc.getSelectedFile();
        String sname = getSimpleName(file.getName());
        File tmpFolder = new File(file.getParentFile(), sname);
        deleteFile(tmpFolder);
        tmpFolder.mkdirs();

        unzip(new FileInputStream(file), tmpFolder);
        List<File> jarList = new ArrayList<File>();
        loadJars(tmpFolder, jarList);
        for (int i = 0; i < jarList.size(); i++) {
            File jar = jarList.get(i);
            System.out.printf("unziping.....%s(%s/%s)\n", jar.getName(), i + 1, jarList.size());
            unzip(new FileInputStream(jar), tmpFolder);
            System.out.printf("unziped OK %s(%s/%s)\n", jar.getName(), i + 1, jarList.size());
            jar.delete();
        }

        FileOutputStream fos = new FileOutputStream(new File(tmpFolder.getParentFile(), tmpFolder.getName() + ".jar"));
        zip(tmpFolder, fos);
        deleteFile(tmpFolder);
    }

}
