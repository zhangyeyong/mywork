package com.zte.jbundle.home.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zte.jbundle.home.config.ConfigManager;
import com.zte.jbundle.home.i18n.I18nManager;
import com.zte.jbundle.home.ioc.BeanManager;

public class JBundleTracker implements SynchronousBundleListener {

    Logger log = LoggerFactory.getLogger(getClass());
    final BundleContext context;

    public JBundleTracker(BundleContext context) {
        this.context = context;
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getState() == Bundle.ACTIVE) {
                bundleStarted(bundle);
            }
        }
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.STARTED) {
            bundleStarted(event.getBundle());
        } else if (event.getType() == BundleEvent.STOPPING) {
            bundleStopping(event.getBundle());
        }

    }

    private void bundleStarted(Bundle bundle) {
        I18nManager.instance.loadBundleI18n(bundle);
        ConfigManager.getInstance().register(bundle);
        BeanManager.instance.registerBeans(bundle);
    }

    private void bundleStopping(Bundle bundle) {
        ConfigManager.getInstance().deleteByBundle(bundle);
        BeanManager.instance.unregisterBeans(bundle);
    }

}
