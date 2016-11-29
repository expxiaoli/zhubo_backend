package com.zhubo.task.parsepage.factory;

import com.google.common.collect.Sets;
import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;
import com.zhubo.task.parsepage.task.ParsePlatformPageWithDirectInfoTask;
import com.zhubo.task.parsepage.task.ParsePlatformPageWithItemNameTask;

public class ParseQianfangPlatformPageFactory extends BaseParsePageFactory {
    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParsePlatformPageWithItemNameTask(filePath, invalidAliasIds, resourceManager, 4);
    }

    @Override
    public String getFilePrefix() {
        return "平台-千帆广场";
    }

    @Override
    public String getTaskName() {
        return "ParsePlatformPageWithItemNameTask";
    }

    @Override
    public void loadInvalidIdFilePath(String path) {
        invalidAliasIds = Sets.newHashSet();
    }
}
