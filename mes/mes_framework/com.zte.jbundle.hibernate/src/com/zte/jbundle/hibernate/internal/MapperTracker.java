package com.zte.jbundle.hibernate.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

import com.zte.jbundle.hibernate.framework.MapperManager;
import com.zte.jbundle.hibernate.framework.SessionManager;

public class MapperTracker implements SynchronousBundleListener {

    private static MapperTracker instance = null;

    public synchronized static void start() {
        if (instance == null) {
            instance = new MapperTracker();
            Activator.getContext().addBundleListener(instance);
        }
    }

    public synchronized static void stop() {
        if (instance != null) {
            Activator.getContext().removeBundleListener(instance);
            instance = null;
        }
    }

    private MapperTracker() {
        for (Bundle bundle : Activator.getContext().getBundles()) {
            bundleStarted(bundle);
        }

        SessionManager.instance.publishDaoHelpers();
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.STARTED) {
            bundleStarted(event.getBundle());
        } else if (event.getType() == BundleEvent.STOPPING) {
            bundleStopping(event.getBundle());
        }
    }

    private void bundleStopping(Bundle bundle) {
        MapperManager.instance.deleteMappers(bundle);
    }

    private void bundleStarted(Bundle bundle) {
        MapperManager.instance.addMappers(bundle);
    }

}
