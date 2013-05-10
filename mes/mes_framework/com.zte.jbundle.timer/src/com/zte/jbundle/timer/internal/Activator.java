package com.zte.jbundle.timer.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;


public class Activator implements BundleActivator {

    private BundleListener cfgListener = null;
    private TimerManager timerManager;
    static BundleContext context;

    public void start(final BundleContext context) throws Exception {
        Activator.context = context;
        JobWrapper.setContext(context);
        timerManager = new TimerManager();

        cfgListener = new CfgTracker(context, timerManager);
        context.addBundleListener(cfgListener);
    }

    public void stop(BundleContext context) throws Exception {
        context.removeBundleListener(cfgListener);
        if (timerManager != null) {
            timerManager.shutdown();
            timerManager = null;
        }
    }
}
