package com.zhubo.helper;

import java.util.Date;

import com.zhubo.task.processdata.TimeUnit;

public class GeneralHelper {

    public static Date getAggregateDate(Date date, TimeUnit unit) {
        switch (unit) {
        case DAY:
            return new Date(date.getYear(), date.getMonth(), date.getDate());
        case MONTH:
            return new Date(date.getYear(), date.getMonth(), 1);
        default:
            return null;
        }

    }
}
