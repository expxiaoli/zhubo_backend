package com.zhubo.task.parsepage.factory;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;
import com.zhubo.task.parsepage.task.ParseRoomPageWithRoundIdentifyTask;

public class ParseLaifengRoomPageFactory extends BaseParsePageFactory {
    
    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParseRoomPageWithRoundIdentifyTask(filePath, invalidAliasIds, resourceManager, 2);
    }

    @Override
    public String getFilePrefix() {
        return "来疯-主播房间";
    }

    @Override
    public String getTaskName() {
        return "ParseRoomPageTask";
    }

    @Override
    public void loadInvalidIdFilePath(String path) {
       invalidAliasIds = Sets.newHashSet();   
    }
}
