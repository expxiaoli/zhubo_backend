package com.zhubo.task.processdata;

import com.zhubo.global.ResourceManager;

public class ProcessAudienceTotalPayByDaysFactory extends BaseProcessDataFactory {

    @Override
    public BaseProcessDataTask create(ResourceManager resourceManager, int platformId) {
        return new ProcessAudienceTotalPayByDaysTask(resourceManager, platformId);
    }

}
