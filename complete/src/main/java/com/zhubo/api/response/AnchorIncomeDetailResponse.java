package com.zhubo.api.response;

import java.util.Date;
import java.util.List;


public class AnchorIncomeDetailResponse {
    private final List<AnchorIncomeItem> audiencePays;
    public static class AnchorIncomeItem {
        private final long audienceId;
        private final Long audienceAliasId;
        private final String audienceName;
        private final Long totalPay;
        private final Date lastPayTime;
        private final double rateInCurAudience;
        private final List<MetricItem> payHistory;
        private final Long latest30DaysPay;
        private final Long latest7DaysPay;
       
        public AnchorIncomeItem (long audienceId, Long audienceAliasId, String audienceName, Long totalPay, Date lastPayTime, double rateInCurAudience, 
                List<MetricItem> payHistory, Long latest7DaysPay, Long latest30daysPay) {
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
        
        public Long getTotalPay() {
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
        
        public Long getLatest7DaysPay() {
            return latest7DaysPay;
        }
        
        public Long getLatest30DaysPay() {
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
