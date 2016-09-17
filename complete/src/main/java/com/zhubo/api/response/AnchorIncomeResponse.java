package com.zhubo.api.response;

import java.util.Date;
import java.util.List;

public class AnchorIncomeResponse {
    private final long anchorId;
    private final List<MetricItem> incomes;
    
    public long getAnchorId() {
        return anchorId;
    }
    
    public List<MetricItem> getMetrics() {
        return incomes;
    }
    
    public AnchorIncomeResponse(Long anchorId, List<MetricItem> incomes) {
        this.anchorId = anchorId;
        this.incomes = incomes;
    }
}
