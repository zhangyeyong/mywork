package com.zte.mcore.cfg;

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

import com.zte.mcore.utils.Logger;
import com.zte.mcore.utils.McoreU;

public final class Config {

    public final Class<?> clazz;
    private Object value;
    private String json;
    private String cfgName;
    Logger log = Logger.getLogger(getClass());

    public Config(Class<?> clazz, Object defaultValue) throws IOException {
        this.clazz = clazz;
        this.cfgName = clazz.getName();

        if (!loadFromFile()) {
            changeValue(defaultValue);
            saveToFile();
        }
    }

    public void saveToFile() {
        File cfgFile = new File(cfgFolder(), cfgName);
        try {
            cfgFile.getParentFile().mkdirs();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(cfgFile), "utf-8"));
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
        File cfgFile = new File(cfgFolder(), getCfgName());
        if (!cfgFile.exists()) {
            return false;
        }

        try {
            BufferedReader pw = new BufferedReader(new InputStreamReader(new FileInputStream(cfgFile), "utf-8"));
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
        File cfgFile = new File(cfgFolder(), cfgName);
        if (cfgFile.exists()) {
            cfgFile.delete();
        }
    }

    private File cfgFolder() {
        return McoreU.getCfgFolder();
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
