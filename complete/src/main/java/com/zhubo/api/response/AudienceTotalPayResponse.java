package com.zhubo.api.response;

import java.util.List;

public class AudienceTotalPayResponse {
    private final long audienceId;
    private final List<MetricItem> pays;
    
    public long getAnchorId() {
        return audienceId;
    }
    
    public List<MetricItem> getPays() {
        return pays;
    }
    
    public AudienceTotalPayResponse(Long audienceId, List<MetricItem> pays) {
        this.audienceId = audienceId;
        this.pays = pays;
    }
}
