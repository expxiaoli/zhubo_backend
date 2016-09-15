package com.zhubo.task.parsepage;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.ResourceManager;

public abstract class BaseParsePageTask {
    protected final File file;
    protected final ResourceManager resourceManager;
    
    public BaseParsePageTask(String filePath, ResourceManager resourceManager) {
        file = new File(filePath);
        this.resourceManager = resourceManager;
    }
   
    public abstract boolean run() throws JDOMException, IOException, PageFormatException;
}
