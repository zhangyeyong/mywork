package com.zte.jbundle.home.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zte.jbundle.api.Advice;
import com.zte.jbundle.api.Invoker;
import com.zte.jbundle.api.OsgiReference;
import com.zte.jbundle.home.ioc.Bean.IocEntry;
import com.zte.jbundle.home.utils.JBundleUitls;

@SuppressWarnings("all")
public class Bean {

    public class IocEntry {

        public final Field field;
        public final Class<?> clazz;
        public final String jid;
        public volatile Object value = null;
        public final long uid = getUid();

        public IocEntry(Field field, String jbundleId) {
            this.field = field;
            this.clazz = field.getType();
            this.jid = jbundleId;
        }

        boolean assignFieldValue(Object srcValue, String srcJbundleId) {
            if (srcValue == this.value || !isIocMatched(srcValue, srcJbundleId)) {
                return false;
            }

            return assignFieldValueDirectly(srcValue);
        }

        boolean unassignFieldValue(Object oldValue, String srcJbundleId) {
            if (!isIocMatched(oldValue, srcJbundleId)) {
                return false;
            }

            return assignFieldValueDirectly(null);
        }

        private boolean assignFieldValueDirectly(Object srcValue) {
            try {
                field.setAccessible(true);
                field.set(instance, srcValue);
                this.value = srcValue;
                return true;
            } catch (Throwable e) {
                String msg = "[!_!]Jbundle ioc[%s] failed,source=%s";
                log.error(String.format(msg, this, srcValue), e);
                return false;
            }
        }

        /**
         * 查询赋值给Ioc字段
         * 
         * @param beans
         * @return 赋值成功或失败
         */
        public boolean assignFieldValue(List<Bean> beans) {
            Object srcValue = null;
            for (int i = beans.size() - 1; i > -1; i--) {
                Bean src = beans.get(i);
                if (assignFieldValue(src.getProxy(), src.jid)) {
                    return true;
                }
            }

            if (srcValue == null) {
                try {
                    ServiceReference[] refs = JBundleUitls.getContext().getAllServiceReferences(clazz.getName(), null);
                    if (refs != null) {
                        for (ServiceReference ref : refs) {
                            String jbundleId = JBundleUitls.getJBundleId(ref);
                            Object service = JBundleUitls.getContext().getService(ref);
                            if (assignFieldValue(service, jbundleId)) {
                                srcValue = service;
                                break;
                            }
                        }
                    }
                } catch (Throwable nothing) {
                }

            }

            return false;
        }

        private boolean isIocMatched(Object value, String srcJbundleId) {
            srcJbundleId = srcJbundleId == null ? "" : srcJbundleId.trim();
            return (value == null || clazz.isInstance(value)) && jid.equalsIgnoreCase(srcJbundleId);
        }

        public Bean getBean() {
            return Bean.this;
        }

        @Override
        public String toString() {
            return Bean.this + "->" + field.getType().getSimpleName() + " " + field.getName();
        }

    }

    static Logger log = LoggerFactory.getLogger(Bean.class);
    public final long uid = getUid();
    private final Object instance;
    private final Bundle from;
    private List<ServiceRegistration> osgiRegs = new ArrayList<ServiceRegistration>();
    private String jid;
    private Object proxy;
    /**
     * Jbundle在bean注册成功事件，服务的事件处理函数签名如下：
     * 
     * <pre>
     *  public void beanRegisterEvent(Object beanInstance, Class<?> beanClazz) throws Exception
     * </pre>
     */
    private Method beanRegisterEventHandler;
    /**
     * Jbundle在bean注销成功事件，服务的事件处理函数签名如下：
     * 
     * <pre>
     *  public void beanUnregisterEvent(Object beanInstance, Class<?> beanClazz) throws Exception
     * </pre>
     */
    private Method beanUnregisterEventHandler;
    private final Class<?>[] intfaces;
    private List<IocEntry> iocEntries = new CopyOnWriteArrayList<IocEntry>();
    private List<Advice> advices = Collections.emptyList();
    private ThreadLocal<Set<Advice>> inAdvices = new ThreadLocal<Set<Advice>>() {
        protected java.util.Set<Advice> initialValue() {
            return new HashSet<Advice>();
        }
    };

