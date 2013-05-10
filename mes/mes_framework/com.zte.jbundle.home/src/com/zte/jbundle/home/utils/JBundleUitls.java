package com.zte.jbundle.home.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import com.zte.jbundle.cfg.HomeCfg;
import com.zte.jbundle.home.internal.Activator;

@SuppressWarnings("unchecked")
public class JBundleUitls {

    public static final String FILTER_JBUNDLE_ID = "JBundleId";

    private static BundleContext context = Activator.getContext();

    /** 配置POJO所在的包名,eg:jbundle.cfg */
    public static final String CFG_PKG = getPackage(HomeCfg.class);
    /** 配置POJO所在bundle的URI,eg:jbundle/cfg */
    public static final String CFG_URI = getPackageUri(HomeCfg.class);
    /** OSGI服务配置POJO所在的包名 ,eg:jbundle.ioc */
    public static final String IOC_PKG = CFG_PKG.substring(0, CFG_PKG.lastIndexOf(".") + 1) + "ioc";
    /** OSGI服务配置POJO所在POJO所处bundle的URI ,eg:jbundle/ioc */
    public static final String IOC_URI = CFG_URI.substring(0, CFG_URI.lastIndexOf("/") + 1) + "ioc";

    public static <T> T getService(Class<T> clazz) {
        ServiceReference<?> ref = context.getServiceReference(clazz.getName());
        if (ref == null) {
            return null;
        }
        return (T) context.getService(ref);
    }

    private static File HOME_FOLDER = null;

    /**
     * JBundle工作目录
     * 
     * @return
     */
    public static File getHomeFolder() {
        if (HOME_FOLDER == null) {
            URL url = Bundle.class.getResource("/org/osgi/framework/Bundle.class");
            String path = url.getPath();
            path = path.substring(0, path.toLowerCase().indexOf(".jar!") + 2);
            if (path.startsWith("file:")) {
                path = path.substring("file:".length());
            }
            File instanceFolder = new File(path);
            HOME_FOLDER = new File(instanceFolder.getParentFile().getParentFile(), "jbundle");
        }
        if (!HOME_FOLDER.exists()) {
            HOME_FOLDER.mkdirs();
        }
        return HOME_FOLDER;
    }

    private static File PLUG_FOLDER = null;

    /**
     * JBundle管理的插件目录
     * 
     * @return
     */
    public static File getPluginFolder() {
        if (PLUG_FOLDER == null) {
            PLUG_FOLDER = new File(getHomeFolder(), "plugins");
        }
        if (!PLUG_FOLDER.exists()) {
            PLUG_FOLDER.mkdirs();
        }
        return PLUG_FOLDER;
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 获取临时目录，用完后要负责删除
     * 
     * @return
     */
    public static File getTempPath() {
        File ret = new File(getHomeFolder(), "temp/" + uuid());
        ret.mkdirs();
        return ret;
    }

    public static File getStoppedPlugins() {
        return new File(getPluginFolder(), "stoppedPlugins");
    }

    private static File JBundle_CFG_FOLDER = null;

    /**
     * 配置保存目录
     * 
     * @return
     */
    public static File getCfgFolder() {
        if (JBundle_CFG_FOLDER == null) {
            JBundle_CFG_FOLDER = new File(getHomeFolder(), "configs");
        }
        if (!JBundle_CFG_FOLDER.exists()) {
            JBundle_CFG_FOLDER.mkdirs();
        }
        return JBundle_CFG_FOLDER;
    }

    /**
     * 日志文件保存目录
     * 
     * @return
     */
    public static File getLogFolder() {
        File ret = new File(getHomeFolder(), "logs");
        ret.mkdirs();
        return ret;
    }

    public static <K, V> Dictionary<K, V> createDict(Object k, Object v) {
        Hashtable<K, V> ret = new Hashtable<K, V>();
        ret.put((K) k, (V) v);
        return ret;
    }

    public static <K, V> Dictionary<K, V> createDict(Object k1, Object v1, Object k2, Object v2) {
        Hashtable<K, V> ret = new Hashtable<K, V>();
        ret.put((K) k1, (V) v1);
        ret.put((K) k2, (V) v2);
        return ret;
    }

    public static <K, V> Dictionary<K, V> createDict(Object k1, Object v1, Object k2, Object v2, Object k3, Object v3) {
        Hashtable<K, V> ret = new Hashtable<K, V>();
        ret.put((K) k1, (V) v1);
        ret.put((K) k2, (V) v2);
        ret.put((K) k3, (V) v3);
        return ret;
    }

    public static List<String> readTextFile(File file) {
        try {
            return readTextFile(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return new ArrayList<String>();
        }
    }

    public static List<String> readTextFile(InputStream inStream) {
        List<String> ret = new ArrayList<String>();
        if (inStream != null) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
                try {
                    for (String line = null; (line = br.readLine()) != null;) {
                        ret.add(line);
                    }
                } finally {
                    br.close();
                }
            } catch (Exception nothing) {
            }
        }

        return ret;
    }

