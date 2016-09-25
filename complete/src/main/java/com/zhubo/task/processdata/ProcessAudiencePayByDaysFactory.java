package com.zhubo.task.processdata;

import com.zhubo.global.ResourceManager;

public class ProcessAudiencePayByDaysFactory extends BaseProcessDataFactory {

    @Override
    public BaseProcessDataTask create(ResourceManager resourceManager, int platformId) {
        return new ProcessAudiencePayByDaysTask(resourceManager, platformId);
    }

}
