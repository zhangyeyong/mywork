package com.zte.jbundle.home.ioc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zte.jbundle.api.Advice;
import com.zte.jbundle.api.AdviceBuilder;

public class ProxyHelper {

    static Logger log = LoggerFactory.getLogger(ProxyHelper.class);

    public static boolean buildProxy(final Bean bean) {
        Object o = bean.getInstance();
        if (bean.getIntfaces().length == 0 || o instanceof Advice || o instanceof AdviceBuilder) {
            bean.setProxy(bean.getInstance());
            return true;
        }

        try {
            InvocationHandler invocationHandler = new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return bean.invokeMethod(method, args);
                }
            };
            Object proxy = Proxy.newProxyInstance(bean.getInstance().getClass().getClassLoader(), bean.getIntfaces(),
                    invocationHandler);
            bean.setProxy(proxy);
            return true;
        } catch (Throwable e) {
            log.error(String.format("[!_!] jbundle failed to proxy [%s]!", bean.getInstance().getClass()), e);
            return false;
        }
    }
}
