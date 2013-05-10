package com.zte.jbundle.timer;

import com.zte.jbundle.api.OsgiService;
import com.zte.jbundle.timer.internal.TimerLog;

@OsgiService
public class DemoTimer implements IDemoTimer {

    static boolean printed = false;

    @Override
    public void foo(String arg) {
        if (!printed) {
            printed = true;
            TimerLog.error("--DemoTimer executing....");
        }
    }

}
