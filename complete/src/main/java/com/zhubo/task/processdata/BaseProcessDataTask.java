package com.zhubo.task.processdata;

import java.util.Date;

import com.zhubo.global.ResourceManager;

public abstract class BaseProcessDataTask {
    protected final ResourceManager resourceManager;
    protected final int platformId;
    protected Date start;
    protected Date end;

    public BaseProcessDataTask(ResourceManager resourceManager, int platformId) {
        this.resourceManager = resourceManager;
        this.platformId = platformId;
        this.start = null;
        this.end = null;
    }

    public abstract boolean run();
    
    public void setStartDate(Date start) {
        this.start = start;
    }
    
    public void setEndDate(Date end) {
        this.end = end;
    }
}
