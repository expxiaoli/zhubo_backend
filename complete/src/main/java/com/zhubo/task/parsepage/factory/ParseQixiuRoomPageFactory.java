package com.zhubo.task.parsepage.factory;

import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;
import com.zhubo.task.parsepage.task.ParseRoomPageTask;

public class ParseQixiuRoomPageFactory extends BaseParsePageFactory {

    static {
        platformId = 1;
    }
    
    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParseRoomPageTask(filePath, resourceManager, platformId);
    }
    
    @Override
    public String getFilePrefix() {
        return "奇秀-主播房间";
    }
}
