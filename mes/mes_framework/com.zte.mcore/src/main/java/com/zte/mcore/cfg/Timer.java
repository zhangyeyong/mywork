package com.zte.mcore.cfg;

import com.zte.mcore.timer.TimerManager;

public class Timer {

    private boolean started = false;
    private static TimerManager timerManager = new TimerManager();
    private static String[] names = null;
    private static Class<?>[] classes = null;
    private static Object[] instances = null;

    public boolean getStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void valueChanged(Timer timer) {
        if (names != null || classes != null || instances != null) {
            timerManager.shutdown();
            if (started) {
                timerManager.startup(names, classes, instances);
            }
        }
    }

    public void mcoreStarted(String[] names, Class<?>[] classes, Object[] instances) {
        Timer.names = names;
        Timer.classes = classes;
        Timer.instances = instances;
        timerManager.shutdown();
        if (started) {
            timerManager.startup(names, classes, instances);
        }
    }

}
