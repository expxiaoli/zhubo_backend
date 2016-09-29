package com.zhubo.task.main;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import com.zhubo.task.parsepage.ParseAllPageTask;
import com.zhubo.task.processdata.ProcessAllDataTask;

public class ParseAndProcessApp {
    
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, IOException, ParseException {
        ParseAllPageTask parseTask = new ParseAllPageTask();
        parseTask.setUpdateTaskRun(true);
        parseTask.run("/backup/catch/20160927");
        
        ProcessAllDataTask processTask = new ProcessAllDataTask();
        processTask.setUpdateTaskRun(true);
        processTask.setDates(new Date(2016-1900, 8, 27), new Date(2016-1900, 8, 28));
    }
}
