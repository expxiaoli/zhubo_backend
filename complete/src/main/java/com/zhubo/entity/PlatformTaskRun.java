package com.zhubo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "1005_PlatformTaskRun")
public class PlatformTaskRun {
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
    
    @Column(name = "platform_id")
    private Integer platformId;
    
    public String getDataTime() {
        return dataTime;
    }
    
    public PlatformTaskRun() {
        
    }
    
    public PlatformTaskRun(int platformId, String dataTime, Date created) {
        this(platformId, dataTime, created, null, false);
    }
    
    public PlatformTaskRun(int platformId, String dataTime, Date created, Date completed, boolean success) {
        this.platformId = platformId;
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
