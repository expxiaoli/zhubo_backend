package com.zhubo.task.parsepage.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.task.BaseParsePageTask;
import com.zhubo.task.parsepage.task.ParsePlatformPageTask;

public class ParseQixiuPlatformPageFactory extends BaseParsePageFactory {
    
    @Override
    public BaseParsePageTask create(String filePath, ResourceManager resourceManager) {
        return new ParsePlatformPageTask(filePath, invalidAliasIds, resourceManager, 1);
    }

    @Override
    public String getFilePrefix() {
        return "平台-奇秀广场";
    }

    @Override
    public String getTaskName() {
        return "ParsePlatformPageTask";
    }

    @Override
    public Set<Integer> getInvalidAliasIds() {
        return invalidAliasIds;
    }

    @Override
    public void loadInvalidIdFilePath(String path) throws NumberFormatException, IOException {
        invalidAliasIds = Sets.newHashSet();
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
     
        String line = null;
        while ((line = br.readLine()) != null) {
            Integer id = Integer.valueOf(line);
            invalidAliasIds.add(id);
        }     
        br.close();
    }
    
}
