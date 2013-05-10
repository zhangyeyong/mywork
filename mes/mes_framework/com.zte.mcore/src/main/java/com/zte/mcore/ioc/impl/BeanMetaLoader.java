package com.zte.mcore.ioc.impl;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import com.zte.mcore.utils.Logger;
import com.zte.mcore.utils.McoreU;
import com.zte.mcore.utils.StringU;

class BeanMetaLoader {

    static Logger log = Logger.getLogger(BeanManager.class);

    public static void load(Collection<String> packs, Collection<BeanMeta> metaList) throws Exception {
        List<String> classes = McoreU.loadClasses(packs, true);
        initBeanMeta(classes, metaList);
    }

    private static void initBeanMeta(Collection<String> classes, Collection<BeanMeta> metaList) throws Exception {
        for (String clsName : classes) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(clsName);
            } catch (Throwable e) {
                log.error("[!_!] Failed to initialize [" + clsName + "] class bean instance. Skipped!");
                continue;
            }

            Resource res = clazz.getAnnotation(Resource.class);
            if (res == null) {
                continue;
            }

            BeanMeta meta = new BeanMeta();
            meta.setClazz(clazz);
            meta.setName(res.name());
            meta.setInstance(clazz.newInstance());

            if (StringU.isBlank(meta.getName())) {
                meta.setName(clsName);
            }

            metaList.add(meta);
        }
    }

}
