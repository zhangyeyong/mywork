package com.zte.jbundle.home.config;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zte.jbundle.home.utils.JBundleUitls;

/**
 * 配置管理
 * 
 * @author PanJun
 * 
 */
@SuppressWarnings("unchecked")
public class ConfigManager {

    private Map<String, Config> cfgMap = new ConcurrentHashMap<String, Config>();
    private static ConfigManager instance;
    static Logger log = LoggerFactory.getLogger(ConfigManager.class);

    public static ConfigManager getInstance() {
        return instance;
    }

    public ConfigManager() {
        instance = this;
    }

    /**
     * 保存UI中配置修改
     * 
     * @param cfgName
     * @param json
     * @return
     * @throws IOException
     */
    public void saveValue(String cfgName, String json) throws IOException {
        Config cfg = cfgMap.get(cfgName);
        if (cfg != null) {
            cfg.setJson(json);
            cfg.saveToFile();
        }
    }

    private void addJbundleCfgClass(Enumeration<?> e, List<String> classeNames) {
        while (e != null && e.hasMoreElements()) {
            String path = ((URL) e.nextElement()).getPath();
            String clsName = JBundleUitls.CFG_PKG + "." + path.substring(path.lastIndexOf("/") + 1, path.length() - 6);
            classeNames.add(clsName);
        }
    }

    /**
     * 注册某个插件中的配置
     * 
     * @param value
     * @param bundle
     * @throws IOException
     */
    public void register(Bundle bundle) {
        List<String> classeNames = new ArrayList<String>();
        addJbundleCfgClass(bundle.findEntries("/bin/" + JBundleUitls.CFG_URI, "*.class", false), classeNames);
        if (classeNames.isEmpty()) {
            addJbundleCfgClass(bundle.findEntries("/" + JBundleUitls.CFG_URI, "*.class", false), classeNames);
        }

        for (String className : classeNames) {
            try {
                Class<?> clazz = bundle.loadClass(className);
                Object o = clazz.newInstance();
                Config cfg = new Config(clazz, o, bundle);
                cfgMap.put(cfg.getCfgName(), cfg);
            } catch (Exception e) {
                log.error("[!_!]Load bundle:" + bundle.getSymbolicName() + ", config:" + className + " failed:" + e);
            }
        }
    }

    /**
     * 获取配置值
     * 
     * @param <T>
     * @param cfgClass
     * @return
     */
    public <T> T getValue(Class<T> cfgClass) {
        Config cfg = cfgMap.get(cfgClass);
        if (cfg != null) {
            return (T) cfg.getValue();
        } else {
            return null;
        }
    }

    /**
     * 获取配置值
     * 
     * @param <T>
     * @param cfgClass
     * @return
     */
    public String getJsonValue(String className) {
        Config cfg = cfgMap.get(className);
        if (cfg != null) {
            return cfg.getJson();
        }
        return null;
    }

    /**
     * 当插件关闭时，配置信息移除，但不修改持久化数据
     * 
     * @param bundle
     */
    public void deleteByBundle(Bundle bundle) {
        String symbolic = bundle.getSymbolicName();
        for (String k : cfgMap.keySet()) {
            Config cfg = cfgMap.get(k);
            if (symbolic.equals(cfg.from.getSymbolicName())) {
                cfgMap.remove(k);
            }
        }
    }

    /**
     * 查询配置列表，配置名称包含 参数cfgName
     * 
     * @param cfgName
     *            过滤名称，为空全查
     * @return
     */
    public List<Config> listConfigs(String cfgName) {
        cfgName = cfgName == null ? "" : cfgName.trim().toLowerCase();
        List<Config> ret = new ArrayList<Config>();
        for (Config cfg : cfgMap.values()) {
            if (cfgName.length() == 0 || cfg.getCfgName().toLowerCase().contains(cfgName)) {
                ret.add(cfg);
            }

        }
        Collections.sort(ret, new Comparator<Config>() {
            @Override
            public int compare(Config o1, Config o2) {
                return o1.getCfgName().compareToIgnoreCase(o2.getCfgName());
            }
        });
        return ret;
    }

    /**
     * 恢复默认值
     * 
     * @param className
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void restoreDefault(String className) throws Exception {
        Config cfg = cfgMap.get(className);
        if (cfg != null) {
            cfg.restoreDefault();
        }
    }

}
