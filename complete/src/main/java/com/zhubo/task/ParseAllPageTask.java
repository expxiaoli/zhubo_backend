package com.zhubo.task;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.jdom.JDOMException;

import com.google.common.collect.Lists;
import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.ResourceManager;

public class ParseAllPageTask {

    private static List<Class> parsePageTaskFactoryClasses = Lists.newArrayList(
            ParseQixiuPlatformPageFactory.class, ParseQixiuRoomPageFactory.class);

    public static void main(String[] args) throws JDOMException, IOException, ParseException,
            InstantiationException, IllegalAccessException {
        String folderPath = args[0];
        File folder = new File(folderPath);
        ResourceManager rm = ResourceManager.generateResourceManager();
        int parseSuccessCount = 0;
        int toParseCount = 0;
        int totalPageCount = folder.list().length;
        List<String> errorFilePaths = Lists.newArrayList();
        ParsePageTaskFactory factory;
        BaseParsePageTask task;
        for (Class factoryClass : parsePageTaskFactoryClasses) {
            factory = (ParsePageTaskFactory) factoryClass.newInstance();
            for (File file : folder.listFiles()) {
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

}
