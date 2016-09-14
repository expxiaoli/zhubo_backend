package com.zhubo.task;

import com.zhubo.global.ResourceManager;


public abstract class ParsePageTaskFactory {
    public abstract BaseParsePageTask create(String filePath, ResourceManager resourceManager);
    public abstract String getFilePrefix();
}
