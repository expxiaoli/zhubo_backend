package com.zhubo.api.response;

import java.util.Date;
import java.util.List;

public class AnchorMetricResponse {
    private final long anchorId;
    private final String type;
    private final List<AnchorMetric> metrics;
    
    public long getAnchorId() {
        return anchorId;
    }
    
    public String getType() {
        return type;
    }
    
    public List<AnchorMetric> getMetrics() {
        return metrics;
    }

    public static class AnchorMetric {
        public final int value;
        public final Date ts;

        public AnchorMetric(int value, Date ts) {
            this.value = value;
            this.ts = ts;
        }

        public int getValue() {
            return value;
        }

        public Date getTs() {
            return ts;
        }
    }

    public AnchorMetricResponse(Long anchorId, String type, List<AnchorMetric> metrics) {
        this.anchorId = anchorId;
        this.type = type;
        this.metrics = metrics;
    }
}
