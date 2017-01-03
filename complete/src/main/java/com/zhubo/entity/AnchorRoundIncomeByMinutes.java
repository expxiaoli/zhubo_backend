package com.zhubo.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "1005_AnchorRoundIncomeByMinutes")
public class AnchorRoundIncomeByMinutes {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "anchor_id")
    private Long anchorId;

    @Column(name = "platform_id")
    private Integer platformId;

    @Column(name = "money")
    private Long money;

    @Column(name = "created")
    private Date created;

    @Column(name = "record_effective_time")
    private Date recordEffectiveTime;

    public AnchorRoundIncomeByMinutes() {

    }

    public AnchorRoundIncomeByMinutes(Long anchorId, Integer platformId, Long money,
            Date recordEffectiveTime) {
        this.anchorId = anchorId;
        this.platformId = platformId;
        this.money = money;
        this.recordEffectiveTime = recordEffectiveTime;
        this.created = new Date();
    }

    public Long getAnchorId() {
        return anchorId;
    }

    public Long getMoney() {
        return money;
    }

    public Integer getPlatformId() {
        return platformId;
    }

    public Date getRecordEffectiveTime() {
        return recordEffectiveTime;
    }
}
