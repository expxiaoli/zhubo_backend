package com.zhubo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pl_TaskRun")
public class TaskRun {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "task_name")
    private String taskName;
    
    @Column(name = "task_type")
    private String taskType;
    
    @Column(name = "platform_id")
    private Integer platformId;
    
    @Column(name = "created")
    private Date created;
    
    @Column(name = "completed")
    private Date completed;
    
    @Column(name = "success")
    private boolean success;
    
    @Column(name = "related_info")
    private String relatedInfo;
    
    @Column(name = "folder_path")
    private String folderPath;
    
    @Column(name = "process_start")
    private Date processStart;
    
    @Column(name = "process_end")
    private Date processEnd;
    
    public void setSuccessAndCompleted(boolean success, Date completed) {
        this.success = success;
        this.completed = completed;
    }
    
    public TaskRun(String taskName, String taskType, Integer platformId, String folderPath, Date processStart, Date processEnd, Date created) {
        this(taskName, taskType, platformId, folderPath, processStart, processEnd, created, null, false, "");
    }
    
    public TaskRun(String taskName, String taskType, Integer platformId, String folderPath, Date processStart, Date processEnd, Date created, Date completed, boolean success, String relatedInfo) {
        this.taskName = taskName;
        this.taskType = taskType;
        this.platformId = platformId;
        this.folderPath = folderPath;
        this.processStart = processStart;
        this.processEnd = processEnd;
        this.created = created;
        this.completed = completed;
        this.success = success;
        this.relatedInfo = relatedInfo;
    }
}
