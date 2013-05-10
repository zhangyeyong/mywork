package com.zte.jbundle.timer.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerLog {

    private static Logger log = LoggerFactory.getLogger("iam-timer");

    public static void error(String msg) {
        log.error("--timer: " + msg);
    }

    public static void error(String msg, Throwable e) {
        log.error("--timer: " + msg, e);
    }

}
