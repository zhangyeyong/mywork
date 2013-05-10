package com.zte.mcore.ui.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zte.mcore.log.DailyAppender;
import com.zte.mcore.log.LogFileManager;
import com.zte.mcore.ui.BizException;
import com.zte.mcore.utils.FileU;
import com.zte.mcore.utils.LogFile;
import com.zte.mcore.utils.McoreU;
import com.zte.mcore.utils.StringU;

public class LoggerAction {

    protected List<LogFile> list(HttpServletRequest req) {
        String logName = req.getParameter("log");
        String name = logName;
        return LogFileManager.getInstance().listLogFiles(name);
    }

    boolean delete(HttpServletRequest req) throws Exception {
        String logName = req.getParameter("log");
        File deleting = new File(McoreU.getLogFolder(), logName);
        String current = DailyAppender.getCurrentFile() == null ? "" : DailyAppender.getCurrentFile().getName();
        if (deleting.getName().equals(current)) {
            throw new BizException("Current logger file can't be deleted!");
        }
        if (deleting.exists()) {
            deleting.delete();
        }
        return true;
    }

    boolean clear(HttpServletRequest req) throws Exception {
        String logName = req.getParameter("log");
        File clearing = new File(logFolder(), logName);
        if (clearing.exists()) {
            new FileOutputStream(clearing).close();
        }
        return true;
    }

    void download(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String logName = req.getParameter("log");
        File file = new File(logFolder(), logName);
        File zipFile = new File(file.getParentFile(), StringU.uuid());
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

        resp.reset();
        logName = FileU.fileExt(logName, ".zip");
        resp.setHeader("Content-disposition", "attachment; filename=" + logName);
        resp.setContentType(req.getSession().getServletContext().getMimeType(logName));
        try {
            FileInputStream in = new FileInputStream(zipFile);
            OutputStream out = resp.getOutputStream();
            try {
                byte[] buffer = new byte[1024 * 1024];
                for (int len; (len = in.read(buffer)) > 0;) {
                    out.write(buffer, 0, len);
                }
            } finally {
                out.close();
                in.close();
            }
        } finally {
            zipFile.delete();
        }
    }

    void view(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.reset();
        resp.setCharacterEncoding("utf-8");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.setHeader("Pragma", "no-cache");

        String logName = req.getParameter("log");
        File file = new File(logFolder(), logName);
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

    private String logFolder() {
        return McoreU.getLogFolder().getAbsolutePath();
    }

    public boolean ifCheckLogin() {
        return false;
    }
}
