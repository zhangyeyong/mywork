package com.zte.mcore.timer;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.zte.mcore.utils.Logger;

@DisallowConcurrentExecution
public class JobWrapper implements Job {

    Logger log = Logger.getLogger(getClass());

    @Override
    public void execute(JobExecutionContext jobCtx) throws JobExecutionException {
        TimerCfgVo cfg = (TimerCfgVo) jobCtx.getMergedJobDataMap().get("cfg");
        if (cfg.getTarget() == null || cfg.getMethod() == null) {
            return;
        }

        try {
            cfg.getMethod().invoke(cfg.getTarget(), cfg.getArgs());
        } catch (Exception e) {
            log.error(cfg + " failed to execute! ", e);
        }
    }

}
