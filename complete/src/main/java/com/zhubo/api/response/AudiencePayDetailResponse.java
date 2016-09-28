package com.zhubo.api.response;

import java.util.List;

public class AudiencePayDetailResponse {
    
    private final List<AudiencePayItem> audiencePays;
    public static class AudiencePayItem {
        private final long anchorId;
        private final Long anchorAliasId;
        private final String anchorName;
        private final int totalIncome;
        private final double rateInCurAudience;
        private final List<MetricItem> payHistory;
        
        public AudiencePayItem(long anchorId, Long anchorAliasId, String anchorName, int totalIncome, 
                double rateInCurAudience, List<MetricItem> payHistory) {
            this.anchorId = anchorId;
            this.anchorAliasId = anchorAliasId;
            this.anchorName = anchorName;
            this.totalIncome = totalIncome;
            this.rateInCurAudience = rateInCurAudience;
            this.payHistory = payHistory;
        }
        
        public Long getAnchorId() {
            return anchorId;
        }
        
        public Long getAnchorAliasId() {
            return anchorAliasId;
        }
        
        public String getAnchorName() {
            return anchorName;
        }
        
        public Integer getTotalIncome() {
            return totalIncome;
        }
        
        public Double getRateInCurAudience() {
            return rateInCurAudience;                      
        }
        
        public List<MetricItem> getPayHistory() {
            return payHistory; 
        }
    }
    
    public List<AudiencePayItem> getAudiencePayItems() {
        return audiencePays;
    }
    
    public AudiencePayDetailResponse(List<AudiencePayItem> audiencePays) {
        this.audiencePays = audiencePays;
    }
}
