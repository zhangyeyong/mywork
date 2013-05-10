package com.zte.mcore.ioc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.zte.mcore.cfg.ConfigManager;
import com.zte.mcore.ioc.impl.BeanManager;
import com.zte.mcore.utils.McoreU;

/**
 * mcore IOC中Bean管理容器类
 * 
 * @author PanJun
 * 
 */
@Resource
public final class BeanContext {

    private Set<String> packList = new HashSet<String>();
    private BeanManager beanManager = new BeanManager();
    private OnDependencyInject onDependencyInject = null;

    public void addScanPackage(String... packages) {
        addScanPackage(Arrays.asList(packages));
    }

    public void addScanPackage(List<String> packages) {
        if (packages != null) {
            for (String pkg : packages) {
                if (pkg != null) {
                    packList.add(pkg.trim());
                }
            }
        }
    }

    public void startup() {
        try {
            if (McoreU.getHomeFolder() == null) {
                throw new RuntimeException(
                        "[!_!] Please invoke McoreU.setHomeFolder() function to initialize the home folder!");
            }
            ConfigManager.getInstance().initialize();
            beanManager.setOnDependencyInject(onDependencyInject);
            beanManager.initBeans(packList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        return (T) beanManager.getBean(name);
    }

    public <T> T getBean(Class<T> clazz) {
        return (T) beanManager.getBean(clazz);
    }

    public <T> Map<String, T> getBeans(Class<T> clazz) {
        return beanManager.getBeans(clazz);
    }

    public OnDependencyInject getOnDependencyInject() {
        return onDependencyInject;
    }

    public void setOnDependencyInject(OnDependencyInject onDependencyInject) {
        this.onDependencyInject = onDependencyInject;
    }

}