    static AtomicLong uidSeed = new AtomicLong(1);

    static long getUid() {
        return uidSeed.getAndIncrement();
    }

    public Bean(Object instance, Bundle from) {
        this.from = from;
        this.instance = instance;
        intfaces = instance.getClass().getInterfaces();

        for (Method m : instance.getClass().getMethods()) {
            String name = m.getName();
            Class<?>[] pTypes = m.getParameterTypes();
            boolean paramsMatched = pTypes.length == 2 && pTypes[0] == Object.class && pTypes[1] == Class.class;
            if (name.equalsIgnoreCase("beanRegisterEvent") && paramsMatched) {
                beanRegisterEventHandler = m;
            }
            if (name.equalsIgnoreCase("beanUnregisterEvent") && paramsMatched) {
                beanUnregisterEventHandler = m;
            }

            if (beanRegisterEventHandler != null && beanUnregisterEventHandler != null) {
                break;
            }
        }

    }

    @Override
    public String toString() {
        return instance.getClass().getName();
    }

    public void extractIocEntries() {
        Class<?> clazz = instance.getClass();
        List<IocEntry> ret = new ArrayList<IocEntry>();
        Set<Field> fields = new HashSet<Field>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        for (Field field : fields) {
            OsgiReference osgiRef = null;
            for (Annotation ann : field.getAnnotations()) {
                if (OsgiReference.class.isAssignableFrom(ann.getClass())) {
                    osgiRef = (OsgiReference) ann;
                    break;
                }
            }

            if (osgiRef == null) {
                continue;
            }

            // 以@OsgiReference注解的jbundleId属性值作为JBundleId
            String jbundleId = osgiRef.jbundleId() == null ? "" : osgiRef.jbundleId().trim();
            ret.add(new IocEntry(field, jbundleId));
        }

        iocEntries.clear();
        iocEntries.addAll(ret);
    }

    public void openIocTracker() {
        for (IocEntry e : iocEntries) {
            IocTracker.openTracker(e.clazz.getName(), e);
        }
    }

