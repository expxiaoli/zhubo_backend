package com.zhubo.global;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zhubo.entity.Anchor;
import com.zhubo.entity.AnchorIncomeByDays;
import com.zhubo.entity.AnchorIncomeByMinutes;
import com.zhubo.entity.AnchorMetricByDays;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.entity.AnchorRoundIncomeByMinutes;
import com.zhubo.entity.Audience;
import com.zhubo.entity.AudiencePayByDays;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.entity.AudiencePayPeriod;
import com.zhubo.entity.AudienceTotalPayByDays;
import com.zhubo.helper.ModelHelper;

public class DatabaseCache {    
    private ResourceManager rm;
    private Date minTs;
    private Date maxTs;
    
    private Map<Long, AnchorObject> anchorMapper;
    private Map<Long, Long> audienceAliasIdToIdMapper;
    private Map<String, Long> audienceNameToIdMapper;
    private Map<Long, Map<Long, Set<Date>>> payByMinutesDatesMapper;
    private Map<Long, Map<String, Set<Date>>> metricByMinutesDatesMapper;
    private Map<Long, Map<Long, PayPeriodObject>> latestPayPeriodMapper;
    private Map<Long, Map<Long, Set<Date>>> payPeriodDatesMapper;
    private Map<Long, Set<Date>> anchorIncomeByMinutesMapper;
    private Map<Long, Set<Date>> roundIncomeDatesMapper;
    private Map<Long, Integer> latestRoundIncomeMapper;
    private Map<Long, Date> latestRoundStartMapper;
    
    private Map<Long, Map<String, Set<Date>>> metricByDaysDatesMapper;
    private Map<Long, Map<Long, Set<Date>>> audiencePayByDaysDatesMapper;
    private Map<Long, Set<Date>> anchorIncomeByDaysDatesMapper;
    private Map<Long, Set<Date>> audienceTotalPayByDaysDatesMapper;
    
    public static class PayPeriodObject {
        public int platformId;
        public long money;
        public Date periodStart;
        public Date recordEffectiveTime;
        public PayPeriodObject (int platformId, long money, Date periodStart, Date recordEffectiveTime) {
           this.platformId = platformId;
           this.money = money;
           this.periodStart = periodStart;
           this.recordEffectiveTime = recordEffectiveTime;
        }
    }
    
    public static class AnchorObject {
        public long anchorId;
        public String area;
        public String type;        
        
        public AnchorObject(long anchorId, String area, String type) {
            this.anchorId = anchorId;
            this.area = area;
            this.type = type;
        }
    }
    
    public DatabaseCache(ResourceManager rm, Date minTs, Date maxTs) {
        this.rm = rm;
        this.minTs = minTs;
        this.maxTs = maxTs;
    }
    
    public void batchLoadParsePageData(int platformId) {
        batchLoadAudienceData(platformId);
        batchLoadAnchorData(platformId);
        batchLoadPayByMinutes(platformId);
        batchLoadMetricByMinutes(platformId);
        batchLoadLatestPayPeriod(platformId);
        batchLoadPayPeriodDates(platformId);
        batchLoadAnchorIncomeByMinutes(platformId);
        batchLoadRoundIncomeDates(platformId);
        batchLoadLatestRoundIncome(platformId);
    }
    
    public void batchLoadProcessData(int platformId) {
        batchLoadMetricByDays(platformId);
        batchLoadAudiencePayByDays(platformId);
        batchLoadAnchorIncomeByDays(platformId);
        batchLoadAudienceTotalPayByDays(platformId);
    }
    
    public void clearParsePageData() {
        clearAudienceData();
        clearAnchorData();
        clearPayByMinutes();
        clearMetricByMinutes();
        clearLatestPayPeriod();
        clearPayPeriodDates();
        clearAnchorIncomeByMinutes();
        clearRoundIncomeDates();
        clearLatestRoundIncome();
    }
    
    public void clearProcessData() {
        clearMetricByDays();
        clearAudiencePayByDays();
        clearAnchorIncomeByDays();
        clearAudienceTotalPayByDays();
    }
    
