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
import com.zhubo.api.response.AudiencePayDetailResponse.AudiencePayItem;
import com.zhubo.entity.Anchor;
import com.zhubo.entity.AudiencePayByDays;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;

@RestController
public class AudienceMetricController {
    private final int maxTopAudience = 7;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    
    @RequestMapping("/audience_total_pay_minute")
    public AudienceTotalPayResponse getAudienceTotalPayMinute(@RequestParam(value = "audience_id") Long audienceId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AudiencePayByMinutes where audience_id = :audience_id "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("audience_id", audienceId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AudiencePayByMinutes> metrics = query.list();
        List<MetricItem> items = Lists.newArrayList();
        for (AudiencePayByMinutes metric : metrics) {
            items.add(new MetricItem(metric.getMoney(), metric.getRecordEffectiveTime()));
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new AudienceTotalPayResponse(audienceId, items);
    }
    
    @RequestMapping("/audience_total_pay_day")
    public AudienceTotalPayResponse getAudienceTotalPayDay(@RequestParam(value = "audience_id") Long audienceId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AudiencePayByDays where audience_id = :audience_id "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("audience_id", audienceId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AudiencePayByDays> metrics = query.list();
        List<MetricItem> items = Lists.newArrayList();
        for (AudiencePayByDays metric : metrics) {
            items.add(new MetricItem(metric.getMoney(), metric.getRecordEffectiveTime()));
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new AudienceTotalPayResponse(audienceId, items);
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
            
            List<AudiencePayByDays> latestPayByDays = getLatestPayByDays(session, audienceId, anchorId);
            int latest7DaysSumPay = getLatestXDaysTotalPay(latestPayByDays, 7);
            int latest30DaysSumPay = getLatestXDaysTotalPay(latestPayByDays, 30);
            
            payItems.add(new AudiencePayItem(anchorId, anchorAliasId, anchorName, income,
                    rateInCurAudience, incomeHistory, latest7DaysSumPay, latest30DaysSumPay));
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
            
            List<AudiencePayByDays> latestPayByDays = getLatestPayByDays(session, audienceId, anchorId);
            int latest7DaysSumPay = getLatestXDaysTotalPay(latestPayByDays, 7);
            int latest30DaysSumPay = getLatestXDaysTotalPay(latestPayByDays, 30);
            
            payItems.add(new AudiencePayItem(anchorId, anchorAliasId, anchorName, income,
                    rateInCurAudience, incomeHistory, latest7DaysSumPay, latest30DaysSumPay));
            count++;
        }         
        return new AudiencePayDetailResponse(payItems);
    }
    
    private List<AudiencePayByDays> getLatestPayByDays(Session session, Long audienceId, Long anchorId) {
        Query latestPayQuery = session.createQuery("from AudiencePayByDays where audience_id = :audience_id and anchor_id = :anchor_id and record_effective_time > :min_latest_ts");
        Date minLatestTs = GeneralHelper.addDay(new Date(), -31);
        latestPayQuery.setParameter("min_latest_ts", minLatestTs);
        latestPayQuery.setParameter("audience_id", audienceId);
        latestPayQuery.setParameter("anchor_id", anchorId);
        return latestPayQuery.list();
    }
    
    private int getLatestXDaysTotalPay(List<AudiencePayByDays> records, int days) {
       int fixDays = days + 1;
       Date minTs = GeneralHelper.addDay(new Date(), -fixDays);
       int sum = 0;
       for(AudiencePayByDays record : records) {
           if(record.getRecordEffectiveTime().compareTo(minTs) > 0) {
               sum += record.getMoney();
           }
       }
       return sum;
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
