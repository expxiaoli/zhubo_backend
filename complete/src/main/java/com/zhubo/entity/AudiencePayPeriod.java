package com.zhubo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AudiencePayPeriod")
public class AudiencePayPeriod {
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

    @Column(name = "updated")
    private Date updated;

    @Column(name = "record_effective_time")
    private Date recordEffectiveTime;

    @Column(name = "period_start")
    private Date periodStart;

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

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setUpdated() {
        this.updated = new Date();
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public void setPeriodStart(Date start) {
        this.periodStart = start;
    }
    
    public void setRecordEffectiveDate(Date ts) {
        this.recordEffectiveTime = ts;
    }
    
    public AudiencePayPeriod() {
        
    }

    public AudiencePayPeriod(Long audienceId, Long anchorId, Integer platformId, Integer money,
            Date recordEffectiveTime, Date periodStart) {
        this(audienceId, anchorId, platformId, money, recordEffectiveTime, periodStart, new Date(),
                new Date());
    }

    public AudiencePayPeriod(Long audienceId, Long anchorId, Integer platformId, Integer money,
            Date recordEffectiveTime, Date perriodStart, Date created, Date updated) {
        this.audienceId = audienceId;
        this.anchorId = anchorId;
        this.platformId = platformId;
        this.money = money;
        this.recordEffectiveTime = recordEffectiveTime;
        this.created = created;
        this.updated = updated;
    }
}