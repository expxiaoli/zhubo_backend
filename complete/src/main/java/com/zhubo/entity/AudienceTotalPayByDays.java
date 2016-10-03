package com.zhubo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AudienceTotalPayByDays_1003")
public class AudienceTotalPayByDays {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
        
    @Column(name = "audience_id")
    private Long audienceId;
    
    @Column(name = "platform_id")
    private Integer platformId;
        
    @Column(name = "value")
    private Integer money;
    
    @Column(name = "created")
    private Date created;
    
    @Column(name = "record_effective_time")
    private Date recordEffectiveTime;
    
    public Long getAudienceId() {
        return audienceId;
    }
    
    public Integer getPlatformId() {
        return platformId;
    }
    
    public Integer getMoney() {
        return money;
    }
    
    public Date getRecordEffectiveTime() {
        return recordEffectiveTime;
    }
    
    public AudienceTotalPayByDays() {
        
    }
    
    public AudienceTotalPayByDays(Long audienceId, Integer platformId, Integer money, Date recordEffectiveTime) {
        this(audienceId, platformId, money, recordEffectiveTime, new Date());
    }
    
    public AudienceTotalPayByDays(Long audienceId, Integer platformId, Integer money, Date recordEffectiveTime, Date created) {
        this.audienceId = audienceId;
        this.platformId = platformId;
        this.money = money;
        this.recordEffectiveTime = recordEffectiveTime;
        this.created = created;                
    }
}