    /**
     * 把Bean注册到OSGI容器中，已经注册就不再注册
     * 
     * @return 注册是否成功
     */
    public boolean registerToOsgi() {
        synchronized (osgiRegs) {
            if (osgiRegs.isEmpty()) {
                for (Class<?> inf : intfaces) {
                    osgiRegs.add(JBundleUitls.registerToOsgi(proxy, inf.getName(), jid));
                }

                if (intfaces.length == 0) {
                    osgiRegs.add(JBundleUitls.registerToOsgi(proxy, instance.getClass().getName(), jid));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 从OSGI容器中注销
     */
    public void unregisterToOsgi() {
        synchronized (osgiRegs) {
            for (ServiceRegistration reg : osgiRegs) {
                reg.unregister();
            }
            osgiRegs.clear();
        }

        for (IocEntry e : iocEntries) {
            IocTracker.removeEntry(e);
        }
    }

    public void notify_beanRegisterEvent(Object beanInstance, Class<?> beanClazz) throws Throwable {
        if (beanRegisterEventHandler != null) {
            beanRegisterEventHandler.setAccessible(true);
            beanRegisterEventHandler.invoke(instance, beanInstance, beanClazz);
        }
    }

    public void notify_beanUnregisterEvent(Object beanInstance, Class<?> beanClazz) throws Throwable {
        if (beanUnregisterEventHandler != null) {
            beanUnregisterEventHandler.setAccessible(true);
            beanUnregisterEventHandler.invoke(instance, beanInstance, beanClazz);
        }
    }

    public Object invokeMethod(Method method, Object[] args) throws Throwable {
        Set<Advice> set = inAdvices.get();
        if (advices == null || advices.isEmpty()) {
            return method.invoke(instance, args);
        }

        Invoker invoker = new FinalInvoker(instance, method, args);
        for (int i = advices.size() - 1; i > -1; i--) {
            Advice advice = advices.get(i);
            if (!set.contains(advice)) {
                set.add(advice);
                invoker = new DecoratedInvoker(advice, invoker);
            }
        }

        try {
            return invoker.proceed();
        } catch (Throwable e) {
            String msg = "[!_!]Failed to invoke %s.%s() method:" + e;
            log.error(String.format(msg, clazz().getName(), method.getName()), e);
            throw e;
        } finally {
            set.clear();
        }
    }

    public Bundle getFrom() {
        return from;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String id) {
        this.jid = id;
    }

    public Object getInstance() {
        return instance;
    }

    /**
     * 返回instance的Class对象
     * 
     * @return
     */
    public Class<?> clazz() {
        return instance.getClass();
    }

    public Object getProxy() {
        return proxy;
    }

    public void setProxy(Object proxy) {
        this.proxy = proxy;
    }

    public Class<?>[] getIntfaces() {
        return intfaces;
    }

    public List<IocEntry> getIocEntries() {
        return iocEntries;
    }

    public void loadUninjectedIocFields(List<IocEntry> entries) {
        for (IocEntry e : iocEntries) {
            if (e.value == null) {
                entries.add(e);
            }
        }
    }

    public List<Advice> getAdvices() {
        return advices;
    }

    public void setAdvices(List<Advice> advices) {
        this.advices = advices;
    }

}

@SuppressWarnings("all")
class IocTracker extends ServiceTracker {

    private final String clazz;
    private AtomicBoolean opened = new AtomicBoolean(false);
    private Map<Long, IocEntry> targets = new HashMap<Long, IocEntry>();
    private static Map<String, IocTracker> cachedIocTrackers = new HashMap<String, IocTracker>();

    public static void openTracker(String serviceClazz, IocEntry e) {
        synchronized (cachedIocTrackers) {
            IocTracker tracker = cachedIocTrackers.get(serviceClazz);
            if (tracker == null) {
                tracker = new IocTracker(serviceClazz);
                cachedIocTrackers.put(serviceClazz, tracker);
            }
            tracker.targets.put(e.uid, e);
            tracker.open();
        }
    }

    public static void closeAllTrackers() {
        synchronized (cachedIocTrackers) {
            for (IocTracker tracker : cachedIocTrackers.values()) {
                tracker.close();
            }
            cachedIocTrackers.clear();
        }
    }

    public static void removeEntry(IocEntry e) {
        synchronized (cachedIocTrackers) {
            for (IocTracker t : cachedIocTrackers.values()) {
                t.targets.remove(e.uid);
            }
        }
    }

    private IocTracker(String serviceClazz) {
        super(JBundleUitls.getContext(), serviceClazz, null);
        this.clazz = serviceClazz;
    }

    @Override
    public void open() {
        if (!opened.getAndSet(true)) {
            super.open();
        }
    }

    @Override
    public void close() {
        if (opened.getAndSet(false)) {
            super.close();
        }
    }

    @Override
    public Object addingService(ServiceReference ref) {
        String jbundleId = JBundleUitls.getJBundleId(ref);
        Object service = JBundleUitls.getContext().getService(ref);
        for (IocEntry e : targets.values()) {
            e.assignFieldValue(service, jbundleId);
        }
        return super.addingService(ref);
    }

    @Override
    public void removedService(ServiceReference ref, Object service) {
        String jbundleId = JBundleUitls.getJBundleId(ref);
        for (IocEntry e : targets.values()) {
            e.unassignFieldValue(service, jbundleId);
        }
        super.removedService(ref, service);
    }

}
