package com.zhubo.task.parsepage.factory;

import java.io.IOException;

import com.google.common.collect.Sets;
import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;
import com.zhubo.task.parsepage.task.ParseAppRoomPageWithTopAudienceIdentifyTask;
import com.zhubo.task.parsepage.task.ParseRoomPageWithTopAudienceIdentifyTask;
import com.zhubo.task.parsepage.task.ParseRoomPageWithWeekIdentifyTask;

public class ParseHaniRoomPageFactory extends BaseParsePageFactory {
    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParseAppRoomPageWithTopAudienceIdentifyTask(filePath, invalidAliasIds, resourceManager, 13);
    }
    
    @Override
    public String getFilePrefix() {
        return "hani";
    }

    @Override
    public String getTaskName() {
        return "ParseAppRoomPageWithTopAudienceIdentifyTask";
    }

    @Override
    public void loadInvalidIdFilePath(String path) throws NumberFormatException, IOException {
        invalidAliasIds = Sets.newHashSet();
    }
}
