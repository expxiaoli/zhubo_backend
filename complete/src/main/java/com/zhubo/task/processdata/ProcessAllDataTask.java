package com.zhubo.task.processdata;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import com.zhubo.entity.TaskRun;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.ModelHelper;

public class ProcessAllDataTask {
    private static List<Class> processDataFactoryClasses = Lists
            .newArrayList( 
                    ProcessAudiencePayByDaysFactory.class, //0918 done
                    ProcessMetricByDaysFactory.class,   //0918 done
                    ProcessAnchorIncomeByDaysFactory.class, //0919 done
                    ProcessAudienceTotalPayByDaysFactory.class
                    );

    private Date start;
    private Date end;
    private Integer maxPlatformId = 3;
    private boolean updateTaskRun = false;

    public void setDates(Date start, Date end) {
        this.start = start;
        this.end = end;
    }
    
    public void setUpdateTaskRun(boolean updateTaskRun) {
        this.updateTaskRun = updateTaskRun;
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
                TaskRun taskRun = null;
                if(updateTaskRun) {
                    taskRun = ModelHelper.markProcessDataTaskStart(rm, task.getClass().getSimpleName(), platformId, start, end);
                }
                task.run();
                if(updateTaskRun) {
                    ModelHelper.markTaskSuccess(rm, taskRun);
                }
            }
            rm.clearProcessDataCache();
        }
    }
/*
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        ProcessAllDataTask task = new ProcessAllDataTask();
        task.setDates(new Date(2016-1900, 8, 27), new Date(2016-1900, 8, 29));
        task.run();
        System.out.println("process all data done");
    }
*/    
}
