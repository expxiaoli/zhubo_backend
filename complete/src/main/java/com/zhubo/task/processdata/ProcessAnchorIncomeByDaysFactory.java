package com.zhubo.task.processdata;

import com.zhubo.global.ResourceManager;

public class ProcessAnchorIncomeByDaysFactory extends BaseProcessDataFactory {

    @Override
    public BaseProcessDataTask create(ResourceManager resourceManager, int platformId) {
        return new ProcessAnchorIncomeByDaysTask(resourceManager, platformId);
    }

}
