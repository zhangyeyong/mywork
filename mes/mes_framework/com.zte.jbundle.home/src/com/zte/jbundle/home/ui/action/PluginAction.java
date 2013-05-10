package com.zte.jbundle.home.ui.action;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.zte.jbundle.home.plugin.Plugin;
import com.zte.jbundle.home.plugin.PluginManager;
import com.zte.jbundle.home.ui.JBundleConsole;
import com.zte.jbundle.home.utils.JBundleUitls;

public class PluginAction extends Action {

    public final static String URL = JBundleConsole.ROOT_URI + "/" + JBundleUitls.getNoPackageName(PluginAction.class);

    protected List<Map<String, Object>> list() throws Exception {
        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        for (Plugin p : PluginManager.getInstance().listPlugins()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("symbolic", p.getSymbolic());
            map.put("active", p.getActive());
            ret.add(map);
        }
        return ret;
    }

    public void delete(String symbolic) throws Exception {
        PluginManager.getInstance().deletePlugin(symbolic);
    }

    public void change(String symbolic) throws Exception {
        Plugin plugin = PluginManager.getInstance().getPluginBySymbolic(symbolic);
        if (plugin != null) {
            if (plugin.getActive()) {
                PluginManager.getInstance().stopPlugin(symbolic);
            } else {
                PluginManager.getInstance().startPlugin(symbolic);
            }
        }
    }

    public void update(String symbolic) throws Exception {
        PluginManager.getInstance().updatePlugin(symbolic);
    }

    public void upload() throws Exception {
        File tempPath = JBundleUitls.getTempPath();
        try {
            List<File> jarFiles = uploadToTempPath(req, tempPath);
            PluginManager.getInstance().uploadPluginJar(jarFiles);
            resp.sendRedirect("/jbundle/plugin.html");
        } finally {
            JBundleUitls.deleteFile(tempPath);
        }
    }

    @SuppressWarnings("unchecked")
    private List<File> uploadToTempPath(HttpServletRequest req, File tempPath) throws Exception {
        List<File> ret = new ArrayList<File>();
        DiskFileItemFactory factory = new DiskFileItemFactory(1024 * 1024, tempPath);
        ServletFileUpload upload = new ServletFileUpload(factory);
        FileItem uploadFileItem = null;
        for (FileItem fi : (List<FileItem>) upload.parseRequest(req)) {
            String ext = JBundleUitls.fileExt(fi.getName()).toLowerCase();
            if (!fi.isFormField() && (ext.equals(".jar") || ext.equals(".zip"))) {
                uploadFileItem = fi;
                break;
            }
        }

        if (uploadFileItem != null) {
            String ext = JBundleUitls.fileExt(uploadFileItem.getName()).toLowerCase();
            File tempFile = new File(tempPath, "temp" + ext);
            uploadFileItem.write(tempFile);
            if (ext.equalsIgnoreCase(".zip")) {
                JBundleUitls.unzip(new FileInputStream(tempFile), new File(tempPath, "unzipped"));
            }

            loadJar(tempPath, ret);
        }
        return ret;
    }

    private void loadJar(File folder, List<File> targetList) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                loadJar(file, targetList);
            } else if (JBundleUitls.fileExt(file.getName()).equalsIgnoreCase(".jar")) {
                targetList.add(file);
            }
        }
    }

}
