package com.zhubo.api.response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.zhubo.api.response.AnchorIncomeRankResponse.RankItem;
import com.zhubo.api.response.AudienceTotalPayRankResponse.AudienceTotalPayRankItem;
import com.zhubo.entity.Anchor;
import com.zhubo.entity.AnchorIncomeByDays;
import com.zhubo.entity.Audience;
import com.zhubo.entity.AudienceTotalPayByDays;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.ModelHelper;

@RestController
public class RankController {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private final int rankNumber = 10;
    
    @RequestMapping("/audience_total_pay_day_rank")
    public AudienceTotalPayRankResponse getAudienceTotalPayDayRank(@RequestParam(value = "platform_id") int platformId,
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
        List<AudienceTotalPayByDays> records = query.list();
        Map<Long, Integer> payMapping = Maps.newHashMap();
        Integer paySum = 0;
        for(AudienceTotalPayByDays record : records) {
            Integer oldPay = payMapping.get(record.getAudienceId());
            if(oldPay == null) {
                oldPay = 0;
            }
            payMapping.put(record.getAudienceId(), oldPay + record.getMoney());
            paySum += record.getMoney();
        }
        List<AudienceTotalPayRankItem> rankItems = Lists.newArrayList();
        for(Long audienceId : payMapping.keySet()) {
            rankItems.add(new AudienceTotalPayRankItem(audienceId, null, null, null, payMapping.get(audienceId)));
        }
        Collections.sort(rankItems, new AudienceTotalPayComparator());
        List<AudienceTotalPayRankItem> finalRankItems = Lists.newArrayList();
        int index = 0;
        while(index < rankNumber && index < rankItems.size()) {
            AudienceTotalPayRankItem item =  rankItems.get(index);
            item.setRank(index + 1);
            Audience audience = ModelHelper.getAudience(ResourceManager.generateResourceManager(), item.getId());
            item.setAliasID(audience.getAudienceAliasId());
            item.setName(audience.getAudienceName());
            item.setRate(1.0 * item.getValue() / paySum);
            
            List<AudienceTotalPayByDays> byDays = getAudienceTotalPaysByDays(audience.getAudienceId(), startDate, endDate);
            List<MetricItem> payHistory = getPayHistory(byDays);
            Date latestPayDate = getLatestPayDate(byDays);
            item.setPayHistory(payHistory);
            item.setLatestPayDate(latestPayDate);
            
            finalRankItems.add(item);
            index++;
        }
        return new AudienceTotalPayRankResponse(finalRankItems);
    }
    
    public List<MetricItem> getPayHistory(List<AudienceTotalPayByDays> records) {
        List<MetricItem> items = Lists.newArrayList();
        for(AudienceTotalPayByDays record : records) {
            items.add(new MetricItem(record.getMoney(), record.getRecordEffectiveTime()));
        }
        return items;
    }
    
    public Date getLatestPayDate(List<AudienceTotalPayByDays> records) {
        Date date = null;
        for(AudienceTotalPayByDays record : records) {
            if(date == null || date.compareTo(record.getRecordEffectiveTime()) < 0) {
                date = record.getRecordEffectiveTime();
            }
        }
        return date;
    }
    
    private List<AudienceTotalPayByDays> getAudienceTotalPaysByDays(Long audienceId, Date start, Date end) {
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AudienceTotalPayByDays where audience_id = :audience_id "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date order by record_effective_time asc");
        query.setParameter("audience_id", audienceId);
        query.setParameter("start_date", start);
        query.setParameter("end_date", end);
        return query.list();
    }
    
    @RequestMapping("/anchor_income_day_rank")
    public AnchorIncomeRankResponse getAnchorIncomeDayRank(@RequestParam(value = "platform_id") int platformId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AnchorIncomeByDays where platform_id = :platform_id "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("platform_id", platformId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AnchorIncomeByDays> records = query.list();
        Map<Long, Integer> incomeMapping = Maps.newHashMap();
        for(AnchorIncomeByDays record : records) {
            Integer oldIncome = incomeMapping.get(record.getAnchorId());
            if(oldIncome == null) {
                incomeMapping.put(record.getAnchorId(), 0);
                oldIncome = incomeMapping.put(record.getAnchorId(), 0);
            }            
            incomeMapping.put(record.getAnchorId(), oldIncome + record.getMoney());
        }
        
        List<RankItem> rankItems = Lists.newArrayList();
        for(Long anchorId : incomeMapping.keySet()) {
            rankItems.add(new RankItem(anchorId, null, null, null, incomeMapping.get(anchorId)));
        }
        Collections.sort(rankItems, new RankComparator());
        List<RankItem> finalRankItems = Lists.newArrayList();
        int index = 0;
        while(index < rankNumber && index < rankItems.size()) {
            RankItem item = rankItems.get(index);
            item.setRank(index + 1);
            Anchor anchor = ModelHelper.getAnchor(ResourceManager.generateResourceManager(), item.getId());           
            if(anchor != null) {
                item.setName(anchor.getAnchorName());
                item.setAliasID(anchor.getAnchorAliasId());
            }
            finalRankItems.add(item);
            index++;
        }
        return new AnchorIncomeRankResponse(finalRankItems);
    }
    
    private class RankComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            RankItem i1 = (RankItem) o1;
            RankItem i2 = (RankItem) o2;
            return i2.getValue() - i1.getValue();
        }        
    }
    
    private class AudienceTotalPayComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            AudienceTotalPayRankItem i1 = (AudienceTotalPayRankItem) o1;
            AudienceTotalPayRankItem i2 = (AudienceTotalPayRankItem) o2;
            return i2.getValue() - i1.getValue();
        }
    }
}
