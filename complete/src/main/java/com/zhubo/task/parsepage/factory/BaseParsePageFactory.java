package com.zhubo.task.parsepage.factory;

import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;

public abstract class BaseParsePageFactory {
    protected static int platformId;

    public abstract BaseParsePageTask create(String filePath, ResourceManager resourceManager);

    public abstract String getFilePrefix();

    public static int getPlatformId() {
        return platformId;
    }
}
