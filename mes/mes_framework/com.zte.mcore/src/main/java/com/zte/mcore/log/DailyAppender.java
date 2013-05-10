package com.zte.mcore.log;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.DailyRollingFileAppender;

/**
 * Log4j Appender支持最多保留多少个文件
 * 
 * @author PanJun
 * 
 */
public class DailyAppender extends DailyRollingFileAppender {

    private int maxCount = 20;

    public int getMaxCount() {
        return maxCount;
    }

    private static File currentFile;

    public static File getCurrentFile() {
        return currentFile;
    }

    public void setMaxCount(int maxCount) {
        if (maxCount < 1) {
            maxCount = 1;
        }
        this.maxCount = maxCount;
    }

    @Override
    protected void reset() {
        currentFile = new File(fileName);
        String prefix = currentFile.getName();
        File folder = currentFile.getParentFile();
        List<File> logFiles = new LinkedList<File>();
        for (File file : folder.listFiles()) {
            if (file.getName().startsWith(prefix) && !file.getName().equals(prefix)) {
                logFiles.add(file);
            }
        }

        if (logFiles.size() > maxCount) {
            Collections.sort(logFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    long m1 = o1.lastModified();
                    long m2 = o2.lastModified();
                    if (m1 > m2) {
                        return 1;
                    } else if (m1 < m2) {
                        return -1;
                    }
                    return 0;
                }
            });

            while (logFiles.size() > maxCount) {
                File deletingFile = logFiles.remove(logFiles.size() - 1);
                deletingFile.delete();
            }
        }

        super.reset();
    }
}
