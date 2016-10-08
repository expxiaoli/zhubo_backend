package com.zhubo.api.response;

import java.util.Date;

public class MetricItem {
    public final Long value;
    public final Date ts;

    public MetricItem(int value, Date ts) {
        this.value = Integer.valueOf(value).longValue();
        this.ts = ts;
    }

    public MetricItem(long value, Date ts) {
        this.value = value;
        this.ts = ts;
    }
    
    public long getValue() {
        return value;
    }

    public Date getTs() {
        return ts;
    }
}
