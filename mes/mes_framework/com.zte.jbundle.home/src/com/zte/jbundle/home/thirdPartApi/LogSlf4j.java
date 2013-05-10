package com.zte.jbundle.home.thirdPartApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zte.jbundle.api.Log;

public class LogSlf4j implements Log {

    private final Logger log;

    public <T> LogSlf4j(Class<T> clazz) {
        log = LoggerFactory.getLogger(clazz);
    }

    public <T> LogSlf4j(String name) {
        log = LoggerFactory.getLogger(name);
    }

    @Override
    public void info(String msg, Object... args) {
        log.info(String.format(msg, args));
    }

    @Override
    public void info(Throwable thr, String msg, Object... args) {
        log.info(String.format(msg, args), thr);
    }

    @Override
    public void debug(String msg, Object... args) {
        log.debug(String.format(msg, args));
    }

    @Override
    public void debug(Throwable thr, String msg, Object... args) {
        log.debug(String.format(msg, args), thr);
    }

    @Override
    public void warn(String msg, Object... args) {
        log.warn(String.format(msg, args));
    }

    @Override
    public void warn(Throwable thr, String msg, Object... args) {
        log.warn(String.format(msg, args), thr);
    }

    @Override
    public void error(String msg, Object... args) {
        log.error(String.format(msg, args));
    }

    @Override
    public void error(Throwable thr, String msg, Object... args) {
        log.error(String.format(msg, args), thr);
    }

}
