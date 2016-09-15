package com.zhubo.task.processdata;

import com.zhubo.global.ResourceManager;

public abstract class BaseProcessDataFactory {
    public abstract BaseProcessDataTask create(ResourceManager resourceManager);
}
