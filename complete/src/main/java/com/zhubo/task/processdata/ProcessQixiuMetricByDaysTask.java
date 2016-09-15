package com.zhubo.task.processdata;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.common.collect.Maps;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;

public class ProcessQixiuMetricByDaysTask extends BaseProcessDataTask {

    private static final int limit = 10;
    private TimeUnit timeUnit = TimeUnit.DAY;

    private Map<Long, Map<String, Map<Date, Integer>>> metrics; // [anchor_id][type][date]

    public ProcessQixiuMetricByDaysTask(ResourceManager rm) {
        super(rm);
    }

    @Override
    public boolean run() {
        metrics = Maps.newHashMap();
        Session session = resourceManager.getDatabaseSession();
        long lowerBoundId = 0;
        long upperBoundId = lowerBoundId + limit;
        long maxAnchorId = 0;
        while (true) {
            Query query = session
                    .createQuery("from AnchorMetricByMinutes where anchor_id > :lower_bound_id and anchor_id < :upper_bound_id");
            query.setParameter("lower_bound_id", lowerBoundId);
            query.setParameter("upper_bound_id", upperBoundId);
            List<AnchorMetricByMinutes> records = query.list();
            if (records.size() == 0) {
                break;
            }
            for (AnchorMetricByMinutes record : records) {
                long anchorId = record.getAnchorId();
                String type = record.getType();
                Date ts = record.getRecordEffectiveTime();
                int value = record.getValue();
                Date aggregateDate = GeneralHelper.getAggregateDate(ts, timeUnit);
                addMetric(anchorId, type, aggregateDate, value);
                if (maxAnchorId < anchorId) {
                    maxAnchorId = anchorId;
                }
            }
            lowerBoundId = maxAnchorId;
            upperBoundId = lowerBoundId + limit;

            break;
        }
        print();
        return true;
    }

    private void print() {
        for (Long anchorId : metrics.keySet()) {
            Map<String, Map<Date, Integer>> anchorMetric = metrics.get(anchorId);
            for (String type : anchorMetric.keySet()) {
                Map<Date, Integer> typeMetric = anchorMetric.get(type);
                for (Date date : typeMetric.keySet()) {
                    System.out.println(anchorId + " " + type + " " + date + " "
                            + typeMetric.get(date));
                }
            }
        }
    }

    private void addMetric(long anchorId, String type, Date date, int count) {
        Map<String, Map<Date, Integer>> anchorMetric = metrics.get(anchorId);
        if (anchorMetric == null) {
            metrics.put(anchorId, Maps.newHashMap());
            anchorMetric = metrics.get(anchorId);
        }

        Map<Date, Integer> typeMetric = anchorMetric.get(type);
        if (typeMetric == null) {
            anchorMetric.put(type, Maps.newHashMap());
            typeMetric = anchorMetric.get(type);
        }

        Integer oldCount = typeMetric.get(date);
        if (oldCount == null) {
            typeMetric.put(date, 0);
            oldCount = 0;
        }
        int newCount = oldCount > count ? oldCount : count;
        typeMetric.put(date, newCount);
    }

}
