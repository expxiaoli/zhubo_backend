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
import com.zhubo.global.ResourceManager;

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
            totalPays.add(new AudiencePay(audienceId, totalPaysByAudience.get(audienceId)));
        }
        AudiencePayComparator comparator = new AudiencePayComparator();
        Collections.sort(totalPays, comparator);

        int count = 0;
        List<AnchorIncomeItem> payItems = Lists.newArrayList();
        while (count < maxTopAudience && count < totalPays.size()) {
            long audienceId = ((AudiencePay) totalPays.get(count)).audienceId;
            int totalPay = totalPaysByAudience.get(audienceId);
            Date lastPayTime = lastPayTimeByAudience.get(audienceId);
            List<MetricItem> payHistory = payItemsByAudience.get(audienceId);
            double rate = (totalPay * 1.0) / totalPayForAllAudience;

            Query audienceQuery = session
                    .createQuery("from Audience where audience_id = :audience_id");
            audienceQuery.setParameter("audience_id", audienceId);
            List<Audience> audiences = audienceQuery.list();
            String audienceName = audiences.get(0).getAudienceName();
            Long audienceAliasId = audiences.get(0).getAudienceAliasId();
            payItems.add(new AnchorIncomeItem(audienceId, audienceAliasId, audienceName, totalPay, lastPayTime, rate,
                    payHistory));

            count++;
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new AnchorIncomeDetailResponse(payItems);
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
        Map<Long, Integer> totalPaysByAudience = Maps.newHashMap();
        Map<Long, Date> lastPayTimeByAudience = Maps.newHashMap();
        Map<Long, List<MetricItem>> payItemsByAudience = Maps.newHashMap();
        int totalPayForAllAudience = 0;
        for (AudiencePayByDays record : records) {
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
            totalPays.add(new AudiencePay(audienceId, totalPaysByAudience.get(audienceId)));
        }
        AudiencePayComparator comparator = new AudiencePayComparator();
        Collections.sort(totalPays, comparator);

        int count = 0;
        List<AnchorIncomeItem> payItems = Lists.newArrayList();
        while (count < maxTopAudience && count < totalPays.size()) {
            long audienceId = ((AudiencePay) totalPays.get(count)).audienceId;
            int totalPay = totalPaysByAudience.get(audienceId);
            Date lastPayTime = lastPayTimeByAudience.get(audienceId);
            List<MetricItem> payHistory = payItemsByAudience.get(audienceId);
            double rate = (totalPay * 1.0) / totalPayForAllAudience;

            Query audienceQuery = session
                    .createQuery("from Audience where audience_id = :audience_id");
            audienceQuery.setParameter("audience_id", audienceId);
            List<Audience> audiences = audienceQuery.list();
            String audienceName = audiences.get(0).getAudienceName();
            Long audienceAliasId = audiences.get(0).getAudienceAliasId();
            payItems.add(new AnchorIncomeItem(audienceId, audienceAliasId, audienceName, totalPay, lastPayTime, rate,
                    payHistory));

            count++;
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new AnchorIncomeDetailResponse(payItems);
    }
    
    @RequestMapping("/audience_pay_detail_minute")
    public AudiencePayDetailResponse getAudiencePayDetailMinute(
            @RequestParam(value = "audience_id") Long audienceId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session.createQuery("from AudiencePayByMinutes where audience_id = :audience_id "
                + "and record_effective_time >= :start_date and record_effective_time < :end_date order by record_effective_time asc");
        query.setParameter("audience_id", audienceId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AudiencePayByMinutes> records = query.list();
        Map<Long, Integer> totalIncomeByAnchor = Maps.newHashMap();
        int totalMoney = 0;
        Map<Long, List<MetricItem>> incomeItemsByAnchor = Maps.newHashMap();
        for(AudiencePayByMinutes record : records) {
            long anchorId = record.getAnchorId();
            int money = record.getMoney();
            Date ts = record.getRecordEffectiveTime();
            Integer oldMoney = totalIncomeByAnchor.get(anchorId);
            if(oldMoney == null) {
                oldMoney = 0;
                incomeItemsByAnchor.put(anchorId, Lists.newArrayList());
            }
            int newMoney = oldMoney + money;
            totalMoney += money;
            totalIncomeByAnchor.put(anchorId, newMoney);
            incomeItemsByAnchor.get(anchorId).add(new MetricItem(money, ts));
        }
        List totalIncomes = new ArrayList();
        for(Long anchorId : totalIncomeByAnchor.keySet()) {
            totalIncomes.add(new AnchorIncome(anchorId, totalIncomeByAnchor.get(anchorId)));
        }
        AnchorIncomeComparator comparator = new AnchorIncomeComparator();
        Collections.sort(totalIncomes, comparator);
        int count = 0;
        List<AudiencePayItem> payItems = Lists.newArrayList();
        
        while(count < maxTopAudience && count < totalIncomes.size()) {
            AnchorIncome anchorIncome = (AnchorIncome) totalIncomes.get(count);
            long anchorId = anchorIncome.anchorId;
            int income = anchorIncome.money;

            Query anchorQuery = session
                    .createQuery("from Anchor where anchor_id = :anchor_id");
            anchorQuery.setParameter("anchor_id", anchorId);
            List<Anchor> anchors = anchorQuery.list();
            Long anchorAliasId = anchors.get(0).getAnchorAliasId();
            String anchorName = anchors.get(0).getAnchorName();
            double rateInCurAudience = income * 1.0 / totalMoney;
            List<MetricItem> incomeHistory = incomeItemsByAnchor.get(anchorId);
            
            payItems.add(new AudiencePayItem(anchorId, anchorAliasId, anchorName, income,
                    rateInCurAudience, incomeHistory));
            count++;
        }         
        return new AudiencePayDetailResponse(payItems);
    }
    
    @RequestMapping("/audience_pay_detail_day")
    public AudiencePayDetailResponse getAudiencePayDetailDay(
            @RequestParam(value = "audience_id") Long audienceId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session.createQuery("from AudiencePayByDays where audience_id = :audience_id "
                + "and record_effective_time >= :start_date and record_effective_time < :end_date order by record_effective_time asc");
        query.setParameter("audience_id", audienceId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AudiencePayByDays> records = query.list();
        Map<Long, Integer> totalIncomeByAnchor = Maps.newHashMap();
        int totalMoney = 0;
        Map<Long, List<MetricItem>> incomeItemsByAnchor = Maps.newHashMap();
        for(AudiencePayByDays record : records) {
            long anchorId = record.getAnchorId();
            int money = record.getMoney();
            Date ts = record.getRecordEffectiveTime();
            Integer oldMoney = totalIncomeByAnchor.get(anchorId);
            if(oldMoney == null) {
                oldMoney = 0;
                incomeItemsByAnchor.put(anchorId, Lists.newArrayList());
            }
            int newMoney = oldMoney + money;
            totalMoney += money;
            totalIncomeByAnchor.put(anchorId, newMoney);
            incomeItemsByAnchor.get(anchorId).add(new MetricItem(money, ts));
        }
        List totalIncomes = new ArrayList();
        for(Long anchorId : totalIncomeByAnchor.keySet()) {
            totalIncomes.add(new AnchorIncome(anchorId, totalIncomeByAnchor.get(anchorId)));
        }
        AnchorIncomeComparator comparator = new AnchorIncomeComparator();
        Collections.sort(totalIncomes, comparator);
        int count = 0;
        List<AudiencePayItem> payItems = Lists.newArrayList();
        
        while(count < maxTopAudience && count < totalIncomes.size()) {
            AnchorIncome anchorIncome = (AnchorIncome) totalIncomes.get(count);
            long anchorId = anchorIncome.anchorId;
            int income = anchorIncome.money;

            Query anchorQuery = session
                    .createQuery("from Anchor where anchor_id = :anchor_id");
            anchorQuery.setParameter("anchor_id", anchorId);
            List<Anchor> anchors = anchorQuery.list();
            Long anchorAliasId = anchors.get(0).getAnchorAliasId();
            String anchorName = anchors.get(0).getAnchorName();
            double rateInCurAudience = income * 1.0 / totalMoney;
            List<MetricItem> incomeHistory = incomeItemsByAnchor.get(anchorId);
            
            payItems.add(new AudiencePayItem(anchorId, anchorAliasId, anchorName, income,
                    rateInCurAudience, incomeHistory));
            count++;
        }         
        return new AudiencePayDetailResponse(payItems);
    }

    public static class AudiencePay {
        public long audienceId;
        public int money;

        public AudiencePay(long audienceId, int pay) {
            this.audienceId = audienceId;
            this.money = pay;
        }
    }
    
    public class AudiencePayComparator implements Comparator {
        public final int compare(Object o1, Object o2) {
            AudiencePay p1 = (AudiencePay) o1;
            AudiencePay p2 = (AudiencePay) o2;
            return p2.money - p1.money;
        }
    }
    
    public static class AnchorIncome {
        public long anchorId;
        public int money;
        
        public AnchorIncome(long anchorId, int money) {
            this.anchorId = anchorId;
            this.money = money;
        }
    }
    
    public class AnchorIncomeComparator implements Comparator {
        public final int compare(Object o1, Object o2) {
            AnchorIncome i1 = (AnchorIncome)o1;
            AnchorIncome i2 = (AnchorIncome)o2;
            return i2.money - i1.money;
        }
    }
    


}