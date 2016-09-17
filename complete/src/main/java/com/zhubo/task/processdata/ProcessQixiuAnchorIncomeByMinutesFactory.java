package com.zhubo.task.processdata;

import com.zhubo.global.ResourceManager;

public class ProcessQixiuAnchorIncomeByMinutesFactory extends BaseProcessDataFactory {

    @Override
    public BaseProcessDataTask create(ResourceManager resourceManager) {
        return new ProcessQixiuAnchorIncomeByMinutesTask(resourceManager);
    }

}
