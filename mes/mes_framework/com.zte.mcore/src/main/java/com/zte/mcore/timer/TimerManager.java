package com.zte.mcore.timer;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.zte.mcore.utils.Logger;
import com.zte.mcore.utils.McoreU;
import com.zte.mcore.utils.StringU;

public class TimerManager {

    private static final List<String> TIMER_PACKS = Arrays.asList("mcore.timer");
    private List<TimerCfgVo> timerCfgList = null;
    Logger log = Logger.getLogger(getClass());
    private Scheduler scheduler = getScheduler();

    private Scheduler getScheduler() {
        SchedulerFactory factory = new StdSchedulerFactory();
        try {
            Scheduler ret = factory.getScheduler();
            ret.start();
            return ret;
        } catch (SchedulerException e) {
            log.error("Initialize Quatz scheduler error:" + e);
            return null;
        }
    }

    private synchronized List<TimerCfgVo> listTimeCfgVo() {
        if (timerCfgList == null) {
            timerCfgList = new ArrayList<TimerCfgVo>();

            List<String> timerFiles = new ArrayList<String>();
            McoreU.loadFileResources(TIMER_PACKS, ".timer.xml", false, timerFiles);
            McoreU.loadJarResources(TIMER_PACKS, ".timer.xml", false, timerFiles);

            for (String path : timerFiles) {
                try {
                    InputStream stream = TimerManager.class.getResource("/" + path).openStream();
                    loadTimerCfgs(stream, timerCfgList);
                } catch (Exception e) {
                    log.efmt("[!_!]Failed to load %s as timer configuration! %s", path, e);
                }
            }
        }
        return timerCfgList;
    }

    private void loadTimerCfgs(InputStream inputStream, List<TimerCfgVo> cfgList) throws Exception {
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(inputStream);
            Element root = doc.getRootElement();
            for (Object o : root.elements("timer")) {
                Element e = (Element) o;
                String className = e.attributeValue("class");
                String methodName = e.attributeValue("method");
                List<?> groups = e.elements("group");// 分组列项
                for (int k = 0; k < groups.size(); k++) {
                    Element eGroup = (Element) groups.get(k);
                    TimerCfgVo cfg = new TimerCfgVo();
                    cfg.setIdentifier(className + "->" + methodName + "->group NO." + (k + 1));
                    try {
                        cfg.setClazz(Class.forName(className));
                    } catch (Exception ex) {
                        log.efmt("[!_!]Failed to intitialize timer class %s!", cfg.getIdentifier());
                        continue;
                    }
                    cfg.setMethodName(methodName);

                    cfg.setCron(eGroup.attributeValue("cron"));
                    cfg.setSecond(StringU.toInteger(eGroup.attributeValue("second")));
                    if (StringU.isBlank(cfg.getCron()) && (cfg.getSecond() == null || cfg.getSecond() < 1)) {
                        log.efmt("[!_!]Failed to parse[%s] 'cron' and 'second' attribute, you must provide one!",
                                cfg.getIdentifier());
                        continue;
                    }

                    String attrArgs = eGroup.attributeValue("args");
                    if (StringU.hasText(attrArgs)) {
                        List<Object> argList = new ArrayList<Object>();
                        for (String arg : attrArgs.split(",")) {
                            argList.add(StringU.trim(arg));
                        }
                        cfg.setArgs(argList.toArray(new Object[argList.size()]));
                    }

                    Method method = null;
                    for (Method m : cfg.getClazz().getMethods()) {
                        if (isTimerMethod(m, methodName, cfg.getArgs())) {
                            method = m;
                            break;
                        }
                    }

                    if (method == null) {
                        log.efmt("[!_!]Failed to intitialize timer[%s], no method %s!", cfg.getIdentifier(), methodName);
                        continue;
                    }

                    cfg.setMethod(method);
                    cfgList.add(cfg);
                }
            }
        } finally {
            inputStream.close();
        }
    }

    private boolean isTimerMethod(Method m, String methodName, Object[] args) {
        Class<?>[] paramTypes = m.getParameterTypes();
        if (!m.getName().equalsIgnoreCase(methodName) || paramTypes.length != args.length) {
            return false;
        }

        for (Class<?> t : paramTypes) {
            if (t != String.class) {
                return false;
            }
        }
        return true;
    }

    private Trigger createTrigger(TimerCfgVo cfg) throws Exception {
        ScheduleBuilder<?> builder = null;
        if (StringU.isBlank(cfg.getCron())) {
            builder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(cfg.getSecond()).withRepeatCount(-1);
        } else {
            builder = CronScheduleBuilder.cronSchedule(cfg.getCron());
        }
        return TriggerBuilder.newTrigger().withSchedule(builder).withIdentity(timerId()).build();
    }

    public void startup(String[] names, Class<?>[] classes, Object[] instances) {
        shutdown();

        for (TimerCfgVo cfg : listTimeCfgVo()) {
            for (int i = 0; i < classes.length; i++) {
                if (cfg.getClazz().isAssignableFrom(classes[i])) {
                    cfg.setTarget(instances[i]);
                    scheduleJob(cfg);
                    break;
                }
            }
        }
    }

    private static AtomicLong timerId = new AtomicLong(1);

    private static String timerId() {
        return timerId.getAndAdd(1) + "";
    }

    private void scheduleJob(TimerCfgVo cfg) {
        JobDataMap map = new JobDataMap();
        map.put("cfg", cfg);
        JobDetail detail = JobBuilder.newJob(JobWrapper.class).withIdentity(timerId()).usingJobData(map).build();
        try {
            scheduler.scheduleJob(detail, createTrigger(cfg));
        } catch (Exception e) {
            log.error("scheduleJob error: " + cfg, e);
        }
    }

    public void shutdown() {
        try {
            scheduler.shutdown();
            scheduler = getScheduler();// 丢弃老的Scheduler
        } catch (SchedulerException NULL) {
        }
    }

}
