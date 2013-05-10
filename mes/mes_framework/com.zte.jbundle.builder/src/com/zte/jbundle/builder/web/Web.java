package com.zte.jbundle.builder.web;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.zte.jbundle.builder.Config;
import com.zte.jbundle.builder.Project;
import com.zte.jbundle.builder.osgi.Bundle;

public class Web extends Project {

    public File webRoot;
    /** 用户设置的eclipse:环境变量(ClassPath variable) */
    public Set<String> varLibs = new LinkedHashSet<String>();
    /** 最终依赖的jar包 */
    public Set<String> depJars = new LinkedHashSet<String>();

    @Override
    public void analyzeDepends(List<Project> projects) {
        // 根据Require-Bundle解析组件依赖关系
        for (String reqName : requires) {
            for (Project checked : projects) {
                if (checked.name.equals(reqName) && checked instanceof Bundle) {
                    checked.rootNode = false;
                    depends.add(checked);
                }
            }
        }

        for (String lib : varLibs) {
            String varLibPath = Config.getVarlibPath(lib);
            if (null != varLibPath) {
                depJars.add(varLibPath);
            }
        }
    }

    @Override
    public void doBuild() throws Exception {
        AntWeb.execute(this);
    }

    @Override
    public String toString() {
        return "Web project:" + name + ",src:" + src;
    }

}
