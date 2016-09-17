package com.zhubo.task.processdata;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;

import com.google.common.collect.Maps;
import com.zhubo.entity.AnchorIncomeByMinutes;
import com.zhubo.entity.AnchorMetricByDays;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;

public class ProcessQixiuAnchorIncomeByMinutesTask extends BaseProcessDataTask {
    
    private Map<Long, Map<Date, Map<Long, PlatformMoney>>> metrics; //[anchor_id][date][audience_id]
    private static final int limit = 500;

    public ProcessQixiuAnchorIncomeByMinutesTask(ResourceManager resourceManager) {
        super(resourceManager);
    }
    
    public static class PlatformMoney {
        public int platformId;
        public int money;
        public PlatformMoney(int platformId, int money) {
            this.platformId = platformId;
            this.money = money;                   
        }
    }

    @Override
    public boolean run() {
        metrics = Maps.newHashMap();
        long lowerBoundId = 0;
        long upperBoundId = lowerBoundId + limit;
        long maxAnchorId = 0;
        while (true) {
            Query query = resourceManager.getDatabaseSession()
                    .createQuery("from AudiencePayByMinutes where anchor_id > :lower_bound_id and anchor_id < :upper_bound_id");
            query.setParameter("lower_bound_id", lowerBoundId);
            query.setParameter("upper_bound_id", upperBoundId);
            List<AudiencePayByMinutes> records = query.list();
            if (records.size() == 0) {
                break;
            }
            for (AudiencePayByMinutes record : records) {
                long anchorId = record.getAnchorId();
                long audienceId = record.getAudienceId();
                int money = record.getMoney();
                int platformId = record.getPlatformId();
      
                Date ts = record.getRecordEffectiveTime();
                addMetric(anchorId, ts, audienceId, new PlatformMoney(platformId, money));
                if (maxAnchorId < anchorId) {
                    maxAnchorId = anchorId;
                }
            }
            storeMetric();
            clearMetric();
            lowerBoundId = maxAnchorId;
            upperBoundId = lowerBoundId + limit;
        }
        return true;
    }
    
    private void addMetric(long anchorId, Date date, long audienceId, PlatformMoney pm) {
        Map<Date, Map<Long, PlatformMoney>> anchorMetric = metrics.get(anchorId);
        if (anchorMetric == null) {
            metrics.put(anchorId, Maps.newHashMap());
            anchorMetric = metrics.get(anchorId);
        }

        Map<Long, PlatformMoney> dateMetric = anchorMetric.get(date);
        if (dateMetric == null) {
            anchorMetric.put(date, Maps.newHashMap());
            dateMetric = anchorMetric.get(date);
        }

        PlatformMoney oldPm = dateMetric.get(audienceId);
        if (oldPm == null) {
            dateMetric.put(audienceId, new PlatformMoney(pm.platformId, 0));
            oldPm = dateMetric.get(audienceId);
        }
        int newMoney = oldPm.money + pm.money;        
        dateMetric.put(audienceId, new PlatformMoney(pm.platformId, newMoney));
    }
    
    private void storeMetric() {
        for (Long anchorId : metrics.keySet()) {
            Map<Date, Map<Long, PlatformMoney>> anchorMetric = metrics.get(anchorId);
            for (Date date : anchorMetric.keySet()) {
                Map<Long, PlatformMoney> dateMetric = anchorMetric.get(date);
                int income = 0;
                int platformId = 0;
                for (PlatformMoney pm : dateMetric.values()) {
                     income += pm.money;
                     platformId = pm.platformId;
                }  
                AnchorIncomeByMinutes byMinutes = new AnchorIncomeByMinutes(anchorId, platformId, income, date);
                resourceManager.getDatabaseSession().save(byMinutes);
            }
        }
        resourceManager.commit();
    }
    
    private void clearMetric() {
        metrics.clear();
    }

}
