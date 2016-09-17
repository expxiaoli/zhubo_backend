package com.zhubo.api.response;

import java.util.List;

public class AnchorMetricResponse {
    private final long anchorId;
    private final String type;
    private final List<MetricItem> metrics;
    
    public long getAnchorId() {
        return anchorId;
    }
    
    public String getType() {
        return type;
    }
    
    public List<MetricItem> getMetrics() {
        return metrics;
    }

    public AnchorMetricResponse(Long anchorId, String type, List<MetricItem> metrics) {
        this.anchorId = anchorId;
        this.type = type;
        this.metrics = metrics;
    }
}
