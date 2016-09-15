package com.zhubo.task.processdata;

import com.zhubo.global.ResourceManager;

public class ProcessQixiuMetricByDaysFactory extends BaseProcessDataFactory {

    @Override
    public BaseProcessDataTask create(ResourceManager resourceManager) {
        return new ProcessQixiuMetricByDaysTask(resourceManager);
    }

}
