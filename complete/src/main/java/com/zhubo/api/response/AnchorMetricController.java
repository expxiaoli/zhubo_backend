package com.zhubo.api.response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhubo.api.response.AnchorIncomeDetailResponse.AnchorIncomeItem;
import com.zhubo.api.response.AudiencePayDetailResponse.AudiencePayItem;
import com.zhubo.entity.Anchor;
import com.zhubo.entity.AnchorIncomeByDays;
import com.zhubo.entity.AnchorIncomeByMinutes;
import com.zhubo.entity.AnchorMetricByDays;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.entity.Audience;
import com.zhubo.entity.AudiencePayByDays;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.entity.AudienceTotalPayByDays;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;

@RestController
public class AnchorMetricController {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private final int maxTopAudience = 7;

    @RequestMapping("/anchor_metric_minute")
    public AnchorMetricResponse getMetricMinute(@RequestParam(value = "anchor_id") Long anchorId,
            @RequestParam(value = "type") String type, @RequestParam(value = "start") String start,
            @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AnchorMetricByMinutes where anchor_id = :anchor_id and type = :type "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("type", type);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AnchorMetricByMinutes> metrics = query.list();
        List<MetricItem> items = Lists.newArrayList();
        for (AnchorMetricByMinutes metric : metrics) {
            items.add(new MetricItem(metric.getValue(), metric.getRecordEffectiveTime()));
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new AnchorMetricResponse(anchorId, type, items);
    }

    @RequestMapping("/anchor_metric_day")
    public AnchorMetricResponse getMetricDay(@RequestParam(value = "anchor_id") Long anchorId,
            @RequestParam(value = "type") String type, @RequestParam(value = "start") String start,
            @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AnchorMetricByDays where anchor_id = :anchor_id and type = :type "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("type", type);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AnchorMetricByDays> metrics = query.list();
        List<MetricItem> items = Lists.newArrayList();
        for (AnchorMetricByDays metric : metrics) {
            items.add(new MetricItem(metric.getValue(), metric.getRecordEffectiveTime()));
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new AnchorMetricResponse(anchorId, type, items);
    }

    @RequestMapping("/anchor_income_minute")
    public AnchorIncomeResponse getIncomeMinute(@RequestParam(value = "anchor_id") Long anchorId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end)
            throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AnchorIncomeByMinutes where anchor_id = :anchor_id "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AnchorIncomeByMinutes> metrics = query.list();
        List<MetricItem> items = Lists.newArrayList();
        for (AnchorIncomeByMinutes metric : metrics) {
            items.add(new MetricItem(metric.getMoney(), metric.getRecordEffectiveTime()));
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new AnchorIncomeResponse(anchorId, items);
    }
    
    @RequestMapping("/anchor_income_day")
    public AnchorIncomeResponse getIncomeDay(@RequestParam(value = "anchor_id") Long anchorId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end)
            throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AnchorIncomeByDays where anchor_id = :anchor_id "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AnchorIncomeByDays> metrics = query.list();
        List<MetricItem> items = Lists.newArrayList();
        for (AnchorIncomeByDays metric : metrics) {
            items.add(new MetricItem(metric.getMoney(), metric.getRecordEffectiveTime()));
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new AnchorIncomeResponse(anchorId, items);
    }

    @RequestMapping("/anchor_income_detail_minute")
    public AnchorIncomeDetailResponse getIncomeDetailMinute(
            @RequestParam(value = "anchor_id") Long anchorId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session.createQuery("from AudiencePayByMinutes where anchor_id = :anchor_id "
                + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AudiencePayByMinutes> records = query.list();
        Map<Long, Long> totalPaysByAudience = Maps.newHashMap();
        Map<Long, Date> lastPayTimeByAudience = Maps.newHashMap();
        Map<Long, List<MetricItem>> payItemsByAudience = Maps.newHashMap();
        Long totalPayForAllAudience = 0L;
        for (AudiencePayByMinutes record : records) {
            Long audienceId = record.getAudienceId();
            Integer money = record.getMoney();
            Long oldMoney = totalPaysByAudience.get(audienceId);
            if (oldMoney == null) {
                oldMoney = 0L;
            }
            Long newMoney = oldMoney + money;
            totalPaysByAudience.put(audienceId, newMoney);
            totalPayForAllAudience += money;

            Date ts = record.getRecordEffectiveTime();
            Date oldTs = lastPayTimeByAudience.get(audienceId);
            if (oldTs == null || ts.compareTo(oldTs) > 0) {
                lastPayTimeByAudience.put(audienceId, ts);
            }

            List<MetricItem> oldPayItems = payItemsByAudience.get(audienceId);
            if (oldPayItems == null) {
                payItemsByAudience.put(audienceId, Lists.newArrayList());
            }
            payItemsByAudience.get(audienceId).add(
                    new MetricItem(record.getMoney(), record.getRecordEffectiveTime()));
        }

        List totalPays = new ArrayList();
        for (Long audienceId : totalPaysByAudience.keySet()) {
            totalPays.add(new AudiencePay(audienceId, totalPaysByAudience.get(audienceId)));
        }
        AudiencePayComparator comparator = new AudiencePayComparator();
        Collections.sort(totalPays, comparator);

        int count = 0;
        List<AnchorIncomeItem> payItems = Lists.newArrayList();
        while (count < maxTopAudience && count < totalPays.size()) {
            long audienceId = ((AudiencePay) totalPays.get(count)).audienceId;
            Long totalPay = totalPaysByAudience.get(audienceId);
            Date lastPayTime = lastPayTimeByAudience.get(audienceId);
            List<MetricItem> payHistory = payItemsByAudience.get(audienceId);
            double rate = (totalPay * 1.0) / totalPayForAllAudience;

            Query audienceQuery = session
                    .createQuery("from Audience where audience_id = :audience_id");
            audienceQuery.setParameter("audience_id", audienceId);
            List<Audience> audiences = audienceQuery.list();
            String audienceName = audiences.get(0).getAudienceName();
            Long audienceAliasId = audiences.get(0).getAudienceAliasId();

            List<AudiencePayByDays> latestPayByDays = getLatestPayByDays(session, audienceId, anchorId);
            Long latest7DaysSumPay = getLatestXDaysTotalPay(latestPayByDays, 7);
            Long latest30DaysSumPay = getLatestXDaysTotalPay(latestPayByDays, 30);
            
            payItems.add(new AnchorIncomeItem(audienceId, audienceAliasId, audienceName, totalPay, lastPayTime, rate,
                    payHistory, latest7DaysSumPay, latest30DaysSumPay));

            count++;
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new AnchorIncomeDetailResponse(payItems);
    }
    
    private List<AudiencePayByDays> getLatestPayByDays(Session session, Long audienceId, Long anchorId) {
        Query latestPayQuery = session.createQuery("from AudiencePayByDays where audience_id = :audience_id and anchor_id = :anchor_id and record_effective_time > :min_latest_ts");
        Date minLatestTs = GeneralHelper.addDay(new Date(), -31);
        latestPayQuery.setParameter("min_latest_ts", minLatestTs);
        latestPayQuery.setParameter("audience_id", audienceId);
        latestPayQuery.setParameter("anchor_id", anchorId);
        return latestPayQuery.list();
    }
    
    private Long getLatestXDaysTotalPay(List<AudiencePayByDays> records, int days) {
       int fixDays = days + 1;
       Date minTs = GeneralHelper.addDay(new Date(), -fixDays);
       Long sum = 0L;
       for(AudiencePayByDays record : records) {
           if(record.getRecordEffectiveTime().compareTo(minTs) > 0) {
               sum += record.getMoney();
           }
       }
       return sum;
    }
    
    @RequestMapping("/anchor_income_detail_day")
    public AnchorIncomeDetailResponse getIncomeDetailDay(
            @RequestParam(value = "anchor_id") Long anchorId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session.createQuery("from AudiencePayByDays where anchor_id = :anchor_id "
                + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AudiencePayByDays> records = query.list();
        Map<Long, Long> totalPaysByAudience = Maps.newHashMap();
        Map<Long, Date> lastPayTimeByAudience = Maps.newHashMap();
        Map<Long, List<MetricItem>> payItemsByAudience = Maps.newHashMap();
        Long totalPayForAllAudience = 0L;
        for (AudiencePayByDays record : records) {
            Long audienceId = record.getAudienceId();
            Integer money = record.getMoney();
            Long oldMoney = totalPaysByAudience.get(audienceId);
            if (oldMoney == null) {
                oldMoney = 0L;
            }
            Long newMoney = oldMoney + money;
            totalPaysByAudience.put(audienceId, newMoney);
            totalPayForAllAudience += money;

            Date ts = record.getRecordEffectiveTime();
            Date oldTs = lastPayTimeByAudience.get(audienceId);
            if (oldTs == null || ts.compareTo(oldTs) > 0) {
                lastPayTimeByAudience.put(audienceId, ts);
            }

            List<MetricItem> oldPayItems = payItemsByAudience.get(audienceId);
            if (oldPayItems == null) {
                payItemsByAudience.put(audienceId, Lists.newArrayList());
            }
            payItemsByAudience.get(audienceId).add(
                    new MetricItem(record.getMoney(), record.getRecordEffectiveTime()));
        }

        List totalPays = new ArrayList();
        for (Long audienceId : totalPaysByAudience.keySet()) {
            totalPays.add(new AudiencePay(audienceId, totalPaysByAudience.get(audienceId)));
        }
        AudiencePayComparator comparator = new AudiencePayComparator();
        Collections.sort(totalPays, comparator);

        int count = 0;
        List<AnchorIncomeItem> payItems = Lists.newArrayList();
        while (count < maxTopAudience && count < totalPays.size()) {
            long audienceId = ((AudiencePay) totalPays.get(count)).audienceId;
            Long totalPay = totalPaysByAudience.get(audienceId);
            Date lastPayTime = lastPayTimeByAudience.get(audienceId);
            List<MetricItem> payHistory = payItemsByAudience.get(audienceId);
            double rate = (totalPay * 1.0) / totalPayForAllAudience;

            Query audienceQuery = session
                    .createQuery("from Audience where audience_id = :audience_id");
            audienceQuery.setParameter("audience_id", audienceId);
            List<Audience> audiences = audienceQuery.list();
            String audienceName = audiences.get(0).getAudienceName();
            Long audienceAliasId = audiences.get(0).getAudienceAliasId();
            
            List<AudiencePayByDays> latestPayByDays = getLatestPayByDays(session, audienceId, anchorId);
            Long latest7DaysSumPay = getLatestXDaysTotalPay(latestPayByDays, 7);
            Long latest30DaysSumPay = getLatestXDaysTotalPay(latestPayByDays, 30);
            payItems.add(new AnchorIncomeItem(audienceId, audienceAliasId, audienceName, totalPay, lastPayTime, rate,
                    payHistory, latest7DaysSumPay, latest30DaysSumPay));

            count++;
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new AnchorIncomeDetailResponse(payItems);
    }

    public static class AudiencePay {
        public long audienceId;
        public Long money;

        public AudiencePay(long audienceId, Long pay) {
            this.audienceId = audienceId;
            this.money = pay;
        }
    }
    
    public class AudiencePayComparator implements Comparator {
        public final int compare(Object o1, Object o2) {
            AudiencePay p1 = (AudiencePay) o1;
            AudiencePay p2 = (AudiencePay) o2;
            return (p2.money - p1.money) > 0 ? 1 : -1;
        }
    }
}