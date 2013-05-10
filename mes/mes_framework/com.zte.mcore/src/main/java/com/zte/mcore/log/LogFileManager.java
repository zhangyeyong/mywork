package com.zte.mcore.log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.zte.mcore.utils.LogFile;
import com.zte.mcore.utils.McoreU;

public class LogFileManager {

    private static LogFileManager instance = new LogFileManager();

    public static LogFileManager getInstance() {
        return instance;
    }

    private LogFileManager() {
        instance = this;
    }

    public List<LogFile> listLogFiles(String name) {
        name = name == null ? "" : name.trim().toLowerCase();
        List<LogFile> fileList = new ArrayList<LogFile>();
        for (File f : McoreU.getLogFolder().listFiles()) {
            if (!f.isDirectory() && (name.length() == 0 || f.getName().toLowerCase().contains(name))) {
                fileList.add(new LogFile(f));
            }
        }
        Collections.sort(fileList, new Comparator<LogFile>() {

            @Override
            public int compare(LogFile o1, LogFile o2) {
                if (o1.getModified() > o2.getModified()) {
                    return -1;
                } else if (o1.getModified() < o2.getModified()) {
                    return 1;
                }
                return 0;
            }
        });

        return fileList;
    }
}
