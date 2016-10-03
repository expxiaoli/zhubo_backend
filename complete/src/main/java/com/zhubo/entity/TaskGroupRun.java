package com.zhubo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TaskGroupRun")
public class TaskGroupRun {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "created")
    private Date created;
    
    @Column(name = "completed")
    private Date completed;
    
    @Column(name = "success")
    private boolean success;
    
    @Column(name = "data_time")
    private String dataTime;
    
    public String getDataTime() {
        return dataTime;
    }
    
    public TaskGroupRun() {
        
    }
    
    public TaskGroupRun(String dataTime, Date created) {
        this(dataTime, created, null, false);
    }
    
    public TaskGroupRun(String dataTime, Date created, Date completed, boolean success) {
        this.dataTime = dataTime;
        this.created = created;
        this.completed = completed;
        this.success = success;
    }
    
    public void setSuccessAndCompleted(boolean success, Date completed) {
        this.success = success;
        this.completed = completed;
    }
}
