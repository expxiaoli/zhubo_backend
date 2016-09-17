package com.zhubo.task.parsepage;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jdom.JDOMException;

import com.google.common.collect.Lists;
import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.ResourceManager;

public class ParseAllPageTask {

    private List<Class> parsePageTaskFactoryClasses = Lists.newArrayList(
            ParseQixiuPlatformPageFactory.class, 
            ParseQixiuRoomPageFactory.class
            );

    @SuppressWarnings("unchecked")
    public void run(String folderPath) throws InstantiationException, IllegalAccessException, IOException {
        File folder = new File(folderPath);
        ResourceManager rm = ResourceManager.generateResourceManager();
        int parseSuccessCount = 0;
        int toParseCount = 0;
        int totalPageCount = folder.list().length;
        List<String> errorFilePaths = Lists.newArrayList();
        BaseParsePageFactory factory;
        BaseParsePageTask task;
        List<File> files = getValidFiles(folder.listFiles());
        Collections.sort(files, new byPageTimeComparator());
        for (Class factoryClass : parsePageTaskFactoryClasses) {
            factory = (BaseParsePageFactory) factoryClass.newInstance();
            for (File file : files) {
                System.out.println("begin to parse " + file.getPath());
                boolean result = false;
                String fileName = file.getName();
                if (fileName.startsWith(factory.getFilePrefix())) {
                    toParseCount++;
                    try {
                        task = factory.create(file.getPath(), rm);
                        result = task.run();
                        if (result) {
                            parseSuccessCount++;
                        }
                    } catch (JDOMException e) {
                        errorFilePaths.add(file.getPath());
                    } catch (PageFormatException e) {
                        errorFilePaths.add(file.getPath());
                    }
                } else {
                    System.out.println("ignore, can not be parsed with "
                            + factory.getClass().getName());
                }
            }

            if (toParseCount % 20 == 0) {
                System.out.println(String.format(
                        "parse page success %d, in parse range %d. total page count %d",
                        parseSuccessCount, toParseCount, totalPageCount));
            }
        }
        System.out.println("ParseAllPageTask done");
        System.out.println(String.format(
                "parse page success %d, in parse range %d. total page count %d", parseSuccessCount,
                toParseCount, totalPageCount));
        System.out.println("error page:");
        for (String errorFilePath : errorFilePaths) {
            System.out.println(errorFilePath);
        }
    }
    
    private List<File> getValidFiles(File[] files) {
        List<File> validFiles = Lists.newArrayList();
        for(File file : files) {
            if(file.getName().split("-").length == 4) {
                validFiles.add(file);
            }
        }
        return validFiles;
    }
   
    
    public class byPageTimeComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            File f1 = (File)o1;
            File f2 = (File)o2;
            String[] parts1 = f1.getName().split("-");
            long pageTs1 = Long.valueOf(parts1[2]);
            long importTs1 = Long.valueOf(parts1[3]);
            String[] parts2 = f2.getName().split("-");
            long pageTs2 = Long.valueOf(parts2[2]);
            long importTs2 = Long.valueOf(parts2[3]);            
            if (pageTs1 < pageTs2 || (pageTs1 == pageTs2 && importTs1 < importTs2)) {
                return -1;
            } else if(pageTs1 == pageTs2 && importTs1 == importTs2) {
                return 0;
            } else {
                return 1;
            }
        }
        
    }
/*
    public static void main(String[] args) throws JDOMException, IOException, ParseException,
            InstantiationException, IllegalAccessException {
        new ParseAllPageTask().run(args[0]);
    }
*/
}