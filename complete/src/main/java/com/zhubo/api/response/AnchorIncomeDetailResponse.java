package com.zhubo.api.response;

import java.util.Date;
import java.util.List;


public class AnchorIncomeDetailResponse {
    private final List<AudiencePayItem> audiencePays;
    public static class AudiencePayItem {
        private final long audienceId;
        private final Long audienceAliasId;
        private final String audienceName;
        private final int totalPay;
        private final Date lastPayTime;
        private final double rateInCurAudience;
        private final List<MetricItem> payHistory;
       
        public AudiencePayItem (long audienceId, Long audienceAliasId, String audienceName, int totalPay, Date lastPayTime, double rateInCurAudience, List<MetricItem> payHistory) {
            this.audienceId = audienceId;
            this.audienceAliasId = audienceAliasId;
            this.audienceName = audienceName;
            this.totalPay = totalPay;
            this.lastPayTime = lastPayTime;
            this.rateInCurAudience = rateInCurAudience;
            this.payHistory = payHistory;
        }
        
        public long getAudinceId() {
            return audienceId;
        }
        
        public Long getAudienceAliasId() {
            return audienceAliasId;
        }
        
        public String getAudienceName() {
            return audienceName;
        }
        
        public int getTotalPay() {
            return totalPay;
        }
        
        public Date getLastPayTime() {
            return lastPayTime;
        }
        
        public double getRateInCurAudience() {
            return rateInCurAudience;
        }
        
        public List<MetricItem> getPayHistory() {
            return payHistory;
        }
    }
    
    public List<AudiencePayItem> getAudiencePays() {
        return audiencePays;
    }
    
    public AnchorIncomeDetailResponse(List<AudiencePayItem> audiencePays) {
        this.audiencePays = audiencePays;
    }
}
