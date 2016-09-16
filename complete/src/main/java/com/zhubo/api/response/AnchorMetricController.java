package com.zhubo.api.response;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.zhubo.api.response.AnchorMetricResponse.AnchorMetric;
import com.zhubo.entity.AnchorMetricByDays;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.global.ResourceManager;

@RestController
public class AnchorMetricController {

    private final Session session = ResourceManager.generateResourceManager().getDatabaseSession();

    @RequestMapping("/anchor_metric_minute")
    public AnchorMetricResponse getMetricMinute(@RequestParam(value="anchor_id") Long anchorId, @RequestParam(value="type") String type) {
        Query query = session.createQuery("from AnchorMetricByMinutes where anchor_id = :anchor_id and type = :type");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("type", type);
        List<AnchorMetricByMinutes> metrics = query.list();
        List<AnchorMetric> items = Lists.newArrayList();
        for(AnchorMetricByMinutes metric : metrics) {
            items.add(new AnchorMetric(metric.getValue(), metric.getRecordEffectiveTime()));
        }
        return new AnchorMetricResponse(anchorId, type, items);
    }
    
    @RequestMapping("/anchor_metric_day")
    public AnchorMetricResponse getMetricDay(@RequestParam(value="anchor_id") Long anchorId, @RequestParam(value="type") String type) {
        Query query = session.createQuery("from AnchorMetricByDays where anchor_id = :anchor_id and type = :type");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("type", type);
        List<AnchorMetricByDays> metrics = query.list();
        List<AnchorMetric> items = Lists.newArrayList();
        for(AnchorMetricByDays metric : metrics) {
            items.add(new AnchorMetric(metric.getValue(), metric.getRecordEffectiveTime()));
        }
        return new AnchorMetricResponse(anchorId, type, items);
    }
    
    
    
}