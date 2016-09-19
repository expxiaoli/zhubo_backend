package com.zhubo.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.zhubo.task.processdata.TimeUnit;

public class GeneralHelper {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); 

    public static Date getAggregateDate(Date date, TimeUnit unit) {
        switch (unit) {
        case DAY:
            return new Date(date.getYear(), date.getMonth(), date.getDate());
        case WEEK:
            int dayOfWeek = date.getDay();
            Calendar cal = Calendar.getInstance();
            cal.set(date.getYear() + 1900, date.getMonth(), date.getDate(), 0, 0, 0);
            cal.add(Calendar.DATE, -dayOfWeek);
            return cal.getTime();
        case MONTH:
            return new Date(date.getYear(), date.getMonth(), 1);
        default:
            return null;
        }
    }
    
    
    public static Date parseDateFromFileMiddleName(String fileMiddleName) throws ParseException {
        return sdf.parse(fileMiddleName);
    }
}
