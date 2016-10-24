package com.zhubo.task.parsepage.factory;

import com.google.common.collect.Sets;
import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;
import com.zhubo.task.parsepage.task.ParsePlatformPageWithDirectInfoTask;

public class ParseWoxiuPlatformPageFactory extends BaseParsePageFactory {
    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParsePlatformPageWithDirectInfoTask(filePath, invalidAliasIds, resourceManager, 3);
    }

    @Override
    public String getFilePrefix() {
        return "我秀平台-我秀广场";
    }

    @Override
    public String getTaskName() {
        return "ParsePlatformPageWithDirectInfoTask";
    }

    @Override
    public void loadInvalidIdFilePath(String path) {
        invalidAliasIds = Sets.newHashSet();
    }
}
