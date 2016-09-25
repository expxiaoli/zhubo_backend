package com.zhubo.api.response;

public class AudienceResponse {
    private final Long audienceId;
    private final Long audienceAliasId;
    private final String audienceName;
   
    public AudienceResponse(long audienceId, Long audienceAliasId, String audienceName) {
        this.audienceId = audienceId;
        this.audienceAliasId = audienceAliasId;
        this.audienceName = audienceName;
    }
   
    public Long getAudienceId() {
        return audienceId;
    }
   
    public String getAudienceName() {
        return audienceName;
    }
    
    public Long getAudienceAliasId() {
        return audienceAliasId;
    }
}
