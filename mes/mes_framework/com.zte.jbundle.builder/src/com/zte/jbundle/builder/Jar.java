package com.zte.jbundle.builder;

public class Jar {

    public String jarFile;
    public String remoteUrl;

    public Jar(String jarFile, String remoteUrl) {
        this.jarFile = jarFile;
        this.remoteUrl = remoteUrl;
    }

    @Override
    public String toString() {
        return jarFile + ":" + remoteUrl;
    }

}
