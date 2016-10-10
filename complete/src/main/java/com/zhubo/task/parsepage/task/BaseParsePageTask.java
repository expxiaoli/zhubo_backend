package com.zhubo.task.parsepage.task;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Set;

import org.jdom.JDOMException;

import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.ResourceManager;

public abstract class BaseParsePageTask {
    protected final File file;
    protected final int platformId;
    protected final ResourceManager resourceManager;
    protected final Set<Long> invalidAliasIds;
    
    public BaseParsePageTask(String filePath, Set<Long> invalidAliasIds, ResourceManager resourceManager, int platformId) {
        file = new File(filePath);
        this.invalidAliasIds = invalidAliasIds;
        this.resourceManager = resourceManager;
        this.platformId = platformId;
    }
   
    public abstract boolean run() throws JDOMException, IOException, PageFormatException, ParseException;
}
