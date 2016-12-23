package com.zhubo.task.main;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.common.collect.Lists;
import com.zhubo.entity.PlatformTaskRun;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.ModelHelper;
import com.zhubo.task.parsepage.ParseOnePlatformTask;
import com.zhubo.task.processdata.ProcessOnePlatformTask;

public class FreeParseAndProcessApp {
    private ResourceManager rm;
    private String catchupFolderPath;
    private String invalidIdFilePath;
    private List<Integer> platformIdsToProcess;
    
    public static void main(String[] args) throws InstantiationException, IllegalAccessException,
    IOException, ParseException {
        if(args.length != 3) {
            System.out.println("args: catchup_folder invalid_uid_folder platform_ids");
            return;
        }
        new FreeParseAndProcessApp(args[0], args[1], args[2]).run();
        System.exit(0);
    }

    
    public FreeParseAndProcessApp(String catchupFolderPath, String invalidIdFilePath, String platformIdStrs) {
        this.catchupFolderPath = catchupFolderPath;
        this.invalidIdFilePath = invalidIdFilePath;
        String[] platformIdParts = platformIdStrs.split(",");
        platformIdsToProcess = Lists.newArrayList();
        for(String platformIdPart : platformIdParts) {
            platformIdsToProcess.add(Integer.valueOf(platformIdPart));
        }
        rm = ResourceManager.generateResourceManager();
    }
    
    public void run() throws InstantiationException, IllegalAccessException, IOException, ParseException {
        List<String> validFolderNames = getValidFolderNames();
        for(int platformId : platformIdsToProcess) {
            List<String> sortedFolderNamesToProcess = getSortedFolderNamesToProcess(validFolderNames, platformId);
            for(String folderName : sortedFolderNamesToProcess) {
                processDateFolder(platformId, folderName, invalidIdFilePath);
            }
        }
        System.out.println("ParseAndProcessApp done");
    }

    public void processDateFolder(int platformId, String dateFolderName, String invalidIdFilePath) throws InstantiationException, IllegalAccessException, IOException,
            ParseException {


        PlatformTaskRun ptr = ModelHelper.markPlatformTaskRunStart(rm, dateFolderName, platformId);
        ParseOnePlatformTask parseTask = new ParseOnePlatformTask(platformId);
        String dataFolderPath = catchupFolderPath + "/" + dateFolderName;
        System.out.println("begin to parse files in folder: " + dataFolderPath + ", platform id: " + platformId);
        Date startFolderDate = parseFromFolderName(dateFolderName);
        Date endFolderDate = addDay(startFolderDate, 1);
        parseTask.run(dataFolderPath, invalidIdFilePath);

        ProcessOnePlatformTask processTask = new ProcessOnePlatformTask(platformId);
        System.out.println("begin to process data from " + startFolderDate.toString() + " to " + endFolderDate.toString()  + ", platform id: " + platformId);
        processTask.setDates(startFolderDate, endFolderDate);
        processTask.run();

        ModelHelper.markPlatformTaskRunSuccess(rm, ptr);
        System.out.println("parse and process file done: " + dateFolderName);
    }
    
    private Date parseFromFolderName(String name) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.parse(name);
    }
    
    private Date addDay(Date date, int day) {
        Calendar now = Calendar.getInstance();  
        now.setTime(date);  
        now.add(Calendar.DAY_OF_YEAR, day);  
        return now.getTime(); 
    }
    
    private List<String> getSortedFolderNamesToProcess(List<String> validFolderNames, int platformId) {
        String latestProcessedDate = getLatestProcessedDate(platformId);
        List<String> folderNamesToProcess = Lists.newArrayList();
        for(String folderName : validFolderNames) {
            if(latestProcessedDate == null || folderName.compareTo(latestProcessedDate) > 0) {
                folderNamesToProcess.add(folderName);
            }
        }
        Collections.sort(folderNamesToProcess, new byFolderTimeComparator());
        return folderNamesToProcess;
    }
    
    public class byFolderTimeComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            String s1 = (String)o1;
            String s2 = (String)o2;   
            return s1.compareTo(s2);
        }
        
    }

    private List<String> getValidFolderNames() {
        File rootFolder = new File(catchupFolderPath);
        File[] dateFolders = rootFolder.listFiles();
        List<String> validFolderNames = Lists.newArrayList();
        for(File dateFolder : dateFolders) {
            String folderName = dateFolder.getName();
            if(isValidDateFolderName(folderName)) {
                validFolderNames.add(folderName);
            }
        }
        return validFolderNames;
    }

    private boolean isValidDateFolderName(String name) {
        char[] validChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
        if (name.length() == 8) {
            for(char c : name.toCharArray()) {
                boolean isExist = false;            
                for(char validChar : validChars) {
                    if(c == validChar) {
                        isExist = true;
                    }
                }
                if(isExist == false) {
                    return false;
                }
            }
            if(name.compareTo("20161223") >= 0) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public String getLatestProcessedDate(int platformId) {
        Session session = rm.getDatabaseSession();
        Query query = session
                .createQuery(String.format(
                        "from PlatformTaskRun where success = 1 and platform_id = %d order by data_time desc", platformId));
        List<PlatformTaskRun> tgrs = query.list();
        if (tgrs.size() > 0) {
            PlatformTaskRun tgr = tgrs.get(0);
            return tgr.getDataTime();
        } else {
            return null;
        }
    }
}
