package com.zhubo.api.response;

import java.util.Date;

public class MetricItem {
    public final int value;
    public final Date ts;

    public MetricItem(int value, Date ts) {
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
