package com.zhubo.api.response;

import java.util.List;

public class AudienceTotalPayRankChangeResponse {
    public List<AudienceTotalPayRankChangeItem> audienceTotalPayRankChangeItems;
    
    public static class AudienceTotalPayRankChangeItem {
        private Long audienceId;
        private Long audienceAliasId;
        private String audienceName;
        private Integer rank1To7Day;
        private Integer rank8To14Day;
        private Integer rank1To30Day;
        
        public Long getAudienceId() {
            return audienceId;
        }
        
        public Long getAudienceAliasId() {
            return audienceAliasId;
        }
        
        public String getAudienceName() {
            return audienceName;
        }
        
        public Integer getRank1To7Day() {
            return rank1To7Day;
        }
        
        public Integer getRank8To14Day() {
            return rank8To14Day;
        }
        
        public Integer getRank1To30Day() {
            return rank1To30Day;
        }
        
        public AudienceTotalPayRankChangeItem(Long audienceId, Long audienceAliasId, String audienceName, Integer rank1To7Day,
                Integer rank8To14Day, Integer rank1To30Day) {
            this.audienceId = audienceId;
            this.audienceAliasId = audienceAliasId;
            this.audienceName = audienceName;
            this.rank1To7Day = rank1To7Day;
            this.rank8To14Day = rank8To14Day;
            this.rank1To30Day = rank1To30Day;                   
        }
    }
    
    public List<AudienceTotalPayRankChangeItem> getAudienceTotalPayRankChangeItems() {
        return audienceTotalPayRankChangeItems;
    }
    
    public AudienceTotalPayRankChangeResponse(List<AudienceTotalPayRankChangeItem> audienceTotalPayRankChangeItems) {
        this.audienceTotalPayRankChangeItems = audienceTotalPayRankChangeItems;
    }
}
