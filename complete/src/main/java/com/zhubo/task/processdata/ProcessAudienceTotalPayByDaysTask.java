package com.zhubo.task.processdata;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;

import com.google.common.collect.Maps;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.entity.AudienceTotalPayByDays;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;

public class ProcessAudienceTotalPayByDaysTask extends BaseProcessDataTask {
    private static final int limit = 20000;
    private TimeUnit timeUnit = TimeUnit.DAY;

    private Map<Long, Map<Date, Integer>> metrics; // [audience_id][date]

    public ProcessAudienceTotalPayByDaysTask(ResourceManager resourceManager, int platformId) {
        super(resourceManager, platformId);
    }

    @Override
    public boolean run() {
        metrics = Maps.newHashMap();
        long lowerBoundId = 0;
        long maxAudineceId = 0;
        while (true) {
            System.out.println(String.format(
                    "begin to process audience total pay by days for platform %d, id after %d",
                    platformId, lowerBoundId));
            Query query = resourceManager
                    .getDatabaseSession()
                    .createQuery(
                            "from AudiencePayByMinutes where audience_id > :lower_bound_id"
                                    + " and platform_id = :platform_id and "
                                    + " record_effective_time >= :min_time and record_effective_time < :max_time order by audience_id asc");
            query.setParameter("lower_bound_id", lowerBoundId);
            query.setMaxResults(limit);
            query.setParameter("platform_id", platformId);
            Date minTime = (start == null) ? new Date(0, 1, 1) : start;
            Date maxTime = (end == null) ? new Date(2100 - 1900, 1, 1) : end;
            query.setParameter("min_time", minTime);
            query.setParameter("max_time", maxTime);
            List<AudiencePayByMinutes> records = query.list();
            if (records.size() == 0) {
                break;
            }
            for (AudiencePayByMinutes record : records) {
                long audienceId = record.getAudienceId();
                Date ts = record.getRecordEffectiveTime();
                int money = record.getMoney();
                Date aggregateDate = GeneralHelper.getAggregateDate(ts, timeUnit);
                addMetric(audienceId, aggregateDate, money);
                if (maxAudineceId < audienceId) {
                    maxAudineceId = audienceId;
                }
            }
            if (isLastAudience()) {
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
        System.out.println(String.format("process audience total pay by days for platform %d done",
                platformId));
        return true;
    }

    private void addMetric(long audienceId, Date date, int money) {
        Map<Date, Integer> audienceMetric = metrics.get(audienceId);
        if (audienceMetric == null) {
            metrics.put(audienceId, Maps.newHashMap());
            audienceMetric = metrics.get(audienceId);
        }

        Integer oldMoney = audienceMetric.get(date);
        if (oldMoney == null) {
            oldMoney = 0;
        }

        int newMoney = oldMoney + money;
        audienceMetric.put(date, newMoney);
    }

    private boolean isLastAudience() {
        return metrics.keySet().size() == 1;
    }

    private void removeFromMetric(Long audienceId) {
        metrics.remove(audienceId);
    }

    private void storeMetric() {
        for (long audienceId : metrics.keySet()) {
            Map<Date, Integer> audienceMetric = metrics.get(audienceId);
            for (Date ts : audienceMetric.keySet()) {
                if (!resourceManager.getDatabaseCache().existInAudienceTotalPayByDays(audienceId,
                        ts)) {
                    int money = audienceMetric.get(ts);
                    AudienceTotalPayByDays byDays = new AudienceTotalPayByDays(audienceId,
                            platformId, money, ts);
                    resourceManager.getDatabaseSession().save(byDays);
                }

            }
        }
        resourceManager.commit();
    }

    private void clearMetric() {
        metrics.clear();
    }

}
