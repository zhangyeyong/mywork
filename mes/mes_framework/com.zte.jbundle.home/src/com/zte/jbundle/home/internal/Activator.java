package com.zte.jbundle.home.internal;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zte.jbundle.api.ICache;
import com.zte.jbundle.cfg.HomeCfg;
import com.zte.jbundle.home.cache.HashCache;
import com.zte.jbundle.home.config.Config;
import com.zte.jbundle.home.config.ConfigManager;
import com.zte.jbundle.home.ioc.BeanManager;
import com.zte.jbundle.home.plugin.PluginManager;
import com.zte.jbundle.home.thirdPartApi.ThirdPartApiAdapterDriver;
import com.zte.jbundle.home.ui.JBundleConsole;

public class Activator implements BundleActivator {

    static Logger log;
    private static BundleContext context;
    static PluginManager pluginManager;
    static ConfigManager configManager;
    static JBundleConsole uiConsole = null;
    private HttpTracker httpTracker;
    private JBundleTracker jBundleTracker;

    public static BundleContext getContext() {
        return context;
    }

    private static void initLogger(BundleContext context) {
        try {
            Bundle bundle = context.getBundle();
            new Config(HomeCfg.class, new HomeCfg(), bundle);
            HomeCfg.setIgnoreNextChange(true);
        } catch (IOException nothing) {
        }
        log = LoggerFactory.getLogger(Activator.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        bundleContext.registerService(ICache.class, new HashCache(), null);

        ThirdPartApiAdapterDriver.execute();
        initLogger(bundleContext);
        configManager = new ConfigManager();
        jBundleTracker = new JBundleTracker(bundleContext);
        bundleContext.addBundleListener(jBundleTracker);
        pluginManager = new PluginManager(bundleContext);

        httpTracker = new HttpTracker();
        httpTracker.open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        ServiceReference<ICache> cacheRef = bundleContext.getServiceReference(ICache.class);
        if (cacheRef != null) {
            bundleContext.ungetService(cacheRef);
        }

        if (uiConsole != null) {
            uiConsole.close();
            uiConsole = null;
        }

        if (httpTracker != null) {
            httpTracker.close();
            httpTracker = null;
        }

        if (jBundleTracker != null) {
            bundleContext.removeBundleListener(jBundleTracker);
        }

        Activator.context = null;
        LogManager.shutdown();
        BeanManager.instance.unregisterBeans(null);
    }

    static void jBundleStarted() {
        uiConsole = new JBundleConsole();
        try {
            uiConsole.open();
        } catch (Exception e) {
            log.error("[!_!]JBundle UI console start failed!", e);
            uiConsole.close();
            uiConsole = null;
        }

        pluginManager.startPlugins();
    }

    static class HttpTracker extends ServiceTracker<HttpService, Object> {

        public HttpTracker() {
            super(Activator.context, HttpService.class, null);
        }

        @Override
        public Object addingService(ServiceReference<HttpService> reference) {
            jBundleStarted();
            return super.addingService(reference);
        }

    }

}
