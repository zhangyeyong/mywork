package com.zte.mcore.cfg;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zte.mcore.utils.Logger;
import com.zte.mcore.utils.McoreU;

/**
 * 配置管理
 * 
 * @author PanJun
 * 
 */
@SuppressWarnings("unchecked")
public class ConfigManager {

    private Map<String, Config> cfgMap = new ConcurrentHashMap<String, Config>();
    private static ConfigManager instance = new ConfigManager();
    private static String CFG_PKG = ConfigManager.class.getPackage().getName();
    private static boolean initialized = false;
    static com.zte.mcore.utils.Logger log = Logger.getLogger(ConfigManager.class);

    public static ConfigManager getInstance() {
        return instance;
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

    /**
     * 配置初始化
     * 
     * @param value
     * @param bundle
     * @throws IOException
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }

        List<String> classeNames = McoreU.loadClasses(Arrays.asList(CFG_PKG), false);
        for (String className : classeNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz == Config.class || clazz == ConfigManager.class) {
                    continue;
                }

                Object o = clazz.newInstance();
                Config cfg = new Config(clazz, o);
                cfgMap.put(cfg.getCfgName(), cfg);
            } catch (Exception e) {
                log.error("[!_!]Load config:" + className + " failed:" + e);
            }
        }
        initialized = true;
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

    /**
     * Mcore内核启动完成通知事件
     * 
     * @param beanMap
     */
    public void mcoreStarted(String[] names, Class<?>[] classes, Object[] instances) {
        List<Config> cfgList = new ArrayList<Config>(cfgMap.values());

        for (Config cfg : cfgList) {
            for (Method m : cfg.clazz.getMethods()) {
                if (McoreU.isMcoreStartedHandler(m)) {
                    try {
                        m.invoke(cfg.getValue(), names, classes, instances);
                    } catch (Exception e) {
                        log.efmt("[!_!]notify %s mcoreStarted event error:%s", cfg.getClass(), e);
                    }
                    break;
                }
            }
        }
    }

}
