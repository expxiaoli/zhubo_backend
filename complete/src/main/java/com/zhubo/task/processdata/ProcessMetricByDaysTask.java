package com.zhubo.task.processdata;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.common.collect.Maps;
import com.zhubo.entity.AnchorMetricByDays;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;

public class ProcessMetricByDaysTask extends BaseProcessDataTask {

    private static final int limit = 5000;
    private TimeUnit timeUnit = TimeUnit.DAY; 

    private Map<Long, Map<String, Map<Date, Integer>>> metrics; // [anchor_id][type][date]

    public ProcessMetricByDaysTask(ResourceManager rm, int platformId) {
        super(rm, platformId);
    }
    
    @Override
    public boolean run() {
        metrics = Maps.newHashMap();
        long lowerBoundId = 0;
        long maxAnchorId = 0;
        while (true) {
            System.out.println(String.format("begin to process metric for platform %d, id after %d", platformId, lowerBoundId));
            Query query = resourceManager.getDatabaseSession()
                    .createQuery("from AnchorMetricByMinutes where anchor_id > :lower_bound_id"
                            + " and platform_id = :platform_id and "
                            + " record_effective_time > :min_time and record_effective_time < :max_time order by anchor_id asc");
            query.setParameter("lower_bound_id", lowerBoundId);
            query.setMaxResults(limit);
            query.setParameter("platform_id", platformId);
            Date minTime = (start == null) ? new Date(0, 1, 1) : start;
            Date maxTime = (end == null) ? new Date(2100-1900, 1, 1) : end;
            query.setParameter("min_time", minTime);
            query.setParameter("max_time", maxTime);
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
            System.out.println("max anchor id: " + maxAnchorId);
            if(isLastAnchor()) {
                storeMetric();
                clearMetric();
                break;
            } else {
                removeFromMetric(maxAnchorId);
                storeMetric();
                clearMetric();
                lowerBoundId = maxAnchorId - 1;
            }
        }
        System.out.println(String.format("process metric for platform %d done", platformId));
        return true;
    }
    
    private void removeFromMetric(Long anchorId) {
        metrics.remove(anchorId);
    }
    
    private boolean isLastAnchor() {
        return metrics.keySet().size() == 1;
    }

    private void storeMetric() {
        for (Long anchorId : metrics.keySet()) {
            Map<String, Map<Date, Integer>> anchorMetric = metrics.get(anchorId);
            for (String type : anchorMetric.keySet()) {
                Map<Date, Integer> typeMetric = anchorMetric.get(type);
                for (Date date : typeMetric.keySet()) {
                    if(!resourceManager.getDatabaseCache().existInAnchorMetricByDays(anchorId, type, date)) {
                        AnchorMetricByDays byDays = new AnchorMetricByDays(anchorId, platformId, type, typeMetric.get(date), date);
                        resourceManager.getDatabaseSession().save(byDays);
                    }
                }
            }
        }
        resourceManager.commit();
    }
    
    private void clearMetric() {
        metrics.clear();
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
