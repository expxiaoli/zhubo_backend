package com.zhubo.api.response;

import java.util.Date;
import java.util.List;

public class AudienceTotalPayRankResponse {
    
    private final List<AudienceTotalPayRankItem> rankItems;
    public static class AudienceTotalPayRankItem {
        private Integer rank;
        private final Long id;
        private Long aliasId;
        private String name;
        private final Integer value;
        private double rate;
        private List<MetricItem> payHistory;
        private Date latestPayDate;
        
        public AudienceTotalPayRankItem(Long id, Long aliasId, Integer rank, String name, Integer value) {
            this.id = id;
            this.aliasId = aliasId;
            this.rank = rank;
            this.name = name;
            this.value = value;
        }
        
        public Integer getRank() {
            return rank;
        }
        
        public String getName() {
            return name;
        }
        
        public Integer getValue() {
            return value;
        }
        
        public Long getId() {
            return id;
        }
        
        public Long getAliasId() {
            return aliasId;
        }
        
        public Double getRate() {
            return rate;
        }
        
        public Date getLatestPayDate() {
            return latestPayDate;
        }
        
        public List<MetricItem> getPayHistory() {
            return payHistory;
        }
        
        public void setAliasID(Long aliasId) {
            this.aliasId = aliasId;
        }
        
        public void setRank(Integer rank) {
            this.rank = rank;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setRate(Double rate) {
            this.rate = rate;
        }
        
        public void setPayHistory(List<MetricItem> payHistory) {
            this.payHistory = payHistory;
        }
        
        public void setLatestPayDate(Date date) {
            this.latestPayDate = date;
        }
    }
    
    public List<AudienceTotalPayRankItem> getRankItems() {
        return rankItems;
    }
    
    public AudienceTotalPayRankResponse(List<AudienceTotalPayRankItem> rankItems) {
        this.rankItems = rankItems;
    }
}
