package com.zhubo.task.processdata;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;

import com.google.common.collect.Maps;
import com.zhubo.entity.AnchorIncomeByDays;
import com.zhubo.entity.AnchorIncomeByMinutes;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;

public class ProcessAnchorIncomeByDaysTask extends BaseProcessDataTask {

    private static final int limit = 5000;
    private Map<Long, Map<Date, Integer>> metrics; // [anchor_id][date]
    private TimeUnit timeUnit = TimeUnit.DAY;

    public ProcessAnchorIncomeByDaysTask(ResourceManager resourceManager, int platformId) {
        super(resourceManager, platformId);
    }

    @Override
    public boolean run() {
        metrics = Maps.newHashMap();
        long lowerBoundId = 0;
        long maxAnchorId = 0;
        while (true) {
            System.out.println(String.format(
                    "begin to process income days for platform %d, id after %d", platformId,
                    lowerBoundId));
            Query query = resourceManager
                    .getDatabaseSession()
                    .createQuery(
                            "from AnchorIncomeByMinutes where anchor_id > :lower_bound_id"
                                    + " and platform_id = :platform_id and "
                                    + " record_effective_time > :min_time and record_effective_time < :max_time order by anchor_id asc");
            query.setParameter("lower_bound_id", lowerBoundId);
            query.setMaxResults(limit);
            query.setParameter("platform_id", platformId);
            Date minTime = (start == null) ? new Date(0, 1, 1) : start;
            Date maxTime = (end == null) ? new Date(2100 - 1900, 1, 1) : end;
            query.setParameter("min_time", minTime);
            query.setParameter("max_time", maxTime);
            List<AnchorIncomeByMinutes> records = query.list();
            if (records.size() == 0) {
                break;
            }
            for (AnchorIncomeByMinutes record : records) {
                long anchorId = record.getAnchorId();
                Date ts = record.getRecordEffectiveTime();
                int money = record.getMoney();
                Date aggregateDate = GeneralHelper.getAggregateDate(ts, timeUnit);
                addMetric(anchorId, aggregateDate, money);
                if (maxAnchorId < anchorId) {
                    maxAnchorId = anchorId;
                }
            }
            if (isLastAnchor()) {
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
        System.out.println(String.format("process income days for platform %d done", platformId));
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
            Map<Date, Integer> anchorMetric = metrics.get(anchorId);
            for (Date date : anchorMetric.keySet()) {
                if (!resourceManager.getDatabaseCache().existInAnchorIncomeByDays(anchorId, date)) {
                    int money = anchorMetric.get(date);
                    AnchorIncomeByDays byDays = new AnchorIncomeByDays(anchorId, platformId, money,
                            date);
                    resourceManager.getDatabaseSession().save(byDays);
                }
            }
        }
        resourceManager.commit();
    }

    private void clearMetric() {
        metrics.clear();
    }

    private void addMetric(long anchorId, Date date, int money) {
        Map<Date, Integer> anchorMetric = metrics.get(anchorId);
        if (anchorMetric == null) {
            metrics.put(anchorId, Maps.newHashMap());
            anchorMetric = metrics.get(anchorId);
        }

        Integer oldMoney = anchorMetric.get(date);
        if (oldMoney == null) {
            anchorMetric.put(date, 0);
            oldMoney = 0;
        }
        int newMoney = oldMoney + money;
        anchorMetric.put(date, newMoney);
    }
}
