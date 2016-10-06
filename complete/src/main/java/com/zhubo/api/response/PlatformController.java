package com.zhubo.api.response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zhubo.entity.AnchorMetricByDays;
import com.zhubo.entity.AudienceTotalPayByDays;
import com.zhubo.global.ResourceManager;

@RestController
public class PlatformController {
    
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    
    @RequestMapping("/platform_active_anchor_day")
    public PlatformMetricResponse getPlatformActiveAnchorsByDays(@RequestParam(value = "platform_id") Integer platformId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AnchorMetricByDays where platform_id = :platform_id "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("platform_id", platformId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AnchorMetricByDays> byDays = query.list();
        Map<Date, Set<Long>> anchorIdsByDays = Maps.newHashMap();
        for(AnchorMetricByDays byDay : byDays) {
            Date ts = byDay.getRecordEffectiveTime();
            Long anchorId = byDay.getAnchorId();
            Set<Long> anchorIds = anchorIdsByDays.get(ts);
            if(anchorIds == null) {
                anchorIdsByDays.put(ts, Sets.newHashSet());
                anchorIds = anchorIdsByDays.get(ts);
            }
            anchorIds.add(anchorId);
        }
        List<MetricItem> metricItems = Lists.newArrayList();
        for(Date ts : anchorIdsByDays.keySet()) {
            Set<Long> anchorIds = anchorIdsByDays.get(ts);
            metricItems.add(new MetricItem(anchorIds.size(), ts));
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new PlatformMetricResponse(metricItems);
    }
    
    @RequestMapping("/platform_income_day")
    public PlatformMetricResponse getPlatformIncomeByDays(@RequestParam(value = "platform_id") Integer platformId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AudienceTotalPayByDays where platform_id = :platform_id "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("platform_id", platformId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AudienceTotalPayByDays> byDays = query.list();
        Map<Date, Integer> incomeByDays = Maps.newHashMap();
        for(AudienceTotalPayByDays byDay : byDays) {
            Date ts = byDay.getRecordEffectiveTime();
            int money = byDay.getMoney();
            Integer oldIncome = incomeByDays.get(ts);
            if(oldIncome == null) {
                oldIncome = 0;
            }
            Integer newIncome = oldIncome + byDay.getMoney();
            incomeByDays.put(ts, newIncome);
        }
        List<MetricItem> metricItems = Lists.newArrayList();
        for(Date ts : incomeByDays.keySet()) {
            Integer money = incomeByDays.get(ts);
            metricItems.add(new MetricItem(money, ts));
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new PlatformMetricResponse(metricItems);
    }
    
    @RequestMapping("/platform_paid_audience_day")
    public PlatformMetricResponse getPlatformPaidAudienceByDays(@RequestParam(value = "platform_id") Integer platformId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AudienceTotalPayByDays where platform_id = :platform_id "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("platform_id", platformId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AudienceTotalPayByDays> byDays = query.list();
        Map<Date, Set<Long>> audienceIdsByDay = Maps.newHashMap();
        for(AudienceTotalPayByDays byDay : byDays) {
            Date ts = byDay.getRecordEffectiveTime();
            Long audienceId = byDay.getAudienceId();
            Set<Long> audienceIds = audienceIdsByDay.get(ts);
            if(audienceIds == null) {
                audienceIdsByDay.put(ts, Sets.newHashSet());
                audienceIds = audienceIdsByDay.get(ts);
            }
            audienceIds.add(audienceId);
        }
        List<MetricItem> metricItems = Lists.newArrayList();
        for(Date ts : audienceIdsByDay.keySet()) {
            Set<Long> audienceIds = audienceIdsByDay.get(ts);
            metricItems.add(new MetricItem(audienceIds.size(), ts));
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new PlatformMetricResponse(metricItems);
    }
}
