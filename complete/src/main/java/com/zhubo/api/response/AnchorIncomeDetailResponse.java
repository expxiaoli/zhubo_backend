package com.zhubo.api.response;

import java.util.Date;
import java.util.List;


public class AnchorIncomeDetailResponse {
    private final List<AnchorIncomeItem> audiencePays;
    public static class AnchorIncomeItem {
        private final long audienceId;
        private final Long audienceAliasId;
        private final String audienceName;
        private final int totalPay;
        private final Date lastPayTime;
        private final double rateInCurAudience;
        private final List<MetricItem> payHistory;
        private final int latest30DaysPay;
        private final int latest7DaysPay;
       
        public AnchorIncomeItem (long audienceId, Long audienceAliasId, String audienceName, int totalPay, Date lastPayTime, double rateInCurAudience, 
                List<MetricItem> payHistory, int latest7DaysPay, int latest30daysPay) {
            this.audienceId = audienceId;
            this.audienceAliasId = audienceAliasId;
            this.audienceName = audienceName;
            this.totalPay = totalPay;
            this.lastPayTime = lastPayTime;
            this.rateInCurAudience = rateInCurAudience;
            this.payHistory = payHistory;
            this.latest7DaysPay = latest7DaysPay;
            this.latest30DaysPay = latest30daysPay; 
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
        
        public int getLatest7DaysPay() {
            return latest7DaysPay;
        }
        
        public int getLatest30DaysPay() {
            return latest30DaysPay;
        }
        
        
    }
    
    public List<AnchorIncomeItem> getAudiencePays() {
        return audiencePays;
    }
    
    public AnchorIncomeDetailResponse(List<AnchorIncomeItem> audiencePays) {
        this.audiencePays = audiencePays;
    }
}
