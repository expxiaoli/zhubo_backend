package com.zhubo.helper;

import java.util.Calendar;
import java.util.Date;

import com.zhubo.task.processdata.TimeUnit;

public class GeneralHelper {

    public static Date getAggregateDate(Date date, TimeUnit unit) {
        switch (unit) {
        case DAY:
            return new Date(date.getYear(), date.getMonth(), date.getDate());
        case WEEK:
            int dayOfWeek = date.getDay();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, date.getYear());
            cal.set(Calendar.MONTH, date.getMonth());
            cal.set(Calendar.DAY_OF_MONTH, date.getDate());
            cal.add(Calendar.DATE, -dayOfWeek);
            return cal.getTime();
        case MONTH:
            return new Date(date.getYear(), date.getMonth(), 1);
        default:
            return null;
        }
    }
}
