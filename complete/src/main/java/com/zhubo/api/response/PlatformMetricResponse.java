package com.zhubo.api.response;

import java.util.List;

public class PlatformMetricResponse {
    public List<MetricItem> metricItems;
    
    public PlatformMetricResponse(List<MetricItem> metricItems) {
        this.metricItems = metricItems;
    }
    
    public List<MetricItem> getMetricItems() {
        return metricItems;
    }
}
