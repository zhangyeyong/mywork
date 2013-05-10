package com.zte.mcore.ui.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zte.mcore.cfg.Config;
import com.zte.mcore.cfg.ConfigManager;

public class ConfigAction {

    protected List<Map<String, Object>> list(HttpServletRequest req, HttpServletResponse resp) {
        String cfgName = req.getParameter("cfg");
        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        for (Config cfg : ConfigManager.getInstance().listConfigs(cfgName)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("clazz", cfg.getCfgName());
            String s = cfg.getJson();
            if (s.length() > 200) {
                s = s.substring(0, 200) + " ......";
            }
            map.put("json", s);
            ret.add(map);
        }

        return ret;
    }

    public boolean restoreDefault(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String clazz = req.getParameter("clazz");
        ConfigManager.getInstance().restoreDefault(clazz);
        return true;
    }

    public boolean saveValue(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String clazz = req.getParameter("clazz");
        String json = req.getParameter("json");
        ConfigManager.getInstance().saveValue(clazz, json);
        return true;
    }

    public String getJsonValue(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String clazz = req.getParameter("clazz");
        return ConfigManager.getInstance().getJsonValue(clazz);
    }

}
