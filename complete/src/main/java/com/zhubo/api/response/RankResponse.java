package com.zhubo.api.response;

import java.util.List;

public class RankResponse {
    
    private final List<RankItem> rankItems;
    public static class RankItem {
        private Integer rank;
        private final Long id;
        private Long aliasId;
        private String name;
        private final Integer value;
        
        public RankItem(Long id, Long aliasId, Integer rank, String name, Integer value) {
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
        
        public void setAliasID(Long aliasId) {
            this.aliasId = aliasId;
        }
        
        public void setRank(Integer rank) {
            this.rank = rank;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public List<RankItem> getRankItems() {
        return rankItems;
    }
    
    public RankResponse(List<RankItem> rankItems) {
        this.rankItems = rankItems;
    }
}
