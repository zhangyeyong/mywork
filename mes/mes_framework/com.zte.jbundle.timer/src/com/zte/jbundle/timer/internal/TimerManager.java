package com.zte.jbundle.timer.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class TimerManager {

    private final Scheduler scheduler = getScheduler();
    private Map<Long, List<JobDetail>> jobMap = new HashMap<Long, List<JobDetail>>();

    private Scheduler getScheduler() {
        SchedulerFactory factory = new StdSchedulerFactory();
        try {
            Scheduler ret = factory.getScheduler();
            ret.start();
            return ret;
        } catch (SchedulerException e) {
            TimerLog.error("Initialize Quatz scheduler error:" + e);
            return null;
        }
    }

    private static long jobName = 1;

    private synchronized String getJobName() {
        return Long.toString(jobName++);
    }

    private static long triggerName = 1;

    private synchronized String getTriggerName() {
        return Long.toString(triggerName++);
    }

    private Trigger parseTrigger(TimerCfgVo cfg) throws Exception {
        if (TimerUtils.isBlank(cfg.getCron())) {
            SimpleTrigger trigger = new SimpleTrigger(getTriggerName());
            trigger.setRepeatInterval(cfg.getSecond() * 1000);
            trigger.setRepeatCount(-1);
            return trigger;
        } else {
            CronTrigger trigger = new CronTrigger(getTriggerName());
            trigger.setCronExpression(cfg.getCron());
            return trigger;
        }
    }

    private List<JobDetail> getJobDetailList(Long bundleId) {
        List<JobDetail> ret = jobMap.get(bundleId);
        if (ret == null) {
            ret = new ArrayList<JobDetail>();
            jobMap.put(bundleId, ret);
        }
        return ret;
    }

    public void scheduleJob(Bundle bundle, TimerCfgVo cfg) {
        JobDetail job = new JobDetail(getJobName(), JobWrapper.class);
        job.getJobDataMap().put("cfg", cfg);
        try {
            scheduler.scheduleJob(job, parseTrigger(cfg));
            synchronized (jobMap) {
                getJobDetailList(bundle.getBundleId()).add(job);
            }
        } catch (Exception e) {
            TimerLog.error("scheduleJob error: " + cfg + "," + e);
        }
    }

    public void shutdown(Bundle bundle) {
        synchronized (jobMap) {
            List<JobDetail> jobList = getJobDetailList(bundle.getBundleId());
            for (JobDetail job : jobList) {
                try {
                    scheduler.deleteJob(job.getName(), job.getGroup());
                } catch (SchedulerException nil) {
                    nil.printStackTrace();
                }
            }
            jobList.clear();
        }
    }

    public void shutdown() throws SchedulerException {
        scheduler.shutdown(false);
    }

}
