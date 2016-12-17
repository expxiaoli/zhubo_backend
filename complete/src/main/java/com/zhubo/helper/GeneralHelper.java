package com.zhubo.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.zhubo.task.processdata.TimeUnit;

public class GeneralHelper {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); 
    private static final SimpleDateFormat sdfFormat1 = new SimpleDateFormat("yyyy-M-d HH:mm:ss");
    private static final SimpleDateFormat sdfFormat2 = new SimpleDateFormat("yyyy/M/d HH:mm:ss");//2016/9/27 12:50:40

    public static Date getAggregateDate(Date date, TimeUnit unit) {
        switch (unit) {
        case DAY:
            return new Date(date.getYear(), date.getMonth(), date.getDate());
        case WEEK:
            int dayOfWeek = (date.getDay() + 6) % 7;
            Calendar cal = Calendar.getInstance();
            cal.set(date.getYear() + 1900, date.getMonth(), date.getDate(), 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.DATE, -dayOfWeek);
            return cal.getTime();
        case MONTH:
            return new Date(date.getYear(), date.getMonth(), 1);
        default:
            return null;
        }
    }
    
    public static Integer getDiffDay(Date date1, Date date2) {
        return ((Long)((date1.getTime() - date2.getTime()) / (3600 * 1000 * 24))).intValue();
    }
    
    public static Date parseDateFromFileMiddleName(String fileMiddleName) throws ParseException {
        return sdf.parse(fileMiddleName);
    }
    
    public static Date parseWithMultipleFormats(String str) throws ParseException {
        if(str.contains("-")) {
            return sdfFormat1.parse(str);
        } else {
            return sdfFormat2.parse(str);
        }
    }
    
    public static Date addDay(Date date, int day) {
        Calendar now = Calendar.getInstance();  
        now.setTime(date);  
        now.add(Calendar.DAY_OF_YEAR, day);  
        return now.getTime(); 
    }
    
    public static Integer getIntegerFromComplextString(String s) {
        if(s.endsWith("万")) {
            String pureStr = s.substring(0, s.length() - 1);
            return (int) (Double.valueOf(pureStr) * 10000);
        } else {
            return Integer.valueOf(s);
        }
    }
}
