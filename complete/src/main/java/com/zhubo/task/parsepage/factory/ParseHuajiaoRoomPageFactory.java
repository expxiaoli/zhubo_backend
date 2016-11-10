package com.zhubo.task.parsepage.factory;

import java.io.IOException;

import com.google.common.collect.Sets;
import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;
import com.zhubo.task.parsepage.task.ParseRoomPageWithHistoryIdentifyTask;
import com.zhubo.task.parsepage.task.ParseRoomPageWithWeekIdentifyTask;

public class ParseHuajiaoRoomPageFactory extends BaseParsePageFactory {
    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParseRoomPageWithHistoryIdentifyTask(filePath, invalidAliasIds, resourceManager, 5);
    }
    
    @Override
    public String getFilePrefix() {
        return "花椒-主播房间";
    }

    @Override
    public String getTaskName() {
        return "ParseRoomPageWithHistoryIdentifyTask";
    }

    @Override
    public void loadInvalidIdFilePath(String path) throws NumberFormatException, IOException {
        invalidAliasIds = Sets.newHashSet();
    }
}
