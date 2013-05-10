package com.zte.jbundle.home.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zte.jbundle.home.utils.JBundleUitls;

public class PluginManager {

    static Logger log = LoggerFactory.getLogger(PluginManager.class);

    static class BundleMeta {

        public final String symbolic;
        public final String version;

        public BundleMeta(String symbolic, String version) {
            this.symbolic = symbolic;
            this.version = version;
        }

    }

    final BundleContext context;
    final Map<String, Plugin> plugins = new ConcurrentHashMap<String, Plugin>();
    final Set<String> stoppedBundles = new CopyOnWriteArraySet<String>();

    private static PluginManager instance = null;

    public static PluginManager getInstance() {
        return instance;
    }

    public PluginManager(BundleContext context) {
        instance = this;
        this.context = context;

        for (File pluginFile : JBundleUitls.getPluginFolder().listFiles()) {
            if (pluginFile.getName().endsWith(".jar")) {
                try {
                    installBundleJar(pluginFile);
                } catch (Exception e) {
                    continue;
                }
            }
        }
        stoppedBundles.addAll(JBundleUitls.readTextFile(JBundleUitls.getStoppedPlugins()));
    }

    private Plugin installBundleJar(File pluginFile) throws Exception {
        Bundle bundle = context.installBundle(pluginFile.toURI().toURL().toExternalForm());
        Plugin plugin = new Plugin(bundle, pluginFile);
        plugins.put(plugin.getSymbolic(), plugin);
        return plugin;
    }

    static BundleMeta getBundleMeta(File pluginFile) {
        try {
            JarFile jarFile = new JarFile(pluginFile);
            try {
                Attributes attr = jarFile.getManifest().getMainAttributes();
                return new BundleMeta(attr.getValue("Bundle-SymbolicName"), attr.getValue("Bundle-Version"));
            } finally {
                jarFile.close();
            }
        } catch (Exception nothing) {
            return null;
        }
    }

    public void startPlugins() {
        for (Plugin plugin : plugins.values()) {
            if (stoppedBundles.contains(plugin.getSymbolic())) {
                continue;
            }

            try {
                plugin.bundle.start();
            } catch (Exception e) {
                log.error("...start plugin [" + plugin.getSymbolic() + "] failed!", e);
            }
        }
    }

    public List<Plugin> listPlugins() {
        List<Plugin> ret = new ArrayList<Plugin>(plugins.values());
        Collections.sort(ret, new Comparator<Plugin>() {

            @Override
            public int compare(Plugin o1, Plugin o2) {
                return o1.getSymbolic().compareToIgnoreCase(o2.getSymbolic());
            }

        });
        return ret;
    }

    public Plugin getPluginBySymbolic(String symbolic) {
        return plugins.get(symbolic);
    }

    public void startPlugin(String symbolic) throws BundleException {
        Plugin plugin = getPluginBySymbolic(symbolic);
        if (plugin != null) {
            plugin.bundle.start();
            if (plugin.bundle.getState() == Bundle.ACTIVE) {
                stoppedBundles.remove(symbolic);
                JBundleUitls.writeTextFile(stoppedBundles, JBundleUitls.getStoppedPlugins());
            }
        }
    }

    public void stopPlugin(String symbolic) throws BundleException {
        Plugin plugin = getPluginBySymbolic(symbolic);
        if (plugin != null) {
            plugin.bundle.stop();
            if (plugin.bundle.getState() != Bundle.ACTIVE) {
                stoppedBundles.add(symbolic);
                JBundleUitls.writeTextFile(stoppedBundles, JBundleUitls.getStoppedPlugins());
            }
        }
    }

    public void deletePlugin(String symbolic) throws BundleException {
        Plugin plugin = getPluginBySymbolic(symbolic);
        if (plugin != null) {
            plugin.bundle.stop();
            plugin.bundle.uninstall();
            plugin.file.delete();
            plugins.remove(symbolic);
        }
    }

    public void updatePlugin(String symbolic) throws BundleException {
        Plugin plugin = getPluginBySymbolic(symbolic);
        if (plugin != null) {
            plugin.bundle.update();
        }
    }

    public void uploadPluginJar(List<File> jarFiles) throws Exception {
        List<Plugin> newPlugins = new ArrayList<Plugin>();
        List<Plugin> oldPlugins = new ArrayList<Plugin>();
        for (File jarFile : jarFiles) {
            BundleMeta meta = getBundleMeta(jarFile);
            if (meta == null) {
                continue;
            }

            Plugin oldPlugin = plugins.get(meta.symbolic);
            if (oldPlugin != null) {
                JBundleUitls.copyFile(jarFile, oldPlugin.file);
                InputStream is = new FileInputStream(oldPlugin.file);
                try {
                    oldPlugins.add(oldPlugin);
                } finally {
                    is.close();
                }
            } else {
                File bundleFile = new File(JBundleUitls.getPluginFolder(), meta.symbolic + ".jar");
                JBundleUitls.copyFile(jarFile, bundleFile);
                newPlugins.add(installBundleJar(bundleFile));
            }
        }

        for (Plugin plugin : oldPlugins) {
            plugin.bundle.update();
        }

        List<Plugin> startedPlugins = new ArrayList<Plugin>(newPlugins);
        startedPlugins.addAll(oldPlugins);
        for (Plugin plugin : startedPlugins) {
            try {
                if (!stoppedBundles.contains(plugin.getSymbolic())) {
                    plugin.bundle.start();
                }
            } catch (Throwable e) {
                log.error("[!_!]starting bundle[" + plugin.getSymbolic() + "] error!", e);
            }
        }
    }
}
