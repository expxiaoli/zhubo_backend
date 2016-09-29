package com.zhubo.task.parsepage.factory;

import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;
import com.zhubo.task.parsepage.task.ParsePlatformPageTask;

public class ParseQixiuPlatformPageFactory extends BaseParsePageFactory {
    
    static {
        platformId = 1;
    }
    
    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParsePlatformPageTask(filePath, resourceManager, platformId);
    }

    @Override
    public String getFilePrefix() {
        return "平台-奇秀广场";
    }

    @Override
    public String getTaskName() {
        return "ParsePlatformPageTask";
    }
    
}
