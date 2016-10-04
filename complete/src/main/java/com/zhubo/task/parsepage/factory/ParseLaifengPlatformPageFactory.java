package com.zhubo.task.parsepage.factory;

import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;
import com.zhubo.task.parsepage.task.ParsePlatformPageTask;

public class ParseLaifengPlatformPageFactory extends BaseParsePageFactory {
    
    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParsePlatformPageTask(filePath, resourceManager, 2);
    }

    @Override
    public String getFilePrefix() {
        return "平台-来疯广场";
    }

    @Override
    public String getTaskName() {
        return "ParsePlatformPageTask";
    }
    
   
}
