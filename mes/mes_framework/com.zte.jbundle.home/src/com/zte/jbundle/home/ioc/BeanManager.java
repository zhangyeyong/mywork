package com.zte.jbundle.home.ioc;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zte.jbundle.api.Advice;
import com.zte.jbundle.api.AdviceBuilder;
import com.zte.jbundle.api.OsgiService;
import com.zte.jbundle.home.ioc.Bean.IocEntry;

public class BeanManager {

    private List<Bean> beanList = new ArrayList<Bean>();
    private List<AdviceRule> adviceList = new ArrayList<AdviceRule>();
    final static Object locker = new Object();
    private RegisterOsgiThread registerOsgiThread;

    Logger log = LoggerFactory.getLogger(getClass());

    // Singleton
    public static BeanManager instance = new BeanManager();

    private BeanManager() {
    }

    public void registerBeans(Bundle bundle) {
        List<Bean> bundleBeans = loadBeans(bundle);
        for (int i = bundleBeans.size() - 1; i > -1; i--) {
            Bean bean = bundleBeans.get(i);
            if (!ProxyHelper.buildProxy(bean)) {
                bundleBeans.remove(i);
                continue;
            }

            if (bean.getInstance() instanceof AdviceBuilder) {
                adviceList.add(new AdviceRule(bean));
            }
        }

        synchronized (locker) {
            beanList.addAll(bundleBeans);
        }

        startRegisterOsgiThread();
    }

    private synchronized void startRegisterOsgiThread() {
        RegisterOsgiThread.updateTimeout();
        if (registerOsgiThread == null) {
            registerOsgiThread = new RegisterOsgiThread(this);
            registerOsgiThread.start();
        }
    }

    synchronized void shutdownRegisterOsgiThread() {
        registerOsgiThread = null;
    }

    private void scanBeanClassNames(Bundle bundle, List<String> classeNames) {
        String symbolic = bundle.getSymbolicName();
        String[] parts = symbolic.split("\\.");// 插件命名必须为：com.zte.项目.模块
        if (parts.length < 3 || symbolic.startsWith("org.eclipse.") || symbolic.startsWith("javax.")
                || symbolic.startsWith("org.apache.")) {
            return;
        }

        String scanRoot = "/" + parts[0] + "/" + parts[1];
        String[] findPaths = { scanRoot, "/bin" + scanRoot };
        for (String findPath : findPaths) {
            Enumeration<?> e = bundle.findEntries(findPath, "*.class", true);
            while (e != null && e.hasMoreElements()) {
                String path = ((URL) e.nextElement()).getPath();
                if (path.startsWith("/bin/")) {
                    path = path.substring("/bin/".length());
                }
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                String clsName = path.substring(0, path.length() - 6).replaceAll("/", ".");
                if (!clsName.contains("$")) {
                    classeNames.add(clsName);
                }
            }
        }
    }

    private List<Bean> loadBeans(Bundle bundle) {
        List<Bean> ret = new ArrayList<Bean>();
        List<String> clazzList = new ArrayList<String>();
        scanBeanClassNames(bundle, clazzList);

        for (String className : clazzList) {
            Object serviceInstance = null;
            OsgiService osgiService = null;
            try {
                Class<?> clazz = bundle.loadClass(className);
                osgiService = clazz.getAnnotation(OsgiService.class);
                if (osgiService == null) {
                    continue;
                }
                serviceInstance = clazz.newInstance();
            } catch (Throwable e) {
                serviceInstance = null;
                log.error("[!_!]Jbundle initialize class[" + className + "] as OSGI service instance failed!" + e);
                continue;
            }

            Bean bean = new Bean(serviceInstance, bundle);
            bean.setJid(osgiService.jbundleId());
            bean.extractIocEntries();
            ret.add(bean);
        }

        return ret;
    }

    private void updateBeanAdvices() {
        List<Bean> allBeans = new ArrayList<Bean>();
        synchronized (locker) {
            allBeans.addAll(beanList);
        }

        for (Bean bean : allBeans) {
            List<Advice> advices = new ArrayList<Advice>();
            for (AdviceRule ah : adviceList) {
                if (ah.matchesRule(bean.clazz().getName())) {
                    for (Advice a : ah.advices()) {
                        advices.add(new PxyAdvice(a));
                    }
                }
            }
            bean.setAdvices(advices);
        }
    }

