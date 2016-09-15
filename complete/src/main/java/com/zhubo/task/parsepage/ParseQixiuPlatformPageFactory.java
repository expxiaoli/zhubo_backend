package com.zhubo.task.parsepage;

import com.zhubo.global.ResourceManager;

public class ParseQixiuPlatformPageFactory extends BaseParsePageFactory {

    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParseQixiuPlatformPageTask(filePath, resourceManager);
    }

    @Override
    public String getFilePrefix() {
        return "平台-奇秀广场";
    }
    
}
