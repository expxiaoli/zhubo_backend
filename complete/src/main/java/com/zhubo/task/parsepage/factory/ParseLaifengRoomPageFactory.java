package com.zhubo.task.parsepage.factory;

import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;
import com.zhubo.task.parsepage.task.ParseRoomPageTask;

public class ParseLaifengRoomPageFactory extends BaseParsePageFactory {
    
    static {
        platformId = 2;
    }
    
    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParseRoomPageTask(filePath, resourceManager, platformId);
    }

    @Override
    public String getFilePrefix() {
        return "来疯-主播房间";
    }

    @Override
    public String getTaskName() {
        return "ParseRoomPageTask";
    }
}
