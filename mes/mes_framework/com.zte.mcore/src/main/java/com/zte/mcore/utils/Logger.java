package com.zte.mcore.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 日志包装类
 * 
 * @author PanJun
 * 
 */
public class Logger {

    private Log innerlog;

    public static <T> Logger getLogger(Class<T> clz) {
        return new Logger(clz);
    }

    private <T> Logger(Class<T> clz) {
        innerlog = LogFactory.getLog(clz);
    }

    private String nvl(Object msg) {
        return msg == null ? "null" : msg.toString();
    }

    private String fmt(Object msg, Object... args) {
        String s = nvl(msg);
        if (args.length == 0) {
            return s;
        }
        return String.format(s, args);
    }

    public void info(Object msg) {
        innerlog.info(nvl(msg));
    }

    public void info(Object msg, Throwable thr) {
        innerlog.info(nvl(msg), thr);
    }

    /**
     * Info级,占位符输出
     * 
     * @param msg
     * @param args
     */
    public void ifmt(Object msg, Object... args) {
        innerlog.info(fmt(msg, args));
    }

    /**
     * Info级,占位符输出,带堆栈
     * 
     * @param msg
     * @param thr
     * @param args
     */
    public void isfmt(Object msg, Throwable thr, Object... args) {
        innerlog.info(fmt(msg, args), thr);
    }

    public void debug(Object msg) {
        innerlog.debug(nvl(msg));
    }

    public void debug(Object msg, Throwable thr) {
        innerlog.debug(nvl(msg), thr);
    }

    /**
     * Debug级,占位符输出
     * 
     * @param msg
     * @param args
     */
    public void dfmt(Object msg, Object... args) {
        innerlog.debug(fmt(msg, args));
    }

    /**
     * Debug级,占位符输出,带堆栈
     * 
     * @param msg
     * @param thr
     * @param args
     */
    public void dsfmt(Object msg, Throwable thr, Object... args) {
        innerlog.debug(fmt(msg, args), thr);
    }

    public void warn(Object msg) {
        innerlog.warn(nvl(msg));
    }

    public void warn(Object msg, Throwable thr) {
        innerlog.warn(nvl(msg), thr);
    }

    /**
     * Warn级,占位符输出
     * 
     * @param msg
     * @param args
     */
    public void wfmt(Object msg, Object... args) {
        innerlog.warn(fmt(msg, args));
    }

    /**
     * Warn级,占位符输出,带堆栈
     * 
     * @param msg
     * @param thr
     * @param args
     */
    public void wsfmt(Object msg, Throwable thr, Object... args) {
        innerlog.warn(fmt(msg, args), thr);
    }

    public void error(Object msg) {
        innerlog.error(nvl(msg));
    }

    public void error(Object msg, Throwable thr) {
        innerlog.error(nvl(msg), thr);
    }

    /**
     * Error级,占位符输出
     * 
     * @param msg
     * @param args
     */
    public void efmt(Object msg, Object... args) {
        innerlog.error(fmt(msg, args));
    }

    /**
     * Error级,占位符输出,带堆栈
     * 
     * @param msg
     * @param thr
     * @param args
     */
    public void esfmt(Object msg, Throwable thr, Object... args) {
        innerlog.error(fmt(msg, args), thr);
    }
}
