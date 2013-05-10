package com.zte.jbundle.hibernate;

import com.zte.jbundle.api.Advice;
import com.zte.jbundle.api.Invoker;
import com.zte.jbundle.api.OsgiService;
import com.zte.jbundle.hibernate.framework.SessionManager;

/**
 * 事物切片服务
 * 
 * @author PanJun
 * 
 */
@OsgiService(jbundleId = "TxAdvice")
public class TxAdvice implements Advice {

    static class Counter {
        int i = 0;

        @Override
        public String toString() {
            return Integer.toString(i);
        }
    }

    static ThreadLocal<Counter> invokes = new ThreadLocal<Counter>() {
        protected Counter initialValue() {
            return new Counter();
        };
    };

    @Override
    public Object invoke(Invoker invoker) throws Throwable {
        Counter invokeCounter = invokes.get();
        try {
            Object result = null;
            try {
                invokeCounter.i++;
                result = invoker.proceed();
            } finally {
                invokeCounter.i--;
            }

            if (invokeCounter.i == 0) {
                SessionManager.instance.commitTransactions();
            }
            return result;
        } catch (Throwable e) {
            if (invokeCounter.i == 0) {
                SessionManager.instance.rollbackTransactions();
            }
            throw e;
        }
    }
}
