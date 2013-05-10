package com.zte.mcore.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 * 
 * @author PanJun
 * 
 */
public final class DateU {

    private static final String YYYY_MM_DD = "yyyy-MM-dd";
    private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    private static final ThreadLocal<SimpleDateFormat> dateFmtInstance = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        };
    };

    private static ThreadLocal<Calendar> calInstance = new ThreadLocal<Calendar>() {
        protected Calendar initialValue() {
            return Calendar.getInstance();
        }
    };

    /**
     * 把日期加天数
     * 
     * @param date
     * @param day
     * @return
     */
    public static Date addDay(Date date, int day) {
        if (date == null)
            return null;

        Calendar calendar = calInstance.get();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }

    /**
     * 把日期加天数，返回结果
     * 
     * @param date
     * @param day
     * @return
     */
    public static Long addDay(Long date, int day) {
        if (date == null)
            return null;

        Date realDate = addDay(toRealDate(date), day);
        return toLongDate(realDate);
    }

    /**
     * 把日期加上指定分钟数，返回结果
     * 
     * @param date
     * @param minutes
     * @return
     */
    public static Date addMin(Date date, int minutes) {
        if (date == null)
            return null;

        Calendar calendar = calInstance.get();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    /**
     * 把日期加上指定分钟数，返回结果
     * 
     * @param date
     * @param hours
     * @return
     */
    public static Date addHour(Date date, int hours) {
        if (date == null)
            return null;

        Calendar calendar = calInstance.get();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, hours);
        return calendar.getTime();
    }

    /**
     * 请参看Calender.get(field)函数
     * 
     * @param date
     * @param field
     * @return
     */
    public static int get(Date date, int field) {
        if (date == null) {
            return -1;
        }

        Calendar cal = calInstance.get();
        cal.setTime(date);
        return cal.get(field);
    }

    /**
     * 请参看Calender.get(field)函数
     * 
     * @param date
     * @param field
     * @return
     */
    public static int get(Long date, int field) {
        return get(toRealDate(date), field);
    }

    /**
     * 把长整形日期转换成真实日期
     * 
     * @param date
     * @return
     */
    public static Date toRealDate(Long date) {
        if (date == null)
            return null;

        Calendar calendar = calInstance.get();
        int ss = (int) (date - (date / 100) * 100);
        calendar.set(Calendar.SECOND, ss);

        int mi = (int) ((date - (date / 10000) * 10000) / 100);
        calendar.set(Calendar.MINUTE, mi);

        int hh = (int) ((date - (date / 1000000) * 1000000) / 10000);
        calendar.set(Calendar.HOUR_OF_DAY, hh);

        int dd = (int) ((date - (date / 100000000) * 100000000) / 1000000);
        calendar.set(Calendar.DAY_OF_MONTH, dd);

        int yy = (int) (date / 10000000000l);
        calendar.set(Calendar.YEAR, yy);

        int noYear = (int) (date - yy * 10000000000l);
        int mm = noYear / 100000000;
        calendar.set(Calendar.MONTH, mm - 1);
        return calendar.getTime();
    }

    /**
     * 把日期转换成yyyyMMddHH格式
     * 
     * @param date
     * @return
     */
    public static Integer toYmdh(Date date) {
        if (date != null) {
            return Integer.parseInt(new SimpleDateFormat("yyyyMMddHH").format(date));
        } else {
            return null;
        }
    }

    /**
     * 把日期转换成yyyyMMdd格式
     * 
     * @param date
     * @return
     */
    public static Integer toYmd(Date date) {
        if (date != null) {
            return Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(date));
        } else {
            return null;
        }
    }

    /**
     * Date类型转换成Long型日期
     * 
     * @param date
     * @return
     */
    public static Long toLongDate(Date date) {
        if (date == null)
            return null;

        return Long.valueOf(dateFmtInstance.get().format(date));
    }

    /**
     * 把日期加月份
     * 
     * @param date
     * @param month
     * @return
     */
    public static Date addMonth(Date date, int month) {
        if (date == null)
            return null;

        Calendar calendar = calInstance.get();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, month);
        return calendar.getTime();
    }

    /**
     * 把日期加月份
     * 
     * @param date
     * @param month
     * @return
     */
    public static Long addMonth(Long date, int month) {
        if (date == null)
            return null;

        Date realDate = addMonth(toRealDate(date), month);
        return toLongDate(realDate);
    }

    /**
     * 把日期的时分秒去除只留年月日
     * 
     * @param date
     * @return 只留年月日的日期
     */
    public static Date clearTime(Date date) {
        if (date == null)
            return null;

        Calendar calendar = calInstance.get();
        calendar.setTime(date);
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.clear();
        calendar.set(Calendar.YEAR, y);
        calendar.set(Calendar.MONTH, m);
        calendar.set(Calendar.DAY_OF_MONTH, d);
        return calendar.getTime();
    }

    /**
     * 设置日期格式的时间部分包含：时、分、秒；如果某部分为负数，不修改此部分
     * 
     * @param date
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static Date setTime(Date date, int hour, int minute, int second) {
        Calendar cal = calInstance.get();
        cal.setTime(date);
        if (hour >= 0) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute >= 0) {
            cal.set(Calendar.MINUTE, minute);
        }
        if (second >= 0) {
            cal.set(Calendar.SECOND, second);
        }
        return cal.getTime();
    }

    /**
     * 把日期的时分秒去除只留年月日 20101230115511 == >20101230115511
     * 
     * @param date
     * @return 只留年月日的日期
     */
    public static Long clearTime(Long date) {
        if (date == null)
            return null;

        return (date / 1000000) * 1000000;
    }

    /**
     * 日期转化为字串
     * 
     * @param date
     * @param pattern
     * @return
     */
    public static String dateToStr(Date date, String pattern) {
        if (date == null || pattern == null)
            return null;

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 日期时间转化为字串yyyy-MM-dd HH:mm:SS
     * 
     * @param date
     * @param pattern
     * @return
     */
    public static String dateTimeToStr(Date date) {
        return dateToStr(date, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 把日期转化成"yyyy-MM-dd"格式的字串
     * 
     * @param date
     * @return
     */
    public static String dateToStr(Date date) {
        return dateToStr(date, YYYY_MM_DD);
    }

    /**
     * 字串转化成日期
     * 
     * @param date
     * @param pattern
     * @return
     */
    public static Date strToDate(String date, String pattern) {
        if (date == null || pattern == null)
            return null;

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回两个日期之间的天数差
     * 
     * @param d1
     * @param d2
     * @return
     */
    public static int dateDiff(Date d1, Date d2) {
        if (d1 == null || d2 == null)
            throw new NullPointerException("dateDiff方法两个参数不能为null!");

        Long diff = (d1.getTime() - d2.getTime()) / 1000 / 60 / 60 / 24;
        return diff.intValue();
    }

    /**
     * 返回两个日期之间的天数差
     * 
     * @param d1
     * @param d2
     * @return
     */
    public static int dateDiff(Long d1, Long d2) {
        return dateDiff(toRealDate(d1), toRealDate(d2));
    }

    /**
     * 把"yyyy-MM-dd"格式的字串转化成日期
     * 
     * @param date
     * @return
     */
    public static Date strToDate(String date) {
        return strToDate(date, YYYY_MM_DD);
    }

}