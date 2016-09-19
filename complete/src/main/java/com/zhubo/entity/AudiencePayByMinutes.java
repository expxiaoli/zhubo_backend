package com.zhubo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AudiencePayByMinutes_3refine_0919")
public class AudiencePayByMinutes {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
        
    @Column(name = "audience_id")
    private Long audienceId;
    
    @Column(name = "anchor_id")
    private Long anchorId;
    
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
    
    public Long getAnchorId() {
        return anchorId;
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
    
    public AudiencePayByMinutes() {
        
    }
    
    public AudiencePayByMinutes(Long audienceId, Long anchorId, Integer platformId, Integer money, Date recordEffectiveTime) {
        this(audienceId, anchorId, platformId, money, recordEffectiveTime, new Date());
    }
    
    public AudiencePayByMinutes(Long audienceId, Long anchorId, Integer platformId, Integer money, Date recordEffectiveTime, Date created) {
        this.audienceId = audienceId;
        this.anchorId = anchorId;
        this.platformId = platformId;
        this.money = money;
        this.recordEffectiveTime = recordEffectiveTime;
        this.created = created;                
    }
}
