package com.zte.jbundle.api;

/**
 * 日志记录服务接口，调用程序通过OsgiReference引出此服务进行日志记录<br/>
 * 所有的方法支持对msg进行String.format操作，以方便打印出业务对象信息
 * 
 * @author PanJun
 * 
 */
public interface Log {

    public void info(String msg, Object... args);

    public void info(Throwable thr, String msg, Object... args);

    public void debug(String msg, Object... args);

    public void debug(Throwable thr, String msg, Object... args);

    public void warn(String msg, Object... args);

    public void warn(Throwable thr, String msg, Object... args);

    public void error(String msg, Object... args);

    public void error(Throwable thr, String msg, Object... args);

}