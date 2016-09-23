package com.zhubo.task.parsepage;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;
import com.zhubo.task.parsepage.factory.BaseParsePageFactory;
import com.zhubo.task.parsepage.factory.ParseLaifengPlatformPageFactory;
import com.zhubo.task.parsepage.factory.ParseLaifengRoomPageFactory;
import com.zhubo.task.parsepage.factory.ParseQixiuPlatformPageFactory;
import com.zhubo.task.parsepage.factory.ParseQixiuRoomPageFactory;
import com.zhubo.task.parsepage.task.BaseParsePageTask;

public class ParseAllPageTask {

    private Map<Integer, Class> parsePlatformPageFactoryClasses;
    private Map<Integer, Class> parseRoomPageFactoryClasses;
    private int maxPlatformId = 2;
    

    public ParseAllPageTask() {
        parsePlatformPageFactoryClasses = Maps.newHashMap();
        parsePlatformPageFactoryClasses.put(1, ParseQixiuPlatformPageFactory.class);
        parsePlatformPageFactoryClasses.put(2, ParseLaifengPlatformPageFactory.class);
        
        parseRoomPageFactoryClasses = Maps.newHashMap();
        parseRoomPageFactoryClasses.put(1, ParseQixiuRoomPageFactory.class);
        parseRoomPageFactoryClasses.put(2, ParseLaifengRoomPageFactory.class);
    }
    
    private int parseSuccessCount = 0;
    private int toParseCount = 0;
    private List<String> errorFilePaths = Lists.newArrayList();
    
    

    @SuppressWarnings("unchecked")
    public void run(String folderPath) throws InstantiationException, IllegalAccessException, IOException, ParseException {
        File folder = new File(folderPath);
        ResourceManager rm = ResourceManager.generateResourceManager();
        List<File> files = getValidFiles(folder.listFiles());
        Collections.sort(files, new byPageTimeComparator());
        
        String minMiddleName = getMinMiddleName(files);
        String maxMiddleName = getMaxMiddleName(files);
        System.out.println("min middle name for files: " + minMiddleName);
        System.out.println("max middle name for files: " + maxMiddleName);
        
        
        rm.initDatabaseCacheAndBatchLoad(
                GeneralHelper.parseDateFromFileMiddleName(minMiddleName), 
                GeneralHelper.parseDateFromFileMiddleName(maxMiddleName));
        
        
        for(int platformId = 1; platformId <= maxPlatformId; platformId++) {
            rm.loadBatchInCache(platformId);
            parseFiles(files, parsePlatformPageFactoryClasses.get(platformId), rm);
            parseFiles(files, parseRoomPageFactoryClasses.get(platformId), rm);
            rm.clearCache();
        }
        
        System.out.println("begin to store cache to database");
        rm.getDatabaseCache().batchSave();
        System.out.println("ParseAllPageTask done");
        System.out.println(String.format(
                "parse page success %d, in parse range %d. total page count %d", parseSuccessCount,
                toParseCount, files.size()));
        System.out.println("error page:");
        for (String errorFilePath : errorFilePaths) {
            System.out.println(errorFilePath);
        }
    }
    
    private void parseFiles(List<File> files, Class factoryClass, ResourceManager rm) throws IOException, InstantiationException, IllegalAccessException {
        BaseParsePageFactory factory;
        BaseParsePageTask task;
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
            }
            if (toParseCount % 20 == 0) {
                System.out.println(String.format(
                        "parse page success %d, in parse range %d. total page count %d",
                        parseSuccessCount, toParseCount, files.size()));
            }
        }
    }
    
    private List<File> getValidFiles(File[] files) {
        List<File> validFiles = Lists.newArrayList();
        for(File file : files) {
            String[] parts = file.getName().split("-");
            if(parts.length == 4 && parts[2].length() == 14) {
                validFiles.add(file);
            }
        }
        return validFiles;
    }
    
    private String getMinMiddleName(List<File> files) {
        return files.get(0).getName().split("-")[2];
    }
    
    private String getMaxMiddleName(List<File> files) {
        return files.get(files.size() - 1).getName().split("-")[2];
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

    public static void main(String[] args) throws JDOMException, IOException, ParseException,
            InstantiationException, IllegalAccessException {
        long start = System.currentTimeMillis();
        new ParseAllPageTask().run(args[0]);
        long end = System.currentTimeMillis();
        long durationSecs = (end - start) / 1000;
        System.out.println("use seconds:" + durationSecs);
        
    }

}
