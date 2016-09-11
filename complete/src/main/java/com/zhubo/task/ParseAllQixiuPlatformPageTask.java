package com.zhubo.task;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.jdom.JDOMException;

import com.google.common.collect.Lists;
import com.zhubo.global.ResourceManager;

public class ParseAllQixiuPlatformPageTask {
    public static void main(String[] args) throws JDOMException, IOException, ParseException {        
        String folderPath = args[0];
        File folder = new File(folderPath);
        ResourceManager rm = ResourceManager.generateResourceManager();
        int platformPageCount = 0;
        int parsedPageCount = 0;
        int totalPageCount = folder.list().length;
        List<String> errorFilePaths = Lists.newArrayList();
        for(File file : folder.listFiles()) {
            System.out.println("begin to parse " + file.getPath());
            ParseQixiuPlatformPageTask task = new ParseQixiuPlatformPageTask(file.getPath(), rm);
            boolean result = false;
            try {
                result = task.run();
            } catch (JDOMException e) {
                errorFilePaths.add(file.getPath());
            }
            if(result) {
                platformPageCount++;
            }
            parsedPageCount++;
            if(parsedPageCount % 20 == 0) {
                System.out.println(String.format("parsed platform page count %d, page count %d. need to parse page count %d", 
                        platformPageCount, parsedPageCount, totalPageCount));
            }
        }
        System.out.println("ParseAllQixiuPlatformPageTask done");
        System.out.println(String.format("parsed platform page count %d, page count %d. need to parse page count %d", 
                platformPageCount, parsedPageCount, totalPageCount));
        System.out.println("error page:");
        for(String errorFilePath : errorFilePaths) {
            System.out.println(errorFilePath);
        }
    }
}
