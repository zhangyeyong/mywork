package com.zte.mcore.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class McoreU {

    private static Lock jarResourceLock = new ReentrantLock();
    private static List<JarResource> jarResources = null;
    private static File homeFolder = null;
    private static File logFolder = null;
    private static File cfgFolder = null;

    public static File getLogFolder() {
        if (logFolder == null) {
            logFolder = new File(homeFolder, "logs");
        }
        logFolder.mkdirs();
        return logFolder;
    }

    public static File getCfgFolder() {
        if (cfgFolder == null) {
            cfgFolder = new File(homeFolder, "cfgs");
        }
        cfgFolder.mkdirs();
        return cfgFolder;
    }

    /**
     * 获取Mcore的工作目录
     * 
     * @return
     */
    public static File getHomeFolder() {
        return homeFolder;
    }

    /**
     * 设置Mcore的工作目录
     * 
     * @param cHomeFolder
     */
    public static void setHomeFolder(File cHomeFolder) {
        if (cHomeFolder.isDirectory()) {
            McoreU.homeFolder = new File(cHomeFolder.getAbsolutePath());
        } else if (cHomeFolder.exists()) {
            throw new RuntimeException("[!_!] Please set a directory to the cHomeFolder parameter");
        } else if (!(McoreU.homeFolder = cHomeFolder).mkdirs()) {
            throw new RuntimeException("[!_!]Failed to Mkdir for homeFolder. Please set a valid directory!");
        }
    }

    /**
     * 取得一个class对象所在的classPath根目录
     * 
     * @param clazz
     * @return
     */
    public static File getClassPathFolder(Class<?> clazz) {
        String classFile = StringU.replaceAll(clazz.getName(), ".", "/");
        String path = clazz.getResource("/" + classFile + ".class").getPath();
        classFile = new File(path).getAbsolutePath();
        String folder = McoreU.toPackage(classFile);
        int i = folder.lastIndexOf(clazz.getName());
        folder = path.substring(0, i);
        return new File(folder);
    }

    public static String toFilePath(String pkg) {
        if (pkg == null) {
            return null;
        }

        while (pkg.startsWith(".")) {
            pkg = pkg.substring(1);
        }

        StringBuilder ret = new StringBuilder(pkg);
        for (int i = 0; i < ret.length(); i++) {
            char c = ret.charAt(i);
            if (c == '.') {
                ret.setCharAt(i, '/');
            }
        }
        return ret.toString();
    }

    public static String toPackage(String path) {
        if (path == null) {
            return null;
        }

        while (path.startsWith("。")) {
            path = path.substring(1);
        }

        StringBuilder ret = new StringBuilder(path);
        for (int i = 0; i < ret.length(); i++) {
            char c = ret.charAt(i);
            if (c == '/' || c == '\\') {
                ret.setCharAt(i, '.');
            }
        }
        return ret.toString();
    }

    public static String unifyPath(String path) {
        if (path == null) {
            return null;
        }

        StringBuilder ret = new StringBuilder(path);
        for (int i = 0; i < ret.length(); i++) {
            char c = ret.charAt(i);
            if (c == '\\') {
                ret.setCharAt(i, '/');
            }
        }
        return ret.toString();
    }

    public static List<JarResource> getJarResources() {
        if (jarResources != null) {
            return jarResources;
        }

        jarResourceLock.lock();
        try {
            if (jarResources != null) {
                return jarResources;
            }

            List<JarResource> list = new ArrayList<JarResource>();
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources("META-INF/MANIFEST.MF");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                URLConnection conn = url.openConnection();
                if (!(conn instanceof JarURLConnection)) {
                    continue;
                }

                JarURLConnection jarConn = (JarURLConnection) conn;
                list.add(new JarResource(jarConn.getJarFile()));
            }

            jarResources = Collections.unmodifiableList(list);
            return jarResources;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            jarResourceLock.unlock();
        }
    }

    private static String normalizeExt(String ext) {
        ext = ext == null ? "" : ext.trim();
        if (ext.length() > 0 && !ext.startsWith(".")) {
            ext = "." + ext;
        }
        return ext;
    }

    public static List<String> loadClasses(Collection<String> pkgPaths, boolean recursive) {
        List<String> ret = new ArrayList<String>();
        String suffix = ".class";
        McoreU.loadFileResources(pkgPaths, suffix, recursive, ret);
        McoreU.loadJarResources(pkgPaths, suffix, recursive, ret);
        for (int i = ret.size() - 1; i > -1; i--) {
            String className = toPackage(ret.get(i));
            if (className.contains("$")) {
                ret.remove(i);
            } else {
                className = className.substring(0, className.length() - suffix.length());
                ret.set(i, className);
            }
        }
        return ret;
    }

    public static void loadFileResources(Collection<String> pkgPaths, String ext, boolean recursive,
            Collection<String> list) {
        ext = normalizeExt(ext);
        if (ext.length() == 0) {
            return;// 必须要有扩展名
        }

        try {
            for (String pkgPath : pkgPaths) {
                String path = toFilePath(pkgPath);
                String pack = toPackage(pkgPath);
                Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(path);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    File file = new File(url.getPath());
                    if (!file.isDirectory()) {
                        continue;
                    }

                    doLoad(file, pack, ext, recursive, list);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void doLoad(File folder, String pack, String ext, boolean recursive, Collection<String> list) {
        String root = McoreU.toPackage(folder.getAbsolutePath());
        String basePath = toFilePath(pack);
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                if (recursive) {
                    String subPack = pack + "." + f.getName();
                    doLoad(f, subPack, ext, recursive, list);
                }
            } else {
                String name = f.getName();
                String path = f.getAbsolutePath();
                if (name.endsWith(ext) && McoreU.toPackage(path).startsWith(root)) {
                    path = unifyPath(path.substring(root.length()));
                    String resPath = StringU.linkUrl(basePath, unifyPath(path));
                    if (!list.contains(resPath)) {
                        list.add(resPath);
                    }
                }
            }
        }
    }

    public static void loadJarResources(Collection<String> pkgPaths, String ext, boolean recursive,
            Collection<String> jarPaths) {
        Map<JarResource, List<String>> jarMap = new HashMap<JarResource, List<String>>();
        loadJarResources(pkgPaths, ext, recursive, jarMap);
        for (List<String> l : jarMap.values()) {
            jarPaths.addAll(l);
        }
    }

    public static void loadJarResources(Collection<String> pkgPaths, String ext, boolean recursive,
            Map<JarResource, List<String>> jarPaths) {
        ext = normalizeExt(ext);
        if (ext.length() == 0) {
            return;// 必须要有扩展名
        }

        for (JarResource jarRes : McoreU.getJarResources()) {
            Set<String> paths = new HashSet<String>();
            for (String path : jarRes.getPaths()) {
                if (isInPackage(McoreU.toPackage(path), pkgPaths, ext, recursive)) {
                    if (!paths.contains(path)) {
                        paths.add(path);
                    }
                }
            }

            if (paths.size() > 0) {
                jarPaths.put(jarRes, new ArrayList<String>(paths));
            }
        }
    }

    private static boolean isInPackage(String jarPath, Collection<String> packages, String ext, boolean recursive) {
        if (!jarPath.endsWith(ext)) {
            return false;
        }

        for (String p : packages) {
            if (jarPath.startsWith(p)) {
                if (!recursive) {
                    String s = jarPath.substring(p.length());
                    if (!s.contains("/")) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 
     * 判断一个方法是否为：Mcore内核启动完成通知事件; 方法的签名为：
     * 
     * <pre>
     * public void mcoreStarted(String[] names, Class&lt;?&gt;[] classes, Object[] instances);
     * </pre>
     * 
     * 其中beanMap中的Key为Bean的Class，Key为Bean实例(已经经过代理)
     * 
     * @param m
     * @return
     */
    public static boolean isMcoreStartedHandler(Method m) {
        Class<?>[] params = m.getParameterTypes();
        if (!m.getName().equalsIgnoreCase("mcoreStarted") || params.length != 3
                || !params[0].isAssignableFrom(String[].class) || !params[1].isAssignableFrom(Class[].class)
                || !params[2].isAssignableFrom(Object[].class)) {
            return false;
        }

        return true;
    }

}
