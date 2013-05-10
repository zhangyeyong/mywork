package com.zte.jbundle.api;

/**
 * 日志记录服务的工厂接口
 * 
 * @author PanJun
 * 
 */
public class LogFactory {

    /**
     * 日志记录服务的工厂接口，为LogFactory支持
     * 
     * @author PanJun
     * 
     */
    public static interface ILogFactorable {

        public <T> Log getLog(Class<T> clazz);

        public <T> Log getLog(String catagory);

    }

    static class NullLog implements Log {

        @Override
        public void info(String msg, Object... args) {
            System.out.printf("[*_*]info" + msg + "\n", args);
        }

        @Override
        public void info(Throwable thr, String msg, Object... args) {
            System.out.printf("[*_*]info" + msg + "\n", args);

        }

        @Override
        public void debug(String msg, Object... args) {
            System.out.printf("[*_*]debug" + msg + "\n", args);
        }

        @Override
        public void debug(Throwable thr, String msg, Object... args) {
            System.out.printf("[*_*]debug" + msg + "\n", args);
        }

        @Override
        public void warn(String msg, Object... args) {
            System.out.printf("[*_*]warn" + msg + "\n", args);
        }

        @Override
        public void warn(Throwable thr, String msg, Object... args) {
            System.out.printf("[*_*]warn" + msg + "\n", args);
        }

        @Override
        public void error(String msg, Object... args) {
            System.out.printf("[*_*]error" + msg + "\n", args);
        }

        @Override
        public void error(Throwable thr, String msg, Object... args) {
            System.out.printf("[*_*]error" + msg + "\n", args);
        }

    }

    private static ILogFactorable factorable = new ILogFactorable() {

        @Override
        public <T> Log getLog(String catagory) {
            return new NullLog();
        }

        @Override
        public <T> Log getLog(Class<T> clazz) {
            return new NullLog();
        }

    };

    public static <T> Log getLog(Class<T> clazz) {
        return factorable.getLog(clazz);
    }

    public static <T> Log getLog(String catagory) {
        return factorable.getLog(catagory);
    }

    public static ILogFactorable getFactorable() {
        return factorable;
    }

    public static void setFactorable(ILogFactorable factorable) {
        LogFactory.factorable = factorable;
    }

}