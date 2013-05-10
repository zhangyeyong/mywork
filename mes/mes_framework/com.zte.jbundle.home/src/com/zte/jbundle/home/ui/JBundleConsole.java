package com.zte.jbundle.home.ui;

import org.osgi.service.http.HttpService;

import com.zte.jbundle.home.utils.JBundleUitls;

public class JBundleConsole {

    public static final String ROOT_URI = "/jbundle";
    public static final String PAGE_URI = ROOT_URI + "/page";
    private final JBundleServlet uiServlet;

    public JBundleConsole() {
        this.uiServlet = new JBundleServlet();
    }

    public void open() throws Exception {
        HttpService http = JBundleUitls.getService(HttpService.class);
        http.registerResources(PAGE_URI, JBundleUitls.getPackageUri(getClass()) + "/page", null);
        http.registerServlet(ROOT_URI, this.uiServlet, null, null);
    }

    public void close() {
        HttpService http = JBundleUitls.getService(HttpService.class);
        if (http != null) {
            http.unregister(ROOT_URI);
            http.unregister(PAGE_URI);
        }
        uiServlet.destroy();
    }

}
