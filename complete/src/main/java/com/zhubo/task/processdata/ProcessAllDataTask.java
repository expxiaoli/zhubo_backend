package com.zhubo.task.processdata;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.zhubo.global.ResourceManager;

public class ProcessAllDataTask {
    private static List<Class> processDataFactoryClasses = Lists
            .newArrayList(
                    ProcessAudiencePayByDaysFactory.class
                    //ProcessMetricByDaysFactory.class
                    );

    private Date start;
    private Date end;
    private Integer maxPlatformId = 2;

    public void setDates(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public void run() throws InstantiationException, IllegalAccessException {
        ResourceManager rm = ResourceManager.generateResourceManager();
        rm.initDatabaseCache(start, end);
        for (int platformId = 1; platformId <= maxPlatformId; platformId++) {
            System.out.println(String.format("begin to process platform %d data", platformId));
            rm.loadBatchProcessDataCache(platformId);
            for (Class factoryClass : processDataFactoryClasses) {
                BaseProcessDataFactory factory = (BaseProcessDataFactory) factoryClass
                        .newInstance();
                BaseProcessDataTask task = factory.create(rm, platformId);
                task.setStartDate(start);
                task.setEndDate(end);
                task.run();
            }
            rm.clearProcessDataCache();
        }
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        ProcessAllDataTask task = new ProcessAllDataTask();
        task.setDates(new Date(2016-1900, 8, 1), new Date(2016-1900, 8, 19));
        task.run();
        System.out.println("process all data done");
    }
}