    public static void writeTextFile(Collection<String> contents, File file) {
        file.getParentFile().mkdirs();
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "gbk"));
            try {
                for (String line : contents) {
                    pw.println(line);
                }
            } finally {
                pw.close();
            }
        } catch (Exception nothing) {
        }
    }

    /**
     * 从路径中提取文件名 D:\1\a.txt ==> a.txt
     * 
     * @param fname
     * @return
     */
    public static String getFileName(String fname) {
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
     * 改变文件名的后缀
     * 
     * @param fileName
     * @param ext
     * @return
     */
    public static String fileExt(String fileName, String ext) {
        if (fileName == null)
            return "";

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
     * 从路径中提取文件后缀名 D:\1\a.txt --> .txt
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

    public static void unzip(InputStream zipStream, File folder) throws IOException {
        if (!folder.exists())
            folder.mkdirs();
        if (!folder.isDirectory())
            throw new IllegalArgumentException("unzip: Arg folder isn't directory(" + folder.getAbsolutePath() + ")");

        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zipStream, 1024 * 1024));
        try {
            byte[] buf = new byte[64 * 1024];
            for (ZipEntry e; (e = zis.getNextEntry()) != null;) {
                if (e.isDirectory())
                    new File(folder, e.getName()).mkdirs();
                else {
                    FileOutputStream fos = new FileOutputStream(new File(folder, e.getName()));
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

    private static boolean recursiveCopyFile(File srcFile, File destFile, byte[] buffer) throws IOException {
        if (!srcFile.exists())
            return false;

        if (!srcFile.isDirectory()) {
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
        } else {
            if (destFile.exists() && destFile.isFile()) {
                if (!destFile.delete())
                    return false;
            }

            destFile.mkdirs();
            for (File srcSubFile : srcFile.listFiles()) {
                File destSubFile = new File(destFile, srcSubFile.getName());
                if (!recursiveCopyFile(srcSubFile, destSubFile, buffer))
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

    public static boolean copyFile(File source, File target) throws IOException {
        return recursiveCopyFile(source, target, new byte[1024 * 1024]);
    }

    public static String getPackageUri(Class<?> clazz) {
        StringBuilder sbRet = new StringBuilder(getPackage(clazz));
        for (int i = 0; i < sbRet.length(); i++) {
            char c = sbRet.charAt(i);
            if (c == '.') {
                sbRet.setCharAt(i, '/');
            }
        }
        return sbRet.toString();
    }

    public static String getPackage(Class<?> clazz) {
        String ret = clazz.getName();
        int i = ret.lastIndexOf(".");
        if (i > -1) {
            return ret.substring(0, i);
        } else {
            return "";
        }
    }

    public static String getNoPackageName(Class<?> clazz) {
        String ret = clazz.getName();
        int i = ret.lastIndexOf(".");
        if (i > -1) {
            return ret.substring(i + 1);
        } else {
            return "";
        }
    }

    public static String trim(String s) {
        return s == null ? null : s.trim();
    }

    public static String toString(Object o) {
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 != null) {
            return o1.equals(o2);
        } else {
            return o2.equals(o1);
        }
    }

    /**
     * 取得JBundle所发布OSGI服务的JBundleId
     * 
     * @param ref
     * @return
     */
    public static String getJBundleId(ServiceReference<?> ref) {
        String id = toString(ref.getProperty(FILTER_JBUNDLE_ID));
        return id == null ? "" : id;
    }

    /**
     * 注册Osgi服务，返回服务注册表
     * 
     * @param service
     * @param osgiName
     * @param jbundleId
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static ServiceRegistration registerToOsgi(Object service, String osgiName, String jbundleId) {
        Dictionary<String, String> filter = new Hashtable<String, String>();
        filter.put(JBundleUitls.FILTER_JBUNDLE_ID, jbundleId);
        return context.registerService(osgiName, service, filter);
    }

    public static BundleContext getContext() {
        return context;
    }

}
