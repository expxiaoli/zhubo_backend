package com.zhubo.api.response;

import java.util.List;

public class RankResponse {
    
    private final List<RankItem> rankItems;
    public static class RankItem {
        private Integer rank;
        private final Long id;
        private String name;
        private final Integer value;
        
        public RankItem(Long id, Integer rank, String name, Integer value) {
            this.id = id;
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
