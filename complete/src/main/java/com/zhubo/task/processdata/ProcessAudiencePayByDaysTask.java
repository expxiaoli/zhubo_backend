package com.zhubo.task.processdata;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;

import com.google.common.collect.Maps;
import com.zhubo.entity.AudiencePayByDays;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;

public class ProcessAudiencePayByDaysTask extends BaseProcessDataTask {
    private static final int limit = 10000;
    private TimeUnit timeUnit = TimeUnit.DAY; 
    
    private Map<Long, Map<Long, Map<Date, Integer>>> metrics; // [audience_id][anchor_id][date]

    public ProcessAudiencePayByDaysTask(ResourceManager resourceManager, int platformId) {
        super(resourceManager, platformId);
    }

    @Override
    public boolean run() {
        metrics = Maps.newHashMap();
        long lowerBoundId = 0;
        long maxAudineceId = 0;
        while(true) {
            System.out.println(String.format("begin to process audience pay by days for platform %d, id after %d", platformId, lowerBoundId));
            Query query = resourceManager.getDatabaseSession()
                    .createQuery("from AudiencePayByMinutes where audience_id > :lower_bound_id"
                            + " and platform_id = :platform_id and "
                            + " record_effective_time >= :min_time and record_effective_time < :max_time order by audience_id asc");
            query.setParameter("lower_bound_id", lowerBoundId);
            query.setMaxResults(limit);
            query.setParameter("platform_id", platformId);
            Date minTime = (start == null) ? new Date(0, 1, 1) : start;
            Date maxTime = (end == null) ? new Date(2100-1900, 1, 1) : end;
            query.setParameter("min_time", minTime);
            query.setParameter("max_time", maxTime);
            List<AudiencePayByMinutes> records = query.list();
            if (records.size() == 0) {
                break;
            }
            for (AudiencePayByMinutes record : records) {
                long audienceId = record.getAudienceId();
                long anchorId = record.getAnchorId();
                Date ts = record.getRecordEffectiveTime();
                int money = record.getMoney();
                Date aggregateDate = GeneralHelper.getAggregateDate(ts, timeUnit);
                addMetric(audienceId, anchorId, aggregateDate, money);
                if (maxAudineceId < audienceId) {
                    maxAudineceId = audienceId;
                }
            }
            if(isLastAudience()) {
                storeMetric();
                clearMetric();
                break;
            } else {
                removeFromMetric(maxAudineceId);
                storeMetric();
                clearMetric();
                lowerBoundId = maxAudineceId - 1;
            }
        }
        System.out.println(String.format("process audience pay by days for platform %d done", platformId));
        return false;
    }
    
    private void addMetric(long audienceId, long anchorId, Date date, int money) {
        Map<Long, Map<Date, Integer>> audienceMetric = metrics.get(audienceId);
        if (audienceMetric == null) {
            metrics.put(audienceId, Maps.newHashMap());
            audienceMetric = metrics.get(audienceId);
        }

        Map<Date, Integer> anchorMetric = audienceMetric.get(anchorId);
        if (anchorMetric == null) {
            audienceMetric.put(anchorId, Maps.newHashMap());
            anchorMetric = audienceMetric.get(anchorId);
        }

        Integer oldMoney = anchorMetric.get(date);
        if(oldMoney == null) {
            oldMoney = 0;
        }
        
        int newMoney = oldMoney + money;
        anchorMetric.put(date, newMoney);
    }
    
    private boolean isLastAudience() {
        return metrics.keySet().size() == 1;
    }
    
    private void removeFromMetric(Long audienceId) {
        metrics.remove(audienceId);
    }
    
    private void storeMetric() {
        for(long audienceId : metrics.keySet()) {
            Map<Long, Map<Date, Integer>> audienceMetric = metrics.get(audienceId);
            for(long anchorId : audienceMetric.keySet()) {
                Map<Date, Integer> anchorMetric = audienceMetric.get(anchorId);
                for(Date ts : anchorMetric.keySet()) {
                    if(!resourceManager.getDatabaseCache().existInAudiencePayByDays(audienceId, anchorId, ts)) {
                        int money = anchorMetric.get(ts);
                        AudiencePayByDays byDays = new AudiencePayByDays(audienceId, anchorId, platformId, money, ts);
                        resourceManager.getDatabaseSession().save(byDays);
                    }

                }
            }
        }
        resourceManager.commit();
    }
    
    private void clearMetric() {
        
    }

}
