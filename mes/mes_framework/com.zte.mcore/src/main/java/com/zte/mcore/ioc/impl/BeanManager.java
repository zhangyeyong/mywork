package com.zte.mcore.ioc.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zte.mcore.cfg.ConfigManager;
import com.zte.mcore.ioc.AdviceRule;
import com.zte.mcore.ioc.AdviceRuleAsm;
import com.zte.mcore.ioc.OnDependencyInject;
import com.zte.mcore.utils.McoreU;

@SuppressWarnings("unchecked")
public class BeanManager {

    Set<BeanMeta> metaList = new HashSet<BeanMeta>();
    private OnDependencyInject onDependencyInject;

    public void initBeans(Collection<String> packList) {
        try {
            metaList.clear();
            BeanMetaLoader.load(packList, metaList);

            for (BeanMeta meta : metaList) {
                meta.initIocFields();
            }

            injectDepencies();

            initAdvices();

            dispatchMcoreStarted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void injectDepencies() throws Exception {
        for (BeanMeta meta : metaList) {
            Iterator<IocEntry> iterator = meta.iocFieldsIterator();
            while (iterator.hasNext()) {
                IocEntry e = iterator.next();
                if (onDependencyInject != null
                        && onDependencyInject.handle(e.getTarget(), e.getField(), e.getResName())) {
                    iterator.remove();
                    continue;
                }

                BeanMeta sourceMeta = getIocSourceMeta(e, metaList);
                if (sourceMeta != null) {
                    e.setValue(sourceMeta.getProxy());
                    iterator.remove();
                }
            }
        }

        reportFailededDi();
    }

    private void reportFailededDi() {
        StringBuilder diErrors = new StringBuilder();
        for (BeanMeta meta : metaList) {
            Iterator<IocEntry> iterator = meta.iocFieldsIterator();
            while (iterator.hasNext()) {
                IocEntry field = iterator.next();
                if (diErrors.length() == 0) {
                    diErrors.append("[!_!]Fatal error!! MCore failed to build dependency of following beans:\n");
                }
                diErrors.append("\t----").append(field).append("\n");
            }
        }

        if (diErrors.length() > 0) {
            throw new RuntimeException(diErrors.toString());
        }
    }

    private BeanMeta getIocSourceMeta(IocEntry field, Set<BeanMeta> metaList) {
        Class<?> targetClazz = field.getField().getType();
        for (BeanMeta meta : metaList) {
            if (targetClazz.isAssignableFrom(meta.getClazz())) {
                return meta;
            }
        }
        return null;
    }

    private void initAdvices() {
        for (BeanMeta meta : metaList) {
            if (AdviceRuleAsm.class.isAssignableFrom(meta.getClazz())) {
                AdviceRuleAsm asm = (AdviceRuleAsm) meta.getProxy();
                List<AdviceRule> rules = asm.buildAdviceRules();
                for (BeanMeta destMeta : metaList) {
                    destMeta.addAdvices(rules);
                }
            }
        }
    }

    public <T> T getBean(String name) {
        for (BeanMeta meta : metaList) {
            if (meta.getName().equalsIgnoreCase(name)) {
                return (T) meta.getProxy();
            }
        }
        return null;
    }

    public <T> T getBean(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }

        for (BeanMeta meta : metaList) {
            if (clazz.isAssignableFrom(meta.getClazz())) {
                return (T) meta.getProxy();
            }
        }
        return null;
    }

    public <T> Map<String, T> getBeans(Class<T> clazz) {
        Map<String, T> ret = new HashMap<String, T>();
        for (BeanMeta meta : metaList) {
            if (clazz.isAssignableFrom(meta.getClazz())) {
                ret.put(meta.getName(), (T) meta.getProxy());
            }
        }
        return ret;
    }

    /**
     * 通知所有的Bean，MCore中的Bean已经初始化完成,通知事件方法签名为：<br/>
     * 
     * <pre>
     * public void mcoreStarted(String[] names, Class&lt;?&gt;[] classes, Object[] instances);
     * </pre>
     * 
     * 其中beanMap中的Key为Bean的Class，Key为Bean实例(已经经过代理)
     */
    private void dispatchMcoreStarted() throws Exception {
        String[] names = new String[metaList.size()];
        Class<?>[] classes = new Class[metaList.size()];
        Object[] instances = new Object[metaList.size()];

        int i = 0;
        for (BeanMeta meta : metaList) {
            names[i] = meta.getName();
            classes[i] = meta.getClazz();
            instances[i] = meta.getProxy();
            i++;
        }

        for (BeanMeta meta : metaList) {
            for (Method m : meta.getClazz().getMethods()) {
                if (McoreU.isMcoreStartedHandler(m)) {
                    m.invoke(meta.getProxy(), names, classes, instances);
                    break;
                }
            }
        }

        ConfigManager.getInstance().mcoreStarted(names, classes, instances);
    }

    public void setOnDependencyInject(OnDependencyInject onDependencyInject) {
        this.onDependencyInject = onDependencyInject;
    }
}