    private void batchLoadAudienceData(int platformId) {
        audienceAliasIdToIdMapper = Maps.newHashMap();
        audienceNameToIdMapper = Maps.newHashMap();
        
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from Audience where platform_id = :platform_id");
        query.setParameter("platform_id", platformId);
        List<Audience> audiences = query.list();
        for(Audience audience : audiences) {
            Long audienceAliasId = audience.getAudienceAliasId();
            String audienceName = audience.getAudienceName();
            Long audienceId = audience.getAudienceId();
            if(audienceAliasId != null) {
                audienceAliasIdToIdMapper.put(audienceAliasId, audienceId);                
            }
            if(audienceName != null) {
                audienceNameToIdMapper.put(audienceName, audienceId);
            }
        }
        System.out.println("batchLoadAudienceData done");
    }
    
    private void clearAudienceData() {
        audienceAliasIdToIdMapper.clear();
        audienceAliasIdToIdMapper = null;
        audienceNameToIdMapper.clear();
        audienceAliasIdToIdMapper = null;
        System.out.println("clearAudienceData done");
    }
    
    private void batchLoadAnchorData(int platformId) {
        anchorMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from Anchor where platform_id = :platform_id");
        query.setParameter("platform_id", platformId);
        List<Anchor> records = query.list();
        for(Anchor record : records) {
            AnchorObject obj = new AnchorObject(record.getAnchorId(), record.getArea(), record.getType());
            anchorMapper.put(record.getAnchorAliasId(), obj);
        }
        System.out.println("batchLoadAnchorData done");
    }
    
    private void clearAnchorData() {
        anchorMapper.clear();
        anchorMapper = null;
    }
    
    private void batchLoadPayByMinutes(int platformId) {
        payByMinutesDatesMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AudiencePayByMinutes where record_effective_time >= :min_ts and record_effective_time <= :max_ts and platform_id = :platform_id");
        query.setParameter("min_ts", minTs);
        query.setParameter("max_ts", maxTs);
        query.setParameter("platform_id", platformId);
        List<AudiencePayByMinutes> payByMinutes = query.list();
        for(AudiencePayByMinutes pay : payByMinutes) {
            Map<Long, Set<Date>> audiencePayMapper = payByMinutesDatesMapper.get(pay.getAudienceId());
            if(audiencePayMapper == null) {
                payByMinutesDatesMapper.put(pay.getAudienceId(), Maps.newHashMap());
                audiencePayMapper = payByMinutesDatesMapper.get(pay.getAudienceId());
            }
            Set<Date> dates = audiencePayMapper.get(pay.getAnchorId());
            if(dates == null) {
                audiencePayMapper.put(pay.getAnchorId(), Sets.newHashSet());
                dates = audiencePayMapper.get(pay.getAnchorId());
            }
            dates.add(pay.getRecordEffectiveTime());
        }
        System.out.println("batchLoadPayByMinutes done");
    }
    
    private void clearPayByMinutes() {
        payByMinutesDatesMapper.clear();
        payByMinutesDatesMapper = null;
    }
    
