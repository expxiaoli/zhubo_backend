package com.zhubo.global;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.entity.Audience;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.entity.AudiencePayPeriod;
import com.zhubo.helper.ModelHelper;

public class DatabaseCache {
    private static final int maxPlatformId = 1;
    
    private ResourceManager rm;
    private Date minTs;
    private Date maxTs;
    
    private Map<Integer, Map<Long, Long>> audienceAliasIdToIdMapper;
    private Map<Integer, Map<String, Long>> audienceNameToIdMapper;
    private Map<Long, Map<Long, Set<Date>>> payByMinutesDatesMapper;
    private Map<Long, Map<String, Set<Date>>> metricByMinutesDatesMapper;
    Map<Long, Map<Long, PayPeriodObject>> latestPayPeriodMapper;
    Map<Long, Map<Long, Set<Date>>> payPeriodDatesMapper;
    
    public static class PayPeriodObject {
        public int platformId;
        public int money;
        public Date periodStart;
        public Date recordEffectiveTime;
        public PayPeriodObject (int platformId, int money, Date periodStart, Date recordEffectiveTime) {
           this.platformId = platformId;
           this.money = money;
           this.periodStart = periodStart;
           this.recordEffectiveTime = recordEffectiveTime;
        }
    }
    
    public DatabaseCache(ResourceManager rm, Date minTs, Date maxTs) {
        this.rm = rm;
        this.minTs = minTs;
        this.maxTs = maxTs;
    }
    
    public void batchLoad() {
        batchLoadAudienceData();
        batchLoadPayByMinutes();
        batchLoadMetricByMinutes();
        batchLoadLatestPayPeriod(1);
        batchLoadPayPeriodDates(1);
    }
    
    public void batchSave() {
    }
    
    private void batchLoadAudienceData() {
        audienceAliasIdToIdMapper = Maps.newHashMap();
        audienceNameToIdMapper = Maps.newHashMap();
        for(int platformId = 1; platformId <= maxPlatformId; platformId++) {
            audienceAliasIdToIdMapper.put(platformId, Maps.newHashMap());
            audienceNameToIdMapper.put(platformId,  Maps.newHashMap());
        }
        
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from Audience");
        List<Audience> audiences = query.list();
        for(Audience audience : audiences) {
            int platformId = audience.getPlatformId();
            Long audienceAliasId = audience.getAudienceAliasId();
            String audienceName = audience.getAudienceName();
            Long audienceId = audience.getAudienceId();
            if(audienceAliasId != null) {
                audienceAliasIdToIdMapper.get(platformId).put(audienceAliasId, audienceId);                
            }
            if(audienceName != null) {
                audienceNameToIdMapper.get(platformId).put(audienceName, audienceId);
            }
        }
        System.out.println("batchLoadAudienceData done");
    }
    
    private void batchLoadPayByMinutes() {
        payByMinutesDatesMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AudiencePayByMinutes where record_effective_time >= :min_ts and record_effective_time <= :max_ts");
        query.setParameter("min_ts", minTs);
        query.setParameter("max_ts", maxTs);
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
    
    private void batchLoadMetricByMinutes() {
        metricByMinutesDatesMapper = Maps.newHashMap();
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AnchorMetricByMinutes where record_effective_time >= :min_ts and record_effective_time <= :max_ts");
        query.setParameter("min_ts", minTs);
        query.setParameter("max_ts", maxTs);
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
    
    public Integer getDiffMoneyAndUpdateLatestPayPeriodInCache(long audienceId, long anchorId, PayPeriodObject payPeriod) {
        PayPeriodObject oldPayPeriod = getPayPeriodFromCache(latestPayPeriodMapper, audienceId, anchorId);
        if(oldPayPeriod == null) {
            putPayPeriodInCache(latestPayPeriodMapper, audienceId, anchorId, payPeriod);
            return null;
        } else if (oldPayPeriod.money < payPeriod.money) {
            putPayPeriodInCache(latestPayPeriodMapper, audienceId, anchorId, payPeriod);
            int diffMoney = payPeriod.money - oldPayPeriod.money;
            return diffMoney;
        } else if (oldPayPeriod.money > payPeriod.money) {
            putPayPeriodInCache(latestPayPeriodMapper, audienceId, anchorId, payPeriod);
            return payPeriod.money;
        } else {
            return 0;
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
    
    private void batchSaveLatestPayPeriod(int platformId) {
        int audienceCount = 0;
        for(Long audienceId : latestPayPeriodMapper.keySet()) {
            Map<Long, PayPeriodObject> audienceCache = latestPayPeriodMapper.get(audienceId);
            for(Long anchorId : audienceCache.keySet()) {
                PayPeriodObject payPeriod = audienceCache.get(anchorId);
                AudiencePayPeriod oldRecord = getLatestPayPeriod(rm, audienceId, anchorId);
                if(oldRecord == null) {
                    AudiencePayPeriod record = new AudiencePayPeriod(audienceId, anchorId, payPeriod.platformId, payPeriod.money, 
                            payPeriod.recordEffectiveTime, payPeriod.periodStart);
                    rm.getDatabaseSession().save(record);                    
                } else {
                    oldRecord.setPlatformId(platformId);
                    oldRecord.setMoney(payPeriod.money);
                    oldRecord.setPeriodStart(payPeriod.periodStart);
                    oldRecord.setRecordEffectiveDate(payPeriod.recordEffectiveTime);
                    rm.getDatabaseSession().update(oldRecord); 
                }
            }
            audienceCount++;
            if(audienceCount % 100 == 0) {
                System.out.println(System.currentTimeMillis() + " save audience pay period:" + audienceCount);
            }
        }
        rm.commit();
    }
    
    private AudiencePayPeriod getLatestPayPeriod(ResourceManager rm, long audienceId, long anchorId) {
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AudiencePayPeriod where anchor_id = :anchor_id and audience_id = :audience_id order by record_effective_time desc");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("audience_id", audienceId);
        List<AudiencePayPeriod> pays = query.list();
        if(pays.isEmpty()) {
            return null;
        } else {
            return pays.get(0);
        }  
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
    
    public Long getIdFromAudienceAliasIdOrAudienceName(int platformId, Long aliasId, String audienceName) {
        if(aliasId != null) {
            Long id = getIdFromAudienceAliasId(platformId, aliasId);
            if(id != null) {
                return id;
            }
        }
        return getIdFromAudienceName(platformId, audienceName);
    }
    
    private Long getIdFromAudienceAliasId(int platformId, long aliasId) {
        return audienceAliasIdToIdMapper.get(platformId).get(aliasId);
    }
    
    private Long getIdFromAudienceName(int platformId, String audienceName) {
        return audienceNameToIdMapper.get(platformId).get(audienceName);
    }
    
}
