package com.zhubo.task.parsepage;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;
import com.zhubo.task.parsepage.factory.BaseParsePageFactory;
import com.zhubo.task.parsepage.factory.ParseHaniRoomPageFactory;
import com.zhubo.task.parsepage.factory.ParseHuajiaoRoomPageFactory;
import com.zhubo.task.parsepage.factory.ParseHuoRoomPageFactory;
import com.zhubo.task.parsepage.factory.ParseLaifengPlatformPageFactory;
import com.zhubo.task.parsepage.factory.ParseLaifengRoomPageFactory;
import com.zhubo.task.parsepage.factory.ParseQianfangPlatformPageFactory;
import com.zhubo.task.parsepage.factory.ParseQianfangRoomPageFactory;
import com.zhubo.task.parsepage.factory.ParseQixiuPlatformPageFactory;
import com.zhubo.task.parsepage.factory.ParseQixiuRoomPageFactory;
import com.zhubo.task.parsepage.factory.ParseWoxiuPlatformPageFactory;
import com.zhubo.task.parsepage.factory.ParseWoxiuRoomPageFactory;
import com.zhubo.task.parsepage.factory.ParseYingkeRoomPageFactory;
import com.zhubo.task.parsepage.factory.ParseYizhiboRoomPageFactory;
import com.zhubo.task.parsepage.task.BaseParsePageTask;

public class ParseOnePlatformTask {
    private static Map<Integer, Class> parsePlatformPageFactoryClasses;
    private static Map<Integer, Class> parseRoomPageFactoryClasses;
 //   private boolean updateTaskRun = false;
    private int platformId;
    
    private int parseSuccessCount = 0;
    private int toParseCount = 0;
    private List<ErrorFileInfo> errorFileInfos = Lists.newArrayList();
    
    public static class ErrorFileInfo {
        public String filePath;
        public String errorMessage;
        public ErrorFileInfo(String filePath, String errorMessage) {
            this.filePath = filePath;
            this.errorMessage = errorMessage;
        }
    }
    static  {
        parsePlatformPageFactoryClasses = Maps.newHashMap();
        parsePlatformPageFactoryClasses.put(1, ParseQixiuPlatformPageFactory.class);
        parsePlatformPageFactoryClasses.put(2, ParseLaifengPlatformPageFactory.class);
        parsePlatformPageFactoryClasses.put(3, ParseWoxiuPlatformPageFactory.class);
        parsePlatformPageFactoryClasses.put(4, ParseQianfangPlatformPageFactory.class);

        parseRoomPageFactoryClasses = Maps.newHashMap();
        parseRoomPageFactoryClasses.put(1, ParseQixiuRoomPageFactory.class);
        parseRoomPageFactoryClasses.put(2, ParseLaifengRoomPageFactory.class);
        parseRoomPageFactoryClasses.put(3, ParseWoxiuRoomPageFactory.class);
        parseRoomPageFactoryClasses.put(4, ParseQianfangRoomPageFactory.class);
        parseRoomPageFactoryClasses.put(5, ParseHuajiaoRoomPageFactory.class);
        parseRoomPageFactoryClasses.put(6, ParseYizhiboRoomPageFactory.class);
        parseRoomPageFactoryClasses.put(12, ParseYingkeRoomPageFactory.class);
        parseRoomPageFactoryClasses.put(13, ParseHaniRoomPageFactory.class);
        parseRoomPageFactoryClasses.put(14, ParseHuoRoomPageFactory.class);
    }
    
    public ParseOnePlatformTask(int platformId) {
        this.platformId = platformId;
    }
    
