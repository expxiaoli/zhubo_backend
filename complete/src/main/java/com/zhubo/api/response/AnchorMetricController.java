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
import com.zhubo.api.response.AnchorIncomeDetailResponse.AudiencePayItem;
import com.zhubo.entity.AnchorIncomeByMinutes;
import com.zhubo.entity.AnchorMetricByDays;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.entity.Audience;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.global.ResourceManager;

@RestController
public class AnchorMetricController {

    private final Session session = ResourceManager.generateResourceManager().getDatabaseSession();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private final int maxTopAudience = 7;

    @RequestMapping("/anchor_metric_minute")
    public AnchorMetricResponse getMetricMinute(@RequestParam(value = "anchor_id") Long anchorId,
            @RequestParam(value = "type") String type, @RequestParam(value = "start") String start,
            @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
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
        return new AnchorMetricResponse(anchorId, type, items);
    }

    @RequestMapping("/anchor_metric_day")
    public AnchorMetricResponse getMetricDay(@RequestParam(value = "anchor_id") Long anchorId,
            @RequestParam(value = "type") String type, @RequestParam(value = "start") String start,
            @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
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
        return new AnchorMetricResponse(anchorId, type, items);
    }

    @RequestMapping("/anchor_income_minute")
    public AnchorIncomeResponse getIncomeMinute(@RequestParam(value = "anchor_id") Long anchorId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end)
            throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
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
        return new AnchorIncomeResponse(anchorId, items);
    }

    @RequestMapping("/anchor_income_detail_minute")
    public AnchorIncomeDetailResponse getIncomeDetailMinute(
            @RequestParam(value = "anchor_id") Long anchorId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Query query = session.createQuery("from AudiencePayByMinutes where anchor_id = :anchor_id "
                + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AudiencePayByMinutes> records = query.list();
        Map<Long, Integer> totalPaysByAudience = Maps.newHashMap();
        Map<Long, Date> lastPayTimeByAudience = Maps.newHashMap();
        Map<Long, List<MetricItem>> payItemsByAudience = Maps.newHashMap();
        int totalPayForAllAudience = 0;
        for (AudiencePayByMinutes record : records) {
            Long audienceId = record.getAudienceId();
            Integer money = record.getMoney();
            Integer oldMoney = totalPaysByAudience.get(audienceId);
            if (oldMoney == null) {
                oldMoney = 0;
            }
            Integer newMoney = oldMoney + money;
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
            totalPays.add(new AudienceTotalPay(audienceId, totalPaysByAudience.get(audienceId)));
        }
        AudienceTotalPayComparator comparator = new AudienceTotalPayComparator();
        Collections.sort(totalPays, comparator);

        int count = 0;
        List<AudiencePayItem> payItems = Lists.newArrayList();
        while (count < maxTopAudience) {
            long audienceId = ((AudienceTotalPay) totalPays.get(count)).audienceId;
            int totalPay = totalPaysByAudience.get(audienceId);
            Date lastPayTime = lastPayTimeByAudience.get(audienceId);
            List<MetricItem> payHistory = payItemsByAudience.get(audienceId);
            double rate = (totalPay * 1.0) / totalPayForAllAudience;

            Query audienceQuery = session
                    .createQuery("from Audience where audience_id = :audience_id");
            audienceQuery.setParameter("audience_id", audienceId);
            List<Audience> audiences = audienceQuery.list();
            String audienceName = audiences.get(0).getAudienceName();
            payItems.add(new AudiencePayItem(audienceId, audienceName, totalPay, lastPayTime, rate,
                    payHistory));

            count++;
        }

        return new AnchorIncomeDetailResponse(payItems);
    }

    public static class AudienceTotalPay {
        public long audienceId;
        public int money;

        public AudienceTotalPay(long audienceId, int pay) {
            this.audienceId = audienceId;
            this.money = pay;
        }
    }

    public class AudienceTotalPayComparator implements Comparator {
        public final int compare(Object o1, Object o2) {
            AudienceTotalPay p1 = (AudienceTotalPay) o1;
            AudienceTotalPay p2 = (AudienceTotalPay) o2;
            return p2.money - p1.money;
        }
    }

}