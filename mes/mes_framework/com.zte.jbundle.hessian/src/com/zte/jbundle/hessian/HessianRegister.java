package com.zte.jbundle.hessian;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.service.http.HttpService;

import com.caucho.hessian.server.HessianServlet;
import com.zte.jbundle.api.Log;
import com.zte.jbundle.api.LogFactory;
import com.zte.jbundle.api.OsgiReference;
import com.zte.jbundle.api.OsgiService;
import com.zte.jbundle.api.Remote;

@OsgiService
public class HessianRegister {

    static class HssnReg {
        HessianServlet servlet;
        String uri;
        Class<?> inface;
        Class<?> clazz;
        Object instance;
    }

    private Lock lock = new ReentrantLock();
    @OsgiReference
    private HttpService http = null;

    Log log = LogFactory.getLog(getClass());
    List<HssnReg> hssnRegMap = new ArrayList<HssnReg>();

    private String getUri(Class<?> clazz, Class<?> inf, Remote remote, boolean manyInterface) {
        String uri = remote.uri();
        if (uri == null || uri.trim().length() == 0) {
            uri = clazz.getSimpleName();
        } else {
            uri = uri.trim();
        }

        if (manyInterface) {
            uri = uri + "/" + inf.getSimpleName();
        }

        uri = "/hessian/" + uri;
        return uri;
    }

    public void beanRegisterEvent(Object instance, Class<?> clazz) throws Throwable {
        Remote ann = clazz.getAnnotation(Remote.class);
        if (ann == null) {
            return;
        }

        lock.lock();
        try {
            Class<?>[] infaces = clazz.getInterfaces();
            for (Class<?> inf : infaces) {
                HssnReg reg = new HssnReg();
                reg.uri = getUri(clazz, inf, ann, infaces.length > 1);
                reg.inface = inf;
                reg.instance = instance;
                reg.clazz = clazz;
                regHessian(reg);
                hssnRegMap.add(reg);
            }
        } finally {
            lock.unlock();
        }
    }

    public void beanUnregisterEvent(Object instance, Class<?> clazz) throws Throwable {
        lock.lock();
        try {
            if (clazz == HessianRegister.class) {
                clearAll();
                return;
            }

            for (int i = hssnRegMap.size() - 1; i > -1; i--) {
                HssnReg reg = hssnRegMap.get(i);
                if (reg.instance == instance) {
                    unRegHessian(reg);
                    hssnRegMap.remove(i);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void regHessian(HssnReg reg) {
        if (http == null || reg.servlet != null) {
            return;
        }

        String clazz = reg.clazz.getName();
        reg.servlet = new HessianServlet();
        try {
            reg.servlet.setHome(reg.instance);
            reg.servlet.setAPIClass(reg.inface);
            http.registerServlet(reg.uri, reg.servlet, null, null);
            log.info("[^_^]Jbundle register \"" + clazz + "\" as hessian(" + reg.uri + ") service ok!");
        } catch (Exception e) {
            reg.servlet.destroy();
            reg.servlet = null;
            log.error("[!_!]Jbundle failed to register (" + clazz + ") as hessian service!", e);
            return;
        }
    }

    private void unRegHessian(HssnReg reg) {
        if (reg != null && reg.servlet != null && http != null) {
            reg.servlet.destroy();
            http.unregister(reg.uri);
            log.info("[^_^]Jbundle unregister \"" + reg.clazz.getName() + "\"(uri" + reg.uri + ")from hessian ok!");
        }
    }

    private void clearAll() {
        lock.lock();
        try {
            for (int i = hssnRegMap.size() - 1; i > -1; i--) {
                HssnReg reg = hssnRegMap.get(i);
                unRegHessian(reg);
                hssnRegMap.remove(i);
            }
        } finally {
            lock.unlock();
        }
    }

}
