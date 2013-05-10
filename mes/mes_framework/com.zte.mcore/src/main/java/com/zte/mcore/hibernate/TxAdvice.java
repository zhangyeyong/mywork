package com.zte.mcore.hibernate;

import com.zte.mcore.hibernate.impl.SessionManager;
import com.zte.mcore.ioc.Advice;
import com.zte.mcore.ioc.Invoker;

/**
 * 基于Hibernate的事物管理
 * 
 * @author PanJun
 * 
 */
public class TxAdvice implements Advice {

    static class Counter {
        int i = 0;
    }

    static ThreadLocal<Counter> counter = new ThreadLocal<Counter>() {
        protected Counter initialValue() {
            return new Counter();
        };
    };

    @Override
    public Object invoke(Invoker invoker) throws Throwable {
        try {
            Object result = null;
            try {
                counter.get().i++;
                int count = counter.get().i;
                if (count > 1) {
                    result = invoker.proceed();
                } else {
                    result = invoker.proceed();
                }
            } finally {
                counter.get().i--;
            }

            if (counter.get().i == 0) {
                SessionManager.instance.commitTransactions();
            }
            return result;
        } catch (Throwable e) {
            if (counter.get().i == 0) {
                SessionManager.instance.rollbackTransactions();
            }
            throw e;
        }
    }
}
