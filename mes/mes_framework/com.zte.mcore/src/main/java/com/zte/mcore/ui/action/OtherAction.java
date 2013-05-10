package com.zte.mcore.ui.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import com.zte.mcore.utils.Logger;
import com.zte.mcore.utils.McoreU;
import com.zte.mcore.utils.StringU;

public class OtherAction {

    static class LinkMeta {
        public String className;
        public Object instance;
        public Method execute;
        public String title;
    }

    static Logger log = Logger.getLogger(OtherAction.class);
    public static List<LinkMeta> linkMetaList = null;
    public static ReentrantLock lock = new ReentrantLock();

    /**
     * 返回列表格式:
     * 
     * <pre>
     * 2) title 标题
     * 3) clazz 连接类名
     * </pre>
     * 
     * @param req
     * @param resp
     * @return
     */
    List<Map<String, String>> list() {
        List<LinkMeta> metaList = getLinkMetas();
        List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
        for (LinkMeta meta : metaList) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("title", meta.title);
            map.put("clazz", meta.className);
            ret.add(map);
        }
        return ret;
    }

    boolean execute(HttpServletRequest req) throws Exception {
        String clsName = req.getParameter("clazz");

        LinkMeta execMeta = null;
        List<LinkMeta> metaList = getLinkMetas();
        for (LinkMeta meta : metaList) {
            if (meta.className.equals(clsName)) {
                execMeta = meta;
                break;
            }
        }

        if (execMeta == null) {
            return false;
        }

        execMeta.execute.invoke(execMeta.instance);
        return true;
    }

    private static synchronized List<LinkMeta> getLinkMetas() {
        if (linkMetaList != null) {
            return linkMetaList;
        }

        linkMetaList = new ArrayList<LinkMeta>();
        loadLinkClasses(linkMetaList);
        return linkMetaList;
    }

    private static void loadLinkClasses(List<LinkMeta> classList) {
        List<String> classNames = McoreU.loadClasses(Arrays.asList("com.zte.mcore.ui.link"), false);

        for (int i = classNames.size() - 1; i > -1; i--) {
            String clsName = classNames.get(i);
            if (clsName.contains("$")) {
                continue;
            }

            try {
                LinkMeta meta = new LinkMeta();
                meta.instance = Class.forName(clsName).newInstance();
                meta.className = clsName;
                Method getTitle = null;
                for (Method m : meta.instance.getClass().getMethods()) {
                    Class<?>[] paramTypes = m.getParameterTypes();
                    if (m.getName().equalsIgnoreCase("getTitle") && paramTypes.length == 0
                            && String.class.isAssignableFrom(m.getReturnType())) {
                        getTitle = m;
                    }

                    if (m.getName().equalsIgnoreCase("execute") && paramTypes.length == 0) {
                        meta.execute = m;
                    }

                    if (meta.execute != null && getTitle != null) {
                        meta.title = (String) getTitle.invoke(meta.instance);
                        if (StringU.hasText(meta.title)) {
                            classList.add(meta);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("[!_!] Failed to initialize [" + clsName + "] link class instance. Ignored!" + e);
            }
        }
    }

}
