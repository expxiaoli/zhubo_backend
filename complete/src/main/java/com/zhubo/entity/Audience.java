package com.zhubo.entity;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "1004_Audience", indexes = {
        @Index(columnList = "platform_id,audience_id", name = "audience_simple_index")
})
public class Audience {
    @Id
    @GeneratedValue
    @Column(name = "audience_id")
    private Long audienceId;
    
    @Column(name = "platform_id")
    private Integer platformId;
    
    @Column(name = "audience_alias_id")
    private Long audienceAliasId;
    
    @Column(name = "audience_name")
    private String audienceName;
    
    @Column(name = "created")
    private Date created;
    
    @Column(name = "last_updated")
    private Date lastUpdated;
    
    @Column(name = "enabled")
    private boolean enabled;
    
    public Audience() {
        
    }
    
    public Audience(Integer platformId, Long audienceAliasId, String audienceName) {
        this(platformId, audienceAliasId, audienceName, new Date(), new Date(), true);
    }
    
    public Audience(Integer platformId, Long audienceAliasId, String audienceName, Date created, Date lastUpdated,  boolean enabled) {
        this.platformId = platformId;
        this.audienceAliasId = audienceAliasId;
        this.audienceName = audienceName;
        this.created = created;
        this.lastUpdated = lastUpdated;
        this.enabled = enabled;
    }
    
    public String getAudienceName() {
        return audienceName;
    }
    
    public Long getAudienceId() {
        return audienceId;
    }
    
    public Long getAudienceAliasId() {
        return audienceAliasId;
    }
    
    public Integer getPlatformId() {
        return platformId;
    }
    
    public void setAudienceAliasId(long audienceAliasId) {
        this.audienceAliasId = audienceAliasId;
    }
    
    public void setAudienceName(String audienceName) {
        this.audienceName = audienceName;
    }
    
    public String toString() {
        return String.format("id:%d platform_id:%d audience_alias_id:%d "
                + "audience_name:%s", 
                audienceId, platformId, audienceAliasId, audienceName);
    }
}
