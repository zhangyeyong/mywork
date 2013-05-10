package com.zte.jbundle.home.ui.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zte.jbundle.home.config.Config;
import com.zte.jbundle.home.config.ConfigManager;
import com.zte.jbundle.home.ui.JBundleConsole;
import com.zte.jbundle.home.utils.JBundleUitls;

public class ConfigAction extends Action {

    public final static String URL = JBundleConsole.ROOT_URI + "/" + JBundleUitls.getNoPackageName(ConfigAction.class);

    protected List<Map<String, Object>> list(String cfgName) {
        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        for (Config cfg : ConfigManager.getInstance().listConfigs(cfgName)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("clazz", cfg.getCfgName());
            map.put("from", cfg.getFromName());
            String s = cfg.getJson();
            if (s.length() > 200) {
                s = s.substring(0, 200) + " ......";
            }
            map.put("json", s);
            ret.add(map);
        }

        return ret;
    }

    public void restoreDefault(String clazz) throws Exception {
        ConfigManager.getInstance().restoreDefault(clazz);
    }

    public void saveValue(String clazz) throws Exception {
        String json = req.getParameter("json");
        ConfigManager.getInstance().saveValue(clazz, json);
    }

    public String getJsonValue(String clazz) throws Exception {
        return ConfigManager.getInstance().getJsonValue(clazz);
    }

}
