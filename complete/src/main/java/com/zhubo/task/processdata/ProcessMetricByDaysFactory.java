package com.zhubo.task.processdata;

import com.zhubo.global.ResourceManager;

public class ProcessMetricByDaysFactory extends BaseProcessDataFactory {

    @Override
    public BaseProcessDataTask create(ResourceManager resourceManager, int platformId) {
        return new ProcessMetricByDaysTask(resourceManager, platformId);
    }

}
