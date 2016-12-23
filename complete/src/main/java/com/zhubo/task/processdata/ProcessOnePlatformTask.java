package com.zhubo.task.processdata;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.zhubo.global.ResourceManager;

public class ProcessOnePlatformTask {
    private static List<Class> processDataFactoryClasses = Lists.newArrayList(
            ProcessAudiencePayByDaysFactory.class, // 0918 done
            ProcessMetricByDaysFactory.class, // 0918 done
            ProcessAnchorIncomeByDaysFactory.class, // 0919 done
            ProcessAudienceTotalPayByDaysFactory.class);

    private Date start;
    private Date end;
    private int platformId;

    public ProcessOnePlatformTask(int platformId) {
        this.platformId = platformId;
    }

    public void setDates(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public void run() throws InstantiationException, IllegalAccessException {
        ResourceManager rm = ResourceManager.generateResourceManager();
        rm.initDatabaseCache(start, end);
        System.out.println(String.format("begin to process platform %d data", platformId));
        rm.loadBatchProcessDataCache(platformId);
        for (Class factoryClass : processDataFactoryClasses) {
            BaseProcessDataFactory factory = (BaseProcessDataFactory) factoryClass.newInstance();
            BaseProcessDataTask task = factory.create(rm, platformId);
            task.setStartDate(start);
            task.setEndDate(end);
            task.run();
        }
        rm.clearProcessDataCache();
    }
}
