package com.zhubo.entity;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "Anchor", indexes = {
        @Index(columnList = "platform_id,anchor_id", name = "anchor_simple_index")
})
public class Anchor {
	@Id
	@GeneratedValue
	@Column(name = "anchor_id")
	private Long anchorId;
	
	@Column(name = "platform_id")
	private Integer platformId;
	
	@Column(name = "anchor_alias_id")
	private Long anchorAliasId;
	
	@Column(name = "anchor_name")
	private String anchorName;
	
	@Column(name = "created")
	private Date created;
	
	@Column(name = "last_updated")
	private Date lastUpdated;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "enabled")
	private boolean enabled;
	
	@Column(name = "area")
	private String area;
	
	public Anchor() {
		
	}
	
	public Anchor(Integer platformId, Long anchorAliasId, String anchorName) {
		this(platformId, anchorAliasId, anchorName, new Date(), new Date(), true);
	}
	
	public Anchor(Integer platformId, Long anchorAliasId, String anchorName, Date created, Date lastUpdated,  boolean enabled) {
		this.platformId = platformId;
		this.anchorAliasId = anchorAliasId;
		this.anchorName = anchorName;
		this.created = created;
		this.lastUpdated = lastUpdated;
		this.enabled = enabled;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}

	public void setArea(String area) {
		this.area = area;
	}
	
	public String getArea() {
		return area;
	}
	
	public String getAnchorName() {
	    return anchorName;
	}
	
	public long getAnchorId() {
	    return anchorId;
	}
	
	public String toString() {
		return String.format("id:%d platform_id:%d anchor_alias_id:%d "
				+ "anchor_name:%s type:%s area:%s", 
				anchorId, platformId, anchorAliasId, anchorName, type, area);
	}
}