    @SuppressWarnings("unchecked")
    public void run(String folderPath, String invalidIdFilePath) throws InstantiationException, IllegalAccessException,
            IOException, ParseException {

        ResourceManager rm = ResourceManager.generateResourceManager();

        File folder = new File(folderPath);
        String folderName = folder.getName();
        List<File> files = getValidFiles(folder.listFiles(), folderName);
        if(files.size() == 0) {
            System.out.println("empty page folder");
            return;
        }
        Collections.sort(files, new byPageTimeComparator());

        String minMiddleName = getMinMiddleName(files);
        String maxMiddleName = getMaxMiddleName(files);
        System.out.println("min middle name for files: " + minMiddleName);
        System.out.println("max middle name for files: " + maxMiddleName);

        rm.initDatabaseCache(GeneralHelper.parseDateFromFileMiddleName(minMiddleName),
                GeneralHelper.parseDateFromFileMiddleName(maxMiddleName));
        rm.loadBatchParsePageCache(platformId);
        parseFiles(folderPath, invalidIdFilePath, files, platformId, parsePlatformPageFactoryClasses.get(platformId), rm);
        parseFiles(folderPath, invalidIdFilePath, files, platformId, parseRoomPageFactoryClasses.get(platformId), rm);
        rm.clearParsePageCache();

        System.out.println("ParseOnePlatformTask done, platform id: " + platformId);
        System.out.println(String.format(
                "parse page success %d, in parse range %d. total page count %d", parseSuccessCount,
                toParseCount, files.size()));
        System.out.println("error page:");
        for (ErrorFileInfo info : errorFileInfos) {
            System.out.println(info.filePath + " " + info.errorMessage);
        }
    }
    
    private void parseFiles(String folderPath, String invalidIdFilePath, List<File> files, Integer platformId, Class factoryClass, ResourceManager rm)
            throws IOException, InstantiationException, IllegalAccessException, ParseException {
        BaseParsePageFactory factory;
        BaseParsePageTask task;
        if(factoryClass == null) {
            return;
        }
        
        factory = (BaseParsePageFactory) factoryClass.newInstance();
 //       TaskRun taskRun = null;
 //       if(updateTaskRun) {
 //           taskRun = ModelHelper.markParsePageTaskStart(rm, factory.getTaskName(), platformId, folderPath);
 //       }
        factory.loadInvalidIdFilePath(invalidIdFilePath);
        for (File file : files) {
            boolean result = false;
            String fileName = file.getName();
            if (fileName.startsWith(factory.getFilePrefix())) {
                System.out.println("begin to parse " + file.getPath());
                toParseCount++;
                try {
                    task = factory.create(file.getPath(), rm);
                    result = task.run();
                    if (result) {
                        parseSuccessCount++;
                    }
                } catch (JDOMException e) {
                    errorFileInfos.add(new ErrorFileInfo(file.getPath(), e.getMessage()));
                } catch (PageFormatException e) {
                    errorFileInfos.add(new ErrorFileInfo(file.getPath(), e.getMessage()));
                }
                if (toParseCount % 100 == 0) {
                    System.out.println(new Date() + " " + String.format(
                            "parse page success %d, in parse range %d. total page count %d",
                            parseSuccessCount, toParseCount, files.size()));
                }
            }
        }
//        if(updateTaskRun) {
//            ModelHelper.markTaskSuccess(rm, taskRun);
//        }
    }

    private List<File> getValidFiles(File[] files, String folderName) {
        List<File> validFiles = Lists.newArrayList();
        for (File file : files) {
            String[] parts = file.getName().split("-");
            if (parts.length == 4 && (parts[2].length() == 14 || parts[2].length() == 17 ) &&
                    !file.getName().endsWith("swp") && parts[2].startsWith(folderName)) {
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
            File f1 = (File) o1;
            File f2 = (File) o2;
            String[] parts1 = f1.getName().split("-");
            long pageTs1 = Long.valueOf(parts1[2]);
            long importTs1 = Long.valueOf(parts1[3]);
            String[] parts2 = f2.getName().split("-");
            long pageTs2 = Long.valueOf(parts2[2]);
            long importTs2 = Long.valueOf(parts2[3]);
            if (pageTs1 < pageTs2 || (pageTs1 == pageTs2 && importTs1 < importTs2)) {
                return -1;
            } else if (pageTs1 == pageTs2 && importTs1 == importTs2) {
                return 0;
            } else {
                return 1;
            }
        }

    }
}
