package com.zte.jbundle.home.ioc;

import java.util.concurrent.atomic.AtomicLong;

import com.zte.jbundle.api.Advice;
import com.zte.jbundle.api.Invoker;

public class PxyAdvice implements Advice, Comparable<PxyAdvice> {

    private Advice advice;
    private static AtomicLong UID_SEED = new AtomicLong(1);
    public final long uid = UID_SEED.getAndDecrement();

    public PxyAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(Invoker invoker) throws Throwable {
        return this.advice.invoke(invoker);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PxyAdvice) {
            PxyAdvice that = (PxyAdvice) obj;
            return that.uid == uid;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) uid;
    }

    @Override
    public int compareTo(PxyAdvice o) {
        if (uid > o.uid) {
            return 1;
        } else if (uid == o.uid) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return advice.getClass().getName();
    }

}
