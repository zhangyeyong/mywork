package com.zte.mcore.hibernate.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.zte.mcore.utils.Logger;
import com.zte.mcore.utils.McoreU;

public class MapperManager {

    private static String ID_GENERATOR = "<generator class=\"" + ZteHiloIdGenerator.class.getName() + "\" />\n";
    private static final List<String> HBM_PACKS = Arrays.asList("mcore.hbm");
    private static Logger log = Logger.getLogger(MapperManager.class);
    private static List<String> mapperList = null;

    public static List<String> allMappers() {
        if (mapperList != null) {
            return mapperList;
        }

        synchronized (MapperManager.class) {
            if (mapperList != null) {
                return mapperList;
            }

            final List<String> hbmXmlList = new ArrayList<String>();
            String idPlace = "#ZTE_ID#";
            List<String> hbmFiles = new ArrayList<String>();
            McoreU.loadFileResources(HBM_PACKS, ".hbm.xml", false, hbmFiles);
            McoreU.loadJarResources(HBM_PACKS, ".hbm.xml", false, hbmFiles);

            for (String hbm : hbmFiles) {
                if (!hbm.startsWith("/")) {
                    hbm = "/" + hbm;
                }
                URL url = MapperManager.class.getResource(hbm);
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                    try {
                        StringBuilder sbXml = new StringBuilder();
                        for (String s; (s = br.readLine()) != null;) {
                            int i = s.indexOf(idPlace);
                            if (i > -1) {
                                // Id产生器
                                sbXml.append(ID_GENERATOR);
                            } else {
                                sbXml.append(s).append("\n");
                            }
                        }
                        hbmXmlList.add(sbXml.toString());
                    } finally {
                        br.close();
                    }
                } catch (IOException ex) {
                    String name = new File(url.getPath()).getName();
                    String fmt = "[!_!]Mcore parse[%s] hibernate mapper file failed!";
                    log.error(String.format(fmt, name), ex);
                }
            }//for
            
            mapperList = hbmXmlList;
            return hbmXmlList;
        }
    }

}
