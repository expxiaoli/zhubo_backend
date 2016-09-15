package com.zhubo.task.parsepage;

import com.zhubo.global.ResourceManager;

public class ParseQixiuRoomPageFactory extends BaseParsePageFactory {

    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParseQixiuRoomPageTask(filePath, resourceManager);
    }
    
    @Override
    public String getFilePrefix() {
        return "奇秀-主播房间";
    }
}
