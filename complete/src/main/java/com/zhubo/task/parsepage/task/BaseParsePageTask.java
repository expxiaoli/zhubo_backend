package com.zhubo.task.parsepage.task;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.jdom.JDOMException;

import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.ResourceManager;

public abstract class BaseParsePageTask {
    protected final File file;
    protected final int platformId;
    protected final ResourceManager resourceManager;
    
    public BaseParsePageTask(String filePath, ResourceManager resourceManager, int platformId) {
        file = new File(filePath);
        this.resourceManager = resourceManager;
        this.platformId = platformId;
    }
   
    public abstract boolean run() throws JDOMException, IOException, PageFormatException, ParseException;
}
