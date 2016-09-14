package com.zhubo.task;

import com.zhubo.global.ResourceManager;

public class ParseQixiuRoomPageFactory extends ParsePageTaskFactory {

    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParseQixiuRoomPageTask(filePath, resourceManager);
    }
    
    @Override
    public String getFilePrefix() {
        return "奇秀-主播房间";
    }
}