    private void batchLoadMetricByMinutes(int platformId) {
        metricByMinutesDatesMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AnchorMetricByMinutes where record_effective_time >= :min_ts and record_effective_time <= :max_ts"
                + " and platform_id = :platform_id");
        query.setParameter("min_ts", minTs);
        query.setParameter("max_ts", maxTs);
        query.setParameter("platform_id", platformId);
        List<AnchorMetricByMinutes> metricByMinutes = query.list();
        for(AnchorMetricByMinutes metric : metricByMinutes) {
            Map<String, Set<Date>> anchorMetricMapper = metricByMinutesDatesMapper.get(metric.getAnchorId());
            if(anchorMetricMapper == null) {
                metricByMinutesDatesMapper.put(metric.getAnchorId(), Maps.newHashMap());
                anchorMetricMapper = metricByMinutesDatesMapper.get(metric.getAnchorId());
            }
            Set<Date> dates = anchorMetricMapper.get(metric.getType());
            if(dates == null) {
                anchorMetricMapper.put(metric.getType(), Sets.newHashSet());
                dates = anchorMetricMapper.get(metric.getType());
            }
            dates.add(metric.getRecordEffectiveTime());
        }
        System.out.println("batchLoadMetricByMinutes done");
    }
    
    private void clearMetricByMinutes() {
        metricByMinutesDatesMapper.clear();
        metricByMinutesDatesMapper = null;   
    }
    
    private void batchLoadLatestPayPeriod(int platformId) {
        Session session = rm.getDatabaseSession();
        latestPayPeriodMapper = Maps.newHashMap();
        Query query = session.createQuery("from AudiencePayPeriod where platform_id = :platform_id and record_effective_time < :min_ts order by record_effective_time desc");
        query.setParameter("platform_id", platformId);
        query.setParameter("min_ts", minTs);
        List<AudiencePayPeriod> payRecords = query.list();
        for(AudiencePayPeriod payRecord : payRecords) {
            putPayPeriodInCacheIfNotExist(payRecord.getAudienceId(), payRecord.getAnchorId(), 
                    new PayPeriodObject(payRecord.getPlatformId(), payRecord.getMoney(), payRecord.getPeriodStart(), payRecord.getRecordEffectiveTime()));
        }
        System.out.println("batchLoadLatestPayPeriod done");
    }
    
    private void putPayPeriodInCacheIfNotExist(long audienceId, long anchorId, 
            PayPeriodObject payPeriod) {
        if(!latestPayPeriodMapper.containsKey(audienceId)) {
            latestPayPeriodMapper.put(audienceId, Maps.newHashMap());
        }
        Map<Long, PayPeriodObject> audiencePays = latestPayPeriodMapper.get(audienceId);
        if(!audiencePays.containsKey(anchorId)) {
            audiencePays.put(anchorId, payPeriod);
        }
    }
    
    private void clearLatestPayPeriod() {
        latestPayPeriodMapper.clear();
        latestPayPeriodMapper = null;
    }
    
    private void batchLoadPayPeriodDates(int platformId) {
        payPeriodDatesMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AudiencePayPeriod where platform_id = :platform_id and record_effective_time >= :min_ts and record_effective_time <= :max_ts");
        query.setParameter("min_ts", minTs);
        query.setParameter("max_ts", maxTs);
        query.setParameter("platform_id", platformId);
        List<AudiencePayPeriod> payPeriods = query.list();
        for(AudiencePayPeriod payPeriod : payPeriods) {
            Map<Long, Set<Date>> audiencePayPeriodMapper = payPeriodDatesMapper.get(payPeriod.getAudienceId());
            if(audiencePayPeriodMapper == null) {
                payPeriodDatesMapper.put(payPeriod.getAudienceId(), Maps.newHashMap());
                audiencePayPeriodMapper = payPeriodDatesMapper.get(payPeriod.getAudienceId());
            }
            Set<Date> dates = audiencePayPeriodMapper.get(payPeriod.getAnchorId());
            if(dates == null) {
                audiencePayPeriodMapper.put(payPeriod.getAnchorId(), Sets.newHashSet());
                dates = audiencePayPeriodMapper.get(payPeriod.getAnchorId());
            }
            dates.add(payPeriod.getRecordEffectiveTime());
        }
        System.out.println("batchLoadPayPeriodDates done");
    }
    
    private void clearPayPeriodDates() {
        payPeriodDatesMapper.clear();
        payPeriodDatesMapper = null;
    }
    
    private void batchLoadAnchorIncomeByMinutes(int platformId) {
        anchorIncomeByMinutesMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AnchorIncomeByMinutes where platform_id = :platform_id and record_effective_time >= :min_ts and record_effective_time <= :max_ts");
        query.setParameter("min_ts", minTs);
        query.setParameter("max_ts", maxTs);
        query.setParameter("platform_id", platformId);
        List<AnchorIncomeByMinutes> records = query.list();
        for(AnchorIncomeByMinutes record : records) {
            Set<Date> dates = anchorIncomeByMinutesMapper.get(record.getAnchorId());
            if(dates == null) {
                anchorIncomeByMinutesMapper.put(record.getAnchorId(), Sets.newHashSet());
                dates = anchorIncomeByMinutesMapper.get(record.getAnchorId());
            }
            if(!dates.contains(record.getRecordEffectiveTime())) {
                dates.add(record.getRecordEffectiveTime());
            }
        }
        System.out.println("batchLoadAnchorIncomeByMinutes done");
    }
    
    private void clearAnchorIncomeByMinutes() {
        anchorIncomeByMinutesMapper.clear();
        anchorIncomeByMinutesMapper = null;
    }
    
    public Integer getDiffMoneyAndUpdateLatestPayPeriodInCache(long audienceId, long anchorId, PayPeriodObject payPeriod) {
        PayPeriodObject oldPayPeriod = getPayPeriodFromCache(latestPayPeriodMapper, audienceId, anchorId);
        if(oldPayPeriod == null) {
            putPayPeriodInCache(latestPayPeriodMapper, audienceId, anchorId, payPeriod);
            return null;
        } else if (oldPayPeriod.recordEffectiveTime.compareTo(payPeriod.recordEffectiveTime) >= 0)  {
            System.out.println("-_-> is old pay data, ignore get diff money");
            System.out.println("audienceId:" + audienceId + " anchorId:" + anchorId + 
                    " old:" + oldPayPeriod.recordEffectiveTime.toString() + " new:" + payPeriod.recordEffectiveTime.toString());
            return 0;
        }
        else if (oldPayPeriod.periodStart.compareTo(payPeriod.periodStart) == 0) {
            putPayPeriodInCache(latestPayPeriodMapper, audienceId, anchorId, payPeriod);
            int diffMoney = Long.valueOf(payPeriod.money - oldPayPeriod.money).intValue();
            return diffMoney;
        } else if (oldPayPeriod.periodStart.compareTo(payPeriod.periodStart) < 0) {
            putPayPeriodInCache(latestPayPeriodMapper, audienceId, anchorId, payPeriod);
            return Long.valueOf(payPeriod.money).intValue();
        } else {
            System.out.println("-_-> is invalid pay period data");
            System.out.println("audienceId:" + audienceId + " anchorId:" + anchorId + 
                    " old:" + oldPayPeriod.recordEffectiveTime.toString() + " new:" + payPeriod.recordEffectiveTime.toString());
            
            return 0;
        }
    }
    
    public Integer getDiffMoneyAndUpdateLatestPayPeriodInCache(long audienceId, long anchorId, boolean isOldRound, Date latestRoundStart, PayPeriodObject payPeriod) {
        PayPeriodObject oldPayPeriod = getPayPeriodFromCache(latestPayPeriodMapper, audienceId, anchorId);
        if(oldPayPeriod == null) {
            putPayPeriodInCache(latestPayPeriodMapper, audienceId, anchorId, payPeriod);
            return null;
        } else if (oldPayPeriod.recordEffectiveTime.compareTo(payPeriod.recordEffectiveTime) >= 0)  {
            System.out.println("-_-> is old pay data, ignore get diff money");
            System.out.println("audienceId:" + audienceId + " anchorId:" + anchorId + 
                    " old:" + oldPayPeriod.recordEffectiveTime.toString() + " new:" + payPeriod.recordEffectiveTime.toString());
            return 0;
        } else if (isOldRound && oldPayPeriod.recordEffectiveTime.compareTo(latestRoundStart) >= 0) {
            if(payPeriod.money > oldPayPeriod.money) {
                putPayPeriodInCache(latestPayPeriodMapper, audienceId, anchorId, payPeriod);
                int diffMoney = Long.valueOf(payPeriod.money - oldPayPeriod.money).intValue();
                return diffMoney;
            } else {
                return 0;
            }
        } else {
            putPayPeriodInCache(latestPayPeriodMapper, audienceId, anchorId, payPeriod);
            return Long.valueOf(payPeriod.money).intValue();
        }
    }
    
    private void putPayPeriodInCache(Map<Long, Map<Long, PayPeriodObject>> payPeriodMapper, long audienceId, long anchorId, 
            PayPeriodObject payPeriod) {
        if(!payPeriodMapper.containsKey(audienceId)) {
            payPeriodMapper.put(audienceId, Maps.newHashMap());
        }
        Map<Long, PayPeriodObject> audiencePays = payPeriodMapper.get(audienceId);
        audiencePays.put(anchorId, payPeriod);
    }
    
    public PayPeriodObject getLatestPayPeriodFromCache(long audienceId, long anchorId) {
        return getPayPeriodFromCache(latestPayPeriodMapper, audienceId, anchorId);
    }
    
    private PayPeriodObject getPayPeriodFromCache(Map<Long, Map<Long, PayPeriodObject>> payCache, long audienceId, long anchorId) {
        if(payCache.containsKey(audienceId)) {
            Map<Long, PayPeriodObject> audiencePays = payCache.get(audienceId);
            if(audiencePays.containsKey(anchorId)) {
                return audiencePays.get(anchorId);
            } else {
                return null;
            }
        } else {
            return null;
        }
        
    } 
    
    public AnchorObject getAnchorObjectFromCache(Long anchorAliasId) {
        return anchorMapper.get(anchorAliasId);
    }
    
    public void setAnchorObjectInCache(Long anchorAliasId, AnchorObject obj) {
        anchorMapper.put(anchorAliasId, obj);
    }
    
    public boolean existInPayByMinutes(Long audienceId, Long anchorId, Date date) {
        Map<Long, Set<Date>> audiencePays = payByMinutesDatesMapper.get(audienceId);
        if(audiencePays == null) {
            return false;
        } else {
            Set<Date> dates = audiencePays.get(anchorId);
            if(dates == null) {
                return false;
            } else {
                return dates.contains(date);
            }
        }
    }
    
    
    public boolean existInPayPeriod(Long audienceId, Long anchorId, Date date) {
        Map<Long, Set<Date>> audiencePays = payPeriodDatesMapper.get(audienceId);
        if(audiencePays == null) {
            return false;
        } else {
            Set<Date> dates = audiencePays.get(anchorId);
            if(dates == null) {
                return false;
            } else {
                return dates.contains(date);
            }
        }
    }
    
    public boolean existInMetricByMinutes(Long anchorId, String type, Date date) {
        Map<String, Set<Date>> anchorMetrics = metricByMinutesDatesMapper.get(anchorId);
        if(anchorMetrics == null) {
            return false;
        } else {
            Set<Date> dates = anchorMetrics.get(type);
            if(dates == null) {
                return false;
            } else {
                return dates.contains(date);
            }
        }
    }
    
    public boolean existInAnchorIncomeByMinutes(Long anchorId, Date date) {
        Set<Date> dates = anchorIncomeByMinutesMapper.get(anchorId);
        if(dates == null) {
            return false;
        } else {
            return dates.contains(date);
        }
    }
    
    public Long getIdFromAudienceAliasIdOrAudienceName(int platformId, Long aliasId, String audienceName) {
        if(aliasId != null) {
            Long id = getIdFromAudienceAliasId(platformId, aliasId);
            if(id != null) {
                return id;
            }
        }
        return getIdFromAudienceName(platformId, audienceName);
    }
    
    public Long getIdFromAudienceAliasId(int platformId, long aliasId) {
        return audienceAliasIdToIdMapper.get(aliasId);
    }
    
    public Long getIdFromAudienceName(int platformId, String audienceName) {
        return audienceNameToIdMapper.get(audienceName);
    }
    
    public void setAudienceMapper(Long aliasId, String audienceName, Long audienceId) {
        if(aliasId != null) {
            audienceAliasIdToIdMapper.put(aliasId, audienceId);
        }
        if(audienceName != null) {
            audienceNameToIdMapper.put(audienceName, audienceId);
        }
    }
    
    private void batchLoadMetricByDays(int platformId) {
        metricByDaysDatesMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AnchorMetricByDays where platform_id = :platform_id and record_effective_time >= :min_ts and record_effective_time <= :max_ts");
        query.setParameter("min_ts", minTs);
        query.setParameter("max_ts", maxTs);
        query.setParameter("platform_id", platformId);
        List<AnchorMetricByDays> records = query.list();
        for(AnchorMetricByDays record : records) {
            Map<String, Set<Date>> anchorMetricMapper = metricByDaysDatesMapper.get(record.getAnchorId());
            if(anchorMetricMapper == null) {
                metricByDaysDatesMapper.put(record.getAnchorId(), Maps.newHashMap());
                anchorMetricMapper = metricByDaysDatesMapper.get(record.getAnchorId());
            }
            Set<Date> dates = anchorMetricMapper.get(record.getType());
            if(dates == null) {
                anchorMetricMapper.put(record.getType(), Sets.newHashSet());
                dates = anchorMetricMapper.get(record.getType());
            }
            dates.add(record.getRecordEffectiveTime());
        }
        System.out.println("batchLoadMetricByDays done");
    }
    
    private void clearMetricByDays() {
        metricByDaysDatesMapper.clear();
    }
    
    public boolean existInAnchorMetricByDays(Long anchorId, String type, Date ts) {
        Map<String, Set<Date>> anchorMetricMapper = metricByDaysDatesMapper.get(anchorId);
        if(anchorMetricMapper == null) {
            return false;
        }
        Set<Date> dates = anchorMetricMapper.get(type);
        if(dates == null) {
            return false;
        }
        return dates.contains(ts);
    }
    
    private void batchLoadAudiencePayByDays(int platformId) {
        audiencePayByDaysDatesMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AudiencePayByDays where platform_id = :platform_id and record_effective_time >= :min_ts and record_effective_time <= :max_ts");
        query.setParameter("min_ts", minTs);
        query.setParameter("max_ts", maxTs);
        query.setParameter("platform_id", platformId);
        List<AudiencePayByDays> records = query.list();
        for(AudiencePayByDays record : records) {
            Map<Long, Set<Date>> audienceMetricMapper = audiencePayByDaysDatesMapper.get(record.getAudienceId());
            if(audienceMetricMapper == null) {
                audiencePayByDaysDatesMapper.put(record.getAudienceId(), Maps.newHashMap());
                audienceMetricMapper = audiencePayByDaysDatesMapper.get(record.getAudienceId());
            }
            Set<Date> dates = audienceMetricMapper.get(record.getAnchorId());
            if(dates == null) {
                audienceMetricMapper.put(record.getAnchorId(), Sets.newHashSet());
                dates = audienceMetricMapper.get(record.getAnchorId());
            }
            dates.add(record.getRecordEffectiveTime());
        }
        System.out.println("batchLoadAudiencePayByDays done");
    }
    
    private void clearAudiencePayByDays() {
        audiencePayByDaysDatesMapper.clear();
    }
    
    public boolean existInAudiencePayByDays(Long audienceId, Long anchorId, Date ts) {
        Map<Long, Set<Date>> audiencePayMapper = audiencePayByDaysDatesMapper.get(audienceId);
        if(audiencePayMapper == null) {
            return false;
        }
        Set<Date> dates = audiencePayMapper.get(anchorId);
        return dates != null && dates.contains(ts);
    }
    
    private void batchLoadAnchorIncomeByDays(int platformId) {
        anchorIncomeByDaysDatesMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AnchorIncomeByDays where platform_id = :platform_id and record_effective_time >= :min_ts and record_effective_time <= :max_ts");
        query.setParameter("min_ts", minTs);
        query.setParameter("max_ts", maxTs);
        query.setParameter("platform_id", platformId);
        List<AnchorIncomeByDays> records = query.list();
        for(AnchorIncomeByDays record : records) {
            Set<Date> dates = anchorIncomeByDaysDatesMapper.get(record.getAnchorId());
            if(dates == null) {
                anchorIncomeByDaysDatesMapper.put(record.getAnchorId(), Sets.newHashSet());
                dates = anchorIncomeByDaysDatesMapper.get(record.getAnchorId());
            }
            dates.add(record.getRecordEffectiveTime());
        }
        System.out.println("batchLoadAnchorIncomeByDays done");
    }
    
    private void clearAnchorIncomeByDays() {
        anchorIncomeByDaysDatesMapper.clear();
    }
    
    public boolean existInAnchorIncomeByDays(long anchorId, Date ts) {
        Set<Date> dates = anchorIncomeByDaysDatesMapper.get(anchorId);
        return dates != null && dates.contains(ts);
    }
    
    //audienceTotalPayByDaysDatesMapper
    private void batchLoadAudienceTotalPayByDays(int platformId) {
        audienceTotalPayByDaysDatesMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AudienceTotalPayByDays where platform_id = :platform_id and record_effective_time >= :min_ts and record_effective_time <= :max_ts");
        query.setParameter("min_ts", minTs);
        query.setParameter("max_ts", maxTs);
        query.setParameter("platform_id", platformId);
        List<AudienceTotalPayByDays> records = query.list();
        for(AudienceTotalPayByDays record : records) {
            Set<Date> dates = audienceTotalPayByDaysDatesMapper.get(record.getAudienceId());
            if(dates == null) {
                audienceTotalPayByDaysDatesMapper.put(record.getAudienceId(), Sets.newHashSet());
                dates = audienceTotalPayByDaysDatesMapper.get(record.getAudienceId());
            }
            dates.add(record.getRecordEffectiveTime());
        }
        System.out.println("batchLoadAudienceTotalPayByDays done");
    }
    
    private void clearAudienceTotalPayByDays() {
        audienceTotalPayByDaysDatesMapper.clear();
    }
    
    public boolean existInAudienceTotalPayByDays(long audienceId, Date ts) {
        Set<Date> dates = audienceTotalPayByDaysDatesMapper.get(audienceId);
        return dates != null && dates.contains(ts);
    }
    
    private void batchLoadRoundIncomeDates(int platformId) {
        roundIncomeDatesMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AnchorRoundIncomeByMinutes where platform_id = :platform_id and record_effective_time >= :min_ts and record_effective_time <= :max_ts");
        query.setParameter("min_ts", minTs);
        query.setParameter("max_ts", maxTs);
        query.setParameter("platform_id", platformId);
        List<AnchorRoundIncomeByMinutes> records = query.list();
        for(AnchorRoundIncomeByMinutes record : records) {
            Long anchorId = record.getAnchorId();
            Set<Date> dates = roundIncomeDatesMapper.get(anchorId);
            if(dates == null) {
                roundIncomeDatesMapper.put(anchorId, Sets.newHashSet());
                dates = roundIncomeDatesMapper.get(anchorId);
            }
            Date date = record.getRecordEffectiveTime();
            dates.add(date);
        }
        System.out.println("batchLoadRoundIncomeDates done");
    }
    
    
    public boolean existInRoundIncomeDates(long anchorId, Date ts) {
        Set<Date> dates = roundIncomeDatesMapper.get(anchorId);
        if(dates == null) {
            return false;
        } else {
            return dates.contains(ts);
        }
    }
    
    public void setRoundIncomeDate(long anchorId, Date ts) {
        Set<Date> dates = roundIncomeDatesMapper.get(anchorId);
        if(dates == null) {
            roundIncomeDatesMapper.put(anchorId, Sets.newHashSet());
            dates = roundIncomeDatesMapper.get(anchorId);
        }
        dates.add(ts);
    }
    
    private void clearRoundIncomeDates() {
        roundIncomeDatesMapper.clear();
    }
    
    private void batchLoadLatestRoundIncome(int platformId) {
        latestRoundIncomeMapper = Maps.newHashMap();
        latestRoundStartMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AnchorRoundIncomeByMinutes where platform_id = :platform_id and record_effective_time < :min_ts order by record_effective_time asc");
        query.setParameter("min_ts", minTs);
        query.setParameter("platform_id", platformId);
        List<AnchorRoundIncomeByMinutes> records = query.list();
        for(AnchorRoundIncomeByMinutes record : records) {
            Integer oldRoundIncome = latestRoundIncomeMapper.get(record.getAnchorId());
            if(oldRoundIncome == null || oldRoundIncome > record.getMoney()) {
                latestRoundStartMapper.put(record.getAnchorId(), record.getRecordEffectiveTime());
            }
            latestRoundIncomeMapper.put(record.getAnchorId(), record.getMoney());
        }
        System.out.println("batchLoadLatestRoundIncome done");
    }
    
    public Integer getLatestRoundIncome(long anchorId) {
        return latestRoundIncomeMapper.get(anchorId);
    }
    
    public Date getLatestRoundStart(long anchorId) {
        return latestRoundStartMapper.get(anchorId);
    }
    
    public void setLatestRoundIncome(long anchorId, Integer money) {
        latestRoundIncomeMapper.put(anchorId, money);
    }
    
    public void setLatestRoundStart(long anchorId, Date ts) {
        latestRoundStartMapper.put(anchorId, ts);
    }
    
    private void clearLatestRoundIncome() {
        latestRoundIncomeMapper.clear();
        latestRoundStartMapper.clear();
    }
}
