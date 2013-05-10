package com.zte.jbundle.builder.osgi;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.zte.jbundle.builder.Project;

public class Bundle extends Project {

    public Set<String> imports = new LinkedHashSet<String>();
    public Set<String> exports = new LinkedHashSet<String>();

    @Override
    public String toString() {
        return "Bundle:" + name + ",src:" + src;
    }

    public void analyzeDepends(List<Project> projects) {
        // 根据imports,exports包解析组件依赖关系
        for (String impPkg : imports) {
            for (Project checked : projects) {
                if (checked == this || !(checked instanceof Bundle)) {
                    continue;
                }

                Bundle bundle = (Bundle) checked;
                if (bundle.exports.contains(impPkg)) {
                    checked.rootNode = false;
                    depends.add(checked);
                }
            }
        }

        // 根据Require-Bundle解析组件依赖关系
        for (String symbolic : requires) {
            for (Project checked : projects) {
                if (checked.name.equals(symbolic) && checked instanceof Bundle) {
                    checked.rootNode = false;
                    depends.add(checked);
                }
            }
        }
    }

    @Override
    public void doBuild() throws Exception {
        AntOsgi.execute(this);
    }

}
