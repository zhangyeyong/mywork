package com.zte.mcore.remote.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caucho.hessian.server.HessianServlet;
import com.zte.mcore.remote.Remote;
import com.zte.mcore.utils.Logger;

/**
 * 以Hessian协议提供外部服务
 * 
 * @author PanJun
 * 
 */
@Resource
public class RemotableByHessian {

    static class HssnReg {
        HessianServlet servlet;
        String uri;
        Class<?> inface;
        Object instance;
        boolean servletInit = false;
    }

    private Lock lock = new ReentrantLock();
    Logger log = Logger.getLogger(getClass());
    static HssnReg[] hessianRegistries = new HssnReg[0];

    public static boolean handledAsHessian(HttpServletRequest req, HttpServletResponse resp, ServletConfig cfg)
            throws IOException, ServletException {
        String uri = req.getRequestURI();
        String hessianPart = "/m-core/hessian/";
        int i = uri.indexOf(hessianPart);
        if (i < 0) {
            return false;
        }

        uri = uri.substring(i + hessianPart.length());
        HssnReg reg = null;
        for (HssnReg r : hessianRegistries) {
            if (r.uri.equals(uri)) {
                reg = r;
                break;
            }
        }

        if (reg == null) {
            for (HssnReg r : hessianRegistries) {
                if (r.uri.equalsIgnoreCase(uri)) {
                    reg = r;
                    break;
                }
            }
        }

        if (reg == null) {
            return false;
        }

        if (!reg.servletInit) {
            reg.servlet.init(cfg);
            reg.servletInit = true;
        }
        reg.servlet.service(req, resp);
        return true;
    }

    public void mcoreStarted(String[] names, Class<?>[] classes, Object[] instances) {
        List<HssnReg> regList = new ArrayList<HssnReg>();
        regList.clear();
        for (int i = 0; i < names.length; i++) {
            Class<?> clazz = classes[i];
            Object instance = instances[i];
            Remote ann = clazz.getAnnotation(Remote.class);
            if (ann == null) {
                continue;
            }

            Class<?>[] infaces = clazz.getInterfaces();
            for (Class<?> inf : infaces) {
                HssnReg reg = new HssnReg();
                reg.uri = getUri(clazz, inf, ann, infaces.length > 1);
                reg.inface = inf;
                reg.instance = instance;

                try {
                    reg.servlet = new HessianServlet();
                    reg.servlet.setHome(instance);
                    reg.servlet.setAPIClass(reg.inface);
                    regList.add(reg);
                } catch (Exception e) {
                    reg.servlet.destroy();
                    reg.servlet = null;
                    log.error("[!_!]Failed to register (" + clazz + ") as hessian service!", e);
                    return;
                }
            }
        }

        lock.lock();
        try {
            hessianRegistries = regList.toArray(new HssnReg[regList.size()]);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 只实现一个接口URI: 实现类名<br>
     * 实现多个接口eg：
     * 
     * <pre>
     * public class A implements IA, IB {
     * }
     * </pre>
     * 
     * URI:A/IA, A/IB
     * 
     * @param clazz
     * @param inf
     * @param remote
     * @param manyInterface
     * @return
     */
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

        return uri;
    }

}
