package com.zte.jbundle.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Eclipse工程基类，子类:OSGI工程、Web工程
 * 
 * @author PanJun
 * 
 */
public abstract class Project {

    public String name;
    public File folder;
    public String binFile;// 输出文件路径,jar或者war
    public String src;
    public boolean rootNode = true;
    public boolean builded = false;
    public Set<String> requires = new LinkedHashSet<String>();
    public Set<Project> depends = new LinkedHashSet<Project>();
    //所有
    public static List<String> allBuildXmls = new ArrayList<String>();

    @Override
    public String toString() {
        return "project:" + name + ",src:" + src;
    }

    abstract public void analyzeDepends(List<Project> projects);

    abstract public void doBuild() throws Exception;

    void build() {
        if (builded) {
            return;
        }
        for (Project b : depends) {
            b.build();
        }
        builded = true;

        try {
            doBuild();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
