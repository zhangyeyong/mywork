package com.zte.jbundle.hibernate.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapperManager {

    public static final MapperManager instance = new MapperManager();

    private MapperManager() {
        // Singleton
    }

    Logger log = LoggerFactory.getLogger(getClass());
    private List<MapperMeta> mapperList = new ArrayList<MapperMeta>();
    private boolean changed = true;

    public void deleteMappers(Bundle from) {
        synchronized (mapperList) {
            for (int i = mapperList.size() - 1; i > -1; i--) {
                MapperMeta mapper = mapperList.get(i);
                if (mapper.from.getSymbolicName().equals(from.getSymbolicName())) {
                    mapperList.remove(i);
                    changed = true;
                }
            }
        }
    }

    public void addMappers(Bundle from, List<String> newMapperList) {
        synchronized (mapperList) {
            if (!newMapperList.isEmpty()) {
                deleteMappers(from);
            }
            for (String xml : newMapperList) {
                mapperList.add(new MapperMeta(xml, from));
                changed = true;
            }
        }
    }

    public List<MapperMeta> getAllMetas() {
        synchronized (mapperList) {
            return new ArrayList<MapperMeta>(mapperList);
        }
    }

    public void addMappers(Bundle bundle) {
        String idPlace = "#ZTE_ID#";
        List<String> bundleMappers = new ArrayList<String>();
        Enumeration<?> e = bundle.findEntries("/META-INF/hbm", "*.hbm.xml", false);
        while (e != null && e.hasMoreElements()) {
            URL url = (URL) e.nextElement();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                try {
                    StringBuilder sbXml = new StringBuilder();
                    for (String s; (s = br.readLine()) != null;) {
                        int i = s.indexOf(idPlace);
                        if (i > -1) {
                            //Id产生器
                            sbXml.append("<generator class=\"org.hibernate.id.ZteHiloIdGenerator\" />").append("\n");
                        } else {
                            sbXml.append(s).append("\n");
                        }
                    }
                    bundleMappers.add(sbXml.toString());
                } finally {
                    br.close();
                }
            } catch (IOException ex) {
                String name = new File(url.getPath()).getName();
                String fmt = "[!_!]Jbundle parse[%s-->%s] hibernate mapper file failed!";
                log.error(String.format(fmt, bundle.getSymbolicName(), name), ex);
            }
        }

        addMappers(bundle, bundleMappers);
    }

    public boolean isChanged() {
        synchronized (mapperList) {
            return changed;
        }
    }

    public void finishChange() {
        synchronized (mapperList) {
            this.changed = false;
        }
    }

}
