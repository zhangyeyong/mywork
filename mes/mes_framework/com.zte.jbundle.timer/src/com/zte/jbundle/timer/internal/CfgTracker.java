package com.zte.jbundle.timer.internal;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;


public class CfgTracker implements SynchronousBundleListener {

    private TimerManager timerManager;

    public CfgTracker(BundleContext context, TimerManager timerManager) {
        this.timerManager = timerManager;
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getState() == Bundle.ACTIVE) {
                loadBundlerTimer(bundle);
            }
        }
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.STARTED) {
            loadBundlerTimer(event.getBundle());
        } else if (event.getType() == BundleEvent.STOPPING) {
            timerManager.shutdown(event.getBundle());
        }
    }

    private void loadBundlerTimer(Bundle bundle) {
        List<TimerCfgVo> cfgList = new ArrayList<TimerCfgVo>();
        Enumeration<?> e = bundle.findEntries("META-INF/timer", "*.timer.xml", false);
        while (e != null && e.hasMoreElements()) {
            URL url = (URL) e.nextElement();
            try {
                List<TimerCfgVo> eList = extractCfgList(bundle, url.openStream());
                cfgList.addAll(eList);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        for (int i = 0; i < cfgList.size();) {
            TimerCfgVo cfg = cfgList.get(i);
            String errMsg = checkTimerCfg(cfg);
            if (!TimerUtils.isBlank(errMsg)) {
                cfgList.remove(i);
                TimerLog.error(errMsg);
                continue;
            }

            timerManager.scheduleJob(bundle, cfg);
            i++;
        }
    }

    private String checkTimerCfg(TimerCfgVo cfg) {
        boolean hasErrors = false;
        StringBuilder ret = new StringBuilder("Cfg error: ").append(cfg);
        if (TimerUtils.isBlank(cfg.getMethod())) {
            ret.append("methed is null! ");
            hasErrors = true;
        }
        if (TimerUtils.isBlank(cfg.getService())) {
            ret.append("service is null! ");
            hasErrors = true;
        }
        if (cfg.getSecond() == null && TimerUtils.isBlank(cfg.getCron())) {
            ret.append("one of interval and cron must have value! ");
            hasErrors = true;
        }

        if (cfg.getSecond() != null && cfg.getSecond() <= 0) {
            ret.append("interval must be greater than 0! ");
            hasErrors = true;
        }

        if (hasErrors) {
            return ret.toString();
        } else {
            return null;
        }
    }

    private List<TimerCfgVo> extractCfgList(Bundle bundle, InputStream inputStream) throws Exception {
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(inputStream);
            List<TimerCfgVo> ret = new ArrayList<TimerCfgVo>();
            Element root = doc.getRootElement();
            for (Object o : root.elements("timer")) {
                Element e = (Element) o;
                String serviceId = e.attributeValue("service");
                String method = e.attributeValue("method");
                List<?> groups = e.elements("group");// 分组列项
                for (int k = 0; k < groups.size(); k++) {
                    Element eGroup = (Element) groups.get(k);
                    TimerCfgVo cfg = new TimerCfgVo();
                    cfg.setIdentifier(serviceId + "->" + method + "->group NO." + (k + 1));
                    cfg.setService(serviceId);
                    cfg.setMethod(method);

                    cfg.setCron(eGroup.attributeValue("cron"));
                    try {
                        String tmp = eGroup.attributeValue("second");
                        if (!TimerUtils.isBlank(tmp)) {
                            cfg.setSecond(Integer.parseInt(tmp));
                        }
                    } catch (Exception nothing) {
                    }

                    String attrArgs = eGroup.attributeValue("args");
                    if (!TimerUtils.isBlank(attrArgs)) {
                        String[] args = attrArgs.split(",");
                        for (String arg : args) {
                            cfg.getArgs().add(arg);
                        }
                    }
                    ret.add(cfg);
                }
            }
            return ret;
        } finally {
            inputStream.close();
        }
    }
}
