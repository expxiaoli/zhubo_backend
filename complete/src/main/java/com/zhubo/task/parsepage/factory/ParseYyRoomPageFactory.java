package com.zhubo.task.parsepage.factory;

import java.io.IOException;

import com.google.common.collect.Sets;
import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;
import com.zhubo.task.parsepage.task.ParseRoomPageWithTopAudienceIdentifyTask;
import com.zhubo.task.parsepage.task.ParseRoomPageWithWeekIdentifyTask;

public class ParseYyRoomPageFactory extends BaseParsePageFactory {
    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParseRoomPageWithTopAudienceIdentifyTask(filePath, invalidAliasIds, resourceManager, 7);
    }
    
    @Override
    public String getFilePrefix() {
        return "YY-主播房间";
    }

    @Override
    public String getTaskName() {
        return "ParseRoomPageWithTopAudienceIdentifyTask";
    }

    @Override
    public void loadInvalidIdFilePath(String path) throws NumberFormatException, IOException {
        invalidAliasIds = Sets.newHashSet();
    }
}
