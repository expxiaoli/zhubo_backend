package com.zhubo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "1004_AnchorMetricByMinutes")
public class AnchorMetricByMinutes {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    
    @Column(name = "anchor_id")
    private Long anchorId;
    
    @Column(name = "platform_id")
    private Integer platformId;    
    
    @Column(name = "type")
    private String type;
        
    @Column(name = "value")
    private Integer value;
    
    @Column(name = "created")
    private Date created;
    
    @Column(name = "record_effective_time")
    private Date recordEffectiveTime;
    
    public AnchorMetricByMinutes() {
        
    }
    
    public AnchorMetricByMinutes(Long anchorId, Integer platformId, String type, Integer value, Date recordEffectiveTime) {
        this.anchorId = anchorId;
        this.platformId = platformId;
        this.type = type;
        this.value = value;
        this.recordEffectiveTime = recordEffectiveTime;
        this.created = new Date();
    }
    
    public String getType() {
        return type;
    }
    
    public Integer getValue() {
        return value;
    }
    
    public Long getAnchorId() {
        return anchorId;
    }
    
    public Date getRecordEffectiveTime() {
        return recordEffectiveTime;
    }
    
}
