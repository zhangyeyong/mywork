package com.zte.jbundle.home.plugin;

import java.io.File;

import org.osgi.framework.Bundle;

public class Plugin {

    public final Bundle bundle;
    public final File file;

    public Plugin(Bundle bundle, File file) {
        this.bundle = bundle;
        this.file = file;
    }

    public String getSymbolic() {
        return bundle.getSymbolicName();
    }

    public boolean getActive() {
        return bundle.getState() == Bundle.ACTIVE;
    }

}