    public void unregisterBeans(Bundle bundle) {
        List<Bean> allBeans = new ArrayList<Bean>();
        List<Bean> removeds = new ArrayList<Bean>();
        synchronized (locker) {
            allBeans.addAll(beanList);
            for (int i = allBeans.size() - 1; i > -1; i--) {
                Bean bean = allBeans.get(i);
                if (bundle == null || bean.getFrom().getSymbolicName().equals(bundle.getSymbolicName())) {
                    removeds.add(beanList.remove(i));
                }
            }
        }

        // 告诉所有的Bean,哪些Bean将要被注销
        for (Bean target : allBeans) {
            for (Bean removed : removeds) {
                try {
                    target.notify_beanUnregisterEvent(removed.getProxy(), removed.getInstance().getClass());
                } catch (Throwable e) {
                    String msg = "[!_!]Jbundle failed to notify [%s] unregisted event to [%s]";
                    log.error(String.format(msg, removed, target), e);
                }
            }
        }

        for (Bean removed : removeds) {
            removed.unregisterToOsgi();
            for (int i = adviceList.size() - 1; i > -1; i--) {
                if (adviceList.get(i).isBean(removed)) {
                    adviceList.remove(i);
                }
            }
        }

        updateBeanAdvices();
    }

    /**
     * 注入依赖，并返回未被注入的依赖条目
     * 
     * @param uninjectedList
     */
    void injectDependencyAndReturnUninjected(List<IocEntry> uninjectedList) {
        uninjectedList.clear();
        List<Bean> allBeans = new ArrayList<Bean>();
        synchronized (locker) {
            allBeans.addAll(beanList);
        }

        List<IocEntry> tmpUninjected = new ArrayList<IocEntry>();
        for (Bean target : allBeans) {
            tmpUninjected.clear();
            target.loadUninjectedIocFields(tmpUninjected);
            for (IocEntry e : tmpUninjected) {
                if (!e.assignFieldValue(allBeans)) {
                    uninjectedList.add(e);
                }
            }
        }

        if (uninjectedList.isEmpty()) {
            List<Bean> existedBeans = new ArrayList<Bean>();
            List<Bean> newOsgiBeans = new ArrayList<Bean>();
            for (Bean bean : allBeans) {
                if (bean.registerToOsgi()) {
                    newOsgiBeans.add(bean);
                } else {
                    existedBeans.add(bean);
                }
            }

            updateBeanAdvices();

            for (Bean bean : allBeans) {
                bean.openIocTracker();
            }

            // 告诉已有Bean，哪些新的Bean注册了
            dispatchBeanRegisterEvent(existedBeans, newOsgiBeans);
            // 告诉新注册的Bean，所有的Bean
            dispatchBeanRegisterEvent(newOsgiBeans, allBeans);
            if (allBeans.size() > 0) {
                log.error("[^_^]Success, maybe every thing is ok! total beans:" + allBeans.size());
            }
        }
    }

    public void dispatchBeanRegisterEvent(List<Bean> targets, List<Bean> eventBeans) {
        for (Bean target : targets) {
            for (int i = eventBeans.size() - 1; i > -1; i--) {
                Bean newBean = eventBeans.get(i);
                try {
                    target.notify_beanRegisterEvent(newBean.getProxy(), newBean.getInstance().getClass());
                } catch (Throwable e) {
                    String msg = "[!_!]Jbundle failed to notify [%s] registed event to [%s]";
                    log.error(String.format(msg, newBean, target), e);
                }
            }
        }
    }

}

class RegisterOsgiThread extends Thread {

    private static final long TIMEOUT_MILLS = TimeUnit.SECONDS.toMillis(10);
    private static AtomicLong registerTimeout = new AtomicLong(System.currentTimeMillis() + TIMEOUT_MILLS);
    private final BeanManager beanManager;

    public static void updateTimeout() {
        registerTimeout.set(System.currentTimeMillis() + TIMEOUT_MILLS);
    }

    public RegisterOsgiThread(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public void run() {
        List<IocEntry> uninjectedEntries = new ArrayList<IocEntry>();
        synchronized (beanManager) {
            while (true) {
                try {
                    beanManager.wait(500);
                } catch (InterruptedException nothing) {
                }

                beanManager.injectDependencyAndReturnUninjected(uninjectedEntries);
                if (uninjectedEntries.isEmpty()) {
                    break;
                } else if (registerTimeout.get() < System.currentTimeMillis()) {
                    StringBuilder sbMsg = new StringBuilder();
                    sbMsg.append("[!_!]Fatal error!! JBundle failed to build dependency of following beans:\n");
                    for (IocEntry e : uninjectedEntries) {
                        sbMsg.append("\t").append(e.getBean()).append(" field: ").append(e.field.getName());
                        sbMsg.append("(").append(e.field.getType().getName()).append(")\n");
                    }
                    beanManager.log.error(sbMsg.toString());
                    break;
                }
            }

            beanManager.shutdownRegisterOsgiThread();
        }
    }
}
