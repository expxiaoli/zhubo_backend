package com.zhubo.entity;

import javax.persistence.*;

@Entity
@Table(name = "Platform")
public class Platform {
	@Id
	@GeneratedValue
	@Column(name = "platform_id")
	private Integer platformId;
	
	@Column(name = "platform_name")
	private String platformName;
	
	public Platform() {
		
	}
	
	public Platform(Integer platformId, String platformName) {
		this.platformId = platformId;
		this.platformName = platformName;
	}
	
	public void setPlatformId(Integer platformId) {
		this.platformId = platformId;
	}
	public void setPlarformName(String platformName) {
		this.platformName = platformName;
	}
	public Integer getPlatformId() {
		return platformId;
	}
	public String getPlatformName() {
		return platformName;
	}
}
