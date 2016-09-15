package com.zhubo.task.processdata;

import com.zhubo.global.ResourceManager;

public abstract class BaseProcessDataTask {
    protected final ResourceManager resourceManager;

    public BaseProcessDataTask(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public abstract boolean run();
}
