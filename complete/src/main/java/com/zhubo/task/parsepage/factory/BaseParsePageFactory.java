package com.zhubo.task.parsepage.factory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;

public abstract class BaseParsePageFactory {

    protected Set<Long> invalidAliasIds;
    
    public abstract BaseParsePageTask create(String filePath, ResourceManager resourceManager);

    public abstract String getFilePrefix();
    
    public abstract String getTaskName();
    
    public abstract void loadInvalidIdFilePath(String path) throws FileNotFoundException, NumberFormatException, IOException;
}
