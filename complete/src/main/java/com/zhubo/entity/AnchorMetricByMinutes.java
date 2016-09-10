package com.zhubo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AnchorMetricByMinutes")
public class AnchorMetricByMinutes {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    
    @Column(name = "anchor_id")
    private Long anchorId;
    
    @Column(name = "type")
    private String type;
        
    @Column(name = "value")
    private Integer value;
    
    @Column(name = "created")
    private Date created;
    
    @Column(name = "record_effective_time")
    private Date recordEffectiveTime;
    
    public AnchorMetricByMinutes(Long anchorId, String type, Integer value, Date recordEffectiveTime) {
        this.anchorId = anchorId;
        this.type = type;
        this.value = value;
        this.recordEffectiveTime = recordEffectiveTime;
        this.created = new Date();
    }
    
}