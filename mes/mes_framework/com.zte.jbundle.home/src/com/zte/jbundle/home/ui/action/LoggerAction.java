package com.zte.jbundle.home.ui.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.zte.jbundle.home.log.LogFile;
import com.zte.jbundle.home.log.LogFileManager;
import com.zte.jbundle.home.ui.BizException;
import com.zte.jbundle.home.ui.JBundleConsole;
import com.zte.jbundle.home.utils.JBundleUitls;
import com.zte.jbundle.log.DailyAppender;

public class LoggerAction extends Action {

    public final static String URL = JBundleConsole.ROOT_URI + "/" + JBundleUitls.getNoPackageName(LoggerAction.class);

    protected List<LogFile> list(String logName) {
        String name = logName;
        return LogFileManager.getInstance().listLogFiles(name);
    }

    void delete(String logName) throws Exception {
        File deleting = new File(JBundleUitls.getLogFolder(), logName);
        String current = DailyAppender.getCurrentFile() == null ? "" : DailyAppender.getCurrentFile().getName();
        if (deleting.getName().equals(current)) {
            throw new BizException("Current logger file can't be deleted!");
        }
        if (deleting.exists()) {
            deleting.delete();
        }
    }

    void clear(String logName) throws Exception {
        File clearing = new File(JBundleUitls.getLogFolder(), logName);
        if (clearing.exists()) {
            new FileOutputStream(clearing).close();
        }
    }

    Download download(String logName) throws Exception {
        File file = new File(JBundleUitls.getLogFolder(), logName);
        File zipFile = new File(file.getParentFile(), JBundleUitls.uuid());
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        try {
            zos.setLevel(1);
            zos.putNextEntry(new ZipEntry(file.getName()));
            FileInputStream fis = new FileInputStream(file);
            try {
                byte[] buffer = new byte[1024 * 1024];
                for (int len; (len = fis.read(buffer)) > 0;) {
                    zos.write(buffer, 0, len);
                }
            } finally {
                fis.close();
            }
        } finally {
            zos.close();
        }

        Download ret = new Download();
        ret.setTitle("log.zip");
        ret.setFile(zipFile);
        ret.setDoneDelete(true);
        return ret;
    }

    void view(String logName) throws Exception {
        resp.reset();
        resp.setCharacterEncoding("utf-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.setHeader("Pragma", "no-cache");

        File file = new File(JBundleUitls.getLogFolder(), logName);
        if (file.length() >= 1024 * 1024 * 4) {
            resp.setContentType("text/html; charset=utf-8");
            PrintWriter out = resp.getWriter();
            out.println("<html><body><h2 style='text-align:center;'>The size of log is too large, please download it and view it!<h2></body></html>");
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
        try {
            resp.setContentType("text/plain; charset=utf-8");
            PrintWriter out = resp.getWriter();
            for (String s = null; (s = br.readLine()) != null;) {
                out.println(s);
            }
        } finally {
            br.close();
        }
    }

}
