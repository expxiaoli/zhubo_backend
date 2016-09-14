package com.zhubo.task;

import com.zhubo.global.ResourceManager;

public class ParseQixiuPlatformPageFactory extends ParsePageTaskFactory {

    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParseQixiuPlatformPageTask(filePath, resourceManager);
    }

    @Override
    public String getFilePrefix() {
        return "平台-奇秀广场";
    }
    
}
