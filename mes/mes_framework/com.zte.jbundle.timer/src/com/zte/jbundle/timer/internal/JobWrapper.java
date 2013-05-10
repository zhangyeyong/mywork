package com.zte.jbundle.timer.internal;

import java.lang.reflect.Method;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class JobWrapper implements StatefulJob {

    private static BundleContext context;
    private TimerCfgVo cfg;
    private Object target;
    private Method method = null;
    private Object[] args = new Object[0];

    private synchronized void initContext(JobExecutionContext jobCtx) {
        cfg = (TimerCfgVo) jobCtx.getJobDetail().getJobDataMap().get("cfg");
        Object newTarget = getOsgiService();
        if (newTarget == null) {
            TimerLog.error(cfg + ", no service[" + cfg.getService() + "]!");
            return;
        }

        target = newTarget;
        for (Method m : newTarget.getClass().getMethods()) {
            if (m.getName().equals(cfg.getMethod()) && m.getParameterTypes().length == cfg.getArgs().size()) {
                method = m;
                break;
            }
        }

        if (method == null) {
            TimerLog.error(cfg + ", no target method[" + cfg.getMethod() + "]!");
            return;
        }

        cfg = (TimerCfgVo) jobCtx.getJobDetail().getJobDataMap().get("cfg");
        args = new Object[cfg.getArgs().size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = cfg.getArgs().get(i);
        }
    }

    private Object getOsgiService() {
        ServiceReference<?> ref = context.getServiceReference(cfg.getService());
        if (ref == null) {
            return null;
        }
        return context.getService(ref);
    }

    @Override
    public void execute(JobExecutionContext jobCtx) throws JobExecutionException {
        initContext(jobCtx);
        if (target == null || method == null) {
            return;
        }

        try {
            method.invoke(target, args);
        } catch (Exception e) {
            TimerLog.error(cfg + " failed to execute! ", e);
        }
    }

    public static void setContext(BundleContext context) {
        JobWrapper.context = context;
    }

}
