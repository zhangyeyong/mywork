package com.zte.jbundle.home.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zte.jbundle.home.utils.JBundleUitls;

public final class Config {

    public final Class<?> clazz;
    public final Bundle from;
    private Object value;
    private String json;
    private String cfgName;
    Logger log = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("unchecked")
    public Config(Class<?> clazz, Object defaultValue, Bundle from) throws IOException {
        this.clazz = (Class<Object>) clazz;
        this.from = from;
        this.cfgName = from.getSymbolicName() + "-" + JBundleUitls.getNoPackageName(clazz);

        if (!loadFromFile()) {
            changeValue(defaultValue);
            saveToFile();
        }
    }

    public void saveToFile() {
        File cfgFile = new File(JBundleUitls.getCfgFolder(), cfgName);
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(cfgFile), "gbk"));
            try {
                pw.println(json);
            } finally {
                pw.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean loadFromFile() {
        File cfgFile = new File(JBundleUitls.getCfgFolder(), getCfgName());
        if (!cfgFile.exists()) {
            return false;
        }

        try {
            BufferedReader pw = new BufferedReader(new InputStreamReader(new FileInputStream(cfgFile), "gbk"));
            try {
                StringBuilder sbJson = new StringBuilder();
                for (String line = null; (line = pw.readLine()) != null;) {
                    sbJson.append(line).append("\n");
                }

                setJson(sbJson.toString());
            } finally {
                pw.close();
            }
            return true;
        } catch (IOException doNothing) {
            return false;
        }
    }

    public void deleteConfigFile() {
        File cfgFile = new File(JBundleUitls.getCfgFolder(), cfgName);
        if (cfgFile.exists()) {
            cfgFile.delete();
        }
    }

    public String getFromName() {
        return from.getSymbolicName();
    }

    public String getCfgName() {
        return cfgName;
    }

    public String getJson() {
        return json;
    }

    public Object getValue() {
        return value;
    }

    /**
     * 如果配置POJO类存在方法：<br>
     * void valueChanged(Object oldValue); 当配置值修改时，调用此value对象上的此方法，通知配置改变
     * 
     * @param oldValue
     */
    private void notifyValueChanged(Object oldValue) {
        try {
            Method valueChanged = null;
            Set<Method> methods = new HashSet<Method>(Arrays.asList(value.getClass().getMethods()));
            methods.addAll(Arrays.asList(value.getClass().getDeclaredMethods()));
            for (Method m : methods) {
                Class<?>[] prmTypes = m.getParameterTypes();
                if (m.getName().equalsIgnoreCase("valueChanged") && prmTypes.length == 1
                        && prmTypes[0].isInstance(value)) {
                    valueChanged = m;
                    break;
                }
            }

            if (valueChanged != null) {
                valueChanged.setAccessible(true);
                valueChanged.invoke(value, oldValue);
            }
        } catch (Exception e) {
            log.error("[!_!]jbundle modified config[" + cfgName + "] notify error!", e);
        }
    }

    private void changeValue(Object value) throws IOException {
        Object oldValue = this.value;
        this.json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(value);
        this.value = value;
        notifyValueChanged(oldValue);
    }

    public void setJson(String json) throws IOException {
        changeValue(new ObjectMapper().readValue(json, clazz));
    }

    public void restoreDefault() throws Exception {
        changeValue(clazz.newInstance());
        saveToFile();
    }

}
