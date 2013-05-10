package com.zte.jbundle.home.ui.action;

import java.io.File;

/**
 * 下载文件请求返回Model
 * 
 * @author PanJun
 * 
 */
public class Download {

    /** 现在文件对象 */
    private File file;
    /** 下载标题 */
    private String title;
    /** 下载完成是否删除 file */
    private boolean doneDelete;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDoneDelete() {
        return doneDelete;
    }

    public void setDoneDelete(boolean doneDelete) {
        this.doneDelete = doneDelete;
    }

}
