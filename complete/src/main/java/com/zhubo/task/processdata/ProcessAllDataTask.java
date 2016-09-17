package com.zhubo.task.processdata;

import java.util.List;

import com.google.common.collect.Lists;
import com.zhubo.global.ResourceManager;

public class ProcessAllDataTask {
    private static List<Class> processDataFactoryClasses = Lists.newArrayList(
            ProcessQixiuAnchorIncomeByMinutesFactory.class
//            ProcessQixiuMetricByDaysFactory.class
            );
    
    public void run() throws InstantiationException, IllegalAccessException {
        ResourceManager rm = ResourceManager.generateResourceManager();
        for(Class factoryClass : processDataFactoryClasses) {
            BaseProcessDataFactory factory = (BaseProcessDataFactory) factoryClass.newInstance(); 
            BaseProcessDataTask task = factory.create(rm);
            task.run();
        }
    }
/*    
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        new ProcessAllDataTask().run();
        System.out.println("process all data done");
    }
    */
}
