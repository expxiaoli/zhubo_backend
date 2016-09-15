package com.zhubo.task.parsepage;

import com.zhubo.global.ResourceManager;


public abstract class BaseParsePageFactory {
    public abstract BaseParsePageTask create(String filePath, ResourceManager resourceManager);
    public abstract String getFilePrefix();
}
