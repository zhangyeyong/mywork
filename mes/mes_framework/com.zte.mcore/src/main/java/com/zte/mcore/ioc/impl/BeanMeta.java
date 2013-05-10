package com.zte.mcore.ioc.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import javax.annotation.Resource;

import com.zte.mcore.ioc.Advice;
import com.zte.mcore.ioc.AdviceRule;
import com.zte.mcore.ioc.AdviceRuleAsm;
import com.zte.mcore.ioc.Invoker;

public class BeanMeta {

    private Class<?> clazz;
    private Object instance;
    private String name;
    private Object proxy;
    private final List<Advice> advices = new ArrayList<Advice>();
    private final List<IocEntry> iocFields = new ArrayList<IocEntry>();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BeanMeta) {
            BeanMeta meta = (BeanMeta) obj;
            return clazz.equals(meta.clazz);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return clazz.getName().hashCode();
    }

    @Override
    public String toString() {
        return clazz.getName();
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
        ProxyFactory factory = new ProxyFactory();
        try {
            factory.setSuperclass(clazz);
            this.proxy = factory.create(new Class<?>[0], new Object[0], new MethodHandler() {

                @Override
                public Object invoke(Object self, Method m, Method proceed, Object[] args) throws Throwable {
                    return invokeMethod(m, args);
                }

            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Object invokeMethod(Method thisMethod, Object[] args) throws Throwable {
        thisMethod.setAccessible(true);
        if (advices.isEmpty()) {
            return thisMethod.invoke(instance, args);
        }

        Invoker invoker = new RealInvoker(instance, thisMethod, args);
        for (int i = advices.size() - 1; i > -1; i--) {
            Advice advice = advices.get(i);
            invoker = new FilterInvoker(advice, invoker);
        }
        return invoker.proceed();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getProxy() {
        return proxy;
    }

    public void addAdvices(List<AdviceRule> rules) {
        // 切片装配器不能再做AOP
        if (instance instanceof AdviceRuleAsm) {
            return;
        }

        // 切片不能再做AOP
        if (instance instanceof Advice) {
            return;
        }

        for (AdviceRule rule : rules) {
            if (rule.matchesRule(clazz)) {
                advices.add(rule.getAdvice());
            }
        }
    }

    public void initIocFields() {
        iocFields.clear();

        Set<Field> fields = new HashSet<Field>();
        Class<?> clazz = this.clazz;
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        for (Field field : fields) {
            Resource resAnn = null;
            for (Annotation ann : field.getAnnotations()) {
                if (Resource.class.isAssignableFrom(ann.getClass())) {
                    resAnn = (Resource) ann;
                    break;
                }
            }

            if (resAnn == null) {
                continue;
            }

            String serviceName = resAnn.name();
            IocEntry iocField = new IocEntry(instance, field, serviceName);
            iocFields.add(iocField);
        }

    }

    public Iterator<IocEntry> iocFieldsIterator() {
        return new Iterator<IocEntry>() {
            int i = iocFields.size() - 1;

            @Override
            public boolean hasNext() {
                return i > -1;
            }

            @Override
            public IocEntry next() {
                return iocFields.get(i--);
            }

            @Override
            public void remove() {
                iocFields.remove(i + 1);
            }
        };
    }
}
