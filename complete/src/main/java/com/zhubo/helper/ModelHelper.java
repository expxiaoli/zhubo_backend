package com.zhubo.helper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import com.google.common.collect.Maps;
import com.zhubo.entity.Anchor;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.entity.Audience;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.entity.AudiencePayPeriod;
import com.zhubo.entity.Platform;
import com.zhubo.global.ResourceManager;
import com.zhubo.task.parsepage.ParseQixiuRoomPageTask.Pay;

public class ModelHelper {
    
    public static Anchor getAnchor(ResourceManager rm, int platformId, Long anchorAliasId) {
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from Anchor where platform_id = :platform_id and anchor_alias_id = :anchor_alias_id");
        query.setParameter("platform_id", platformId);
        query.setParameter("anchor_alias_id", anchorAliasId);
        List<Anchor> anchors = query.list();
        if(anchors.isEmpty()) {
            return null;
        } else {
            return anchors.get(0);
        }
    }
    
    public static Audience getAudience(ResourceManager rm, int platformId, String audienceName) {
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from Audience where audience_name = :audience_name");
        query.setParameter("audience_name", audienceName);
        List<Audience> audiences = query.list();
        if(audiences.isEmpty()) {
            return null;
        } else {
            return audiences.get(0);
        }
    }
    
    public static Platform getPlatform(ResourceManager rm, String platformName) {
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from Platform where platform_name = :platform_name");
        query.setParameter("platform_name", platformName);
        List<Platform> platforms = query.list();
        if(platforms.isEmpty()) {
            return null;
        } else {
            return platforms.get(0);
        }
    }
    
    public static AnchorMetricByMinutes getMetric(ResourceManager rm, long anchorId, String type, Date ts) {
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AnchorMetricByMinutes where anchor_id = :anchor_id and type = :type and record_effective_time = :record_effective_time");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("type", type);
        query.setParameter("record_effective_time", ts);
        List<AnchorMetricByMinutes> metrics = query.list();
        if(metrics.isEmpty()) {
            return null;
        } else {
            return metrics.get(0);
        }
    }
    
    public static AudiencePayByMinutes getPay(ResourceManager rm, long audienceId, long anchorId, Date ts) {
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from AudiencePayByMinutes where anchor_id = :anchor_id and audience_id = :audience_id and record_effective_time = :record_effective_time");
        query.setParameter("anchor_id", anchorId);
        query.setParameter("audience_id", audienceId);
        query.setParameter("record_effective_time", ts);
        List<AudiencePayByMinutes> metrics = query.list();
        if(metrics.isEmpty()) {
            return null;
        } else {
            return metrics.get(0);
        }
    }
    
    public static AudiencePayPeriod getLatestPayPeriod(ResourceManager rm, long audienceId, long anchorId) {
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
    
    public static Map<Long, Map<Long, PayPeriodObject>> getAllLatestPayPeriod(ResourceManager rm, int platformId) {
        Session session = rm.getDatabaseSession();
        Map<Long, Map<Long, PayPeriodObject>> payCache = Maps.newHashMap();
        Query query = session.createQuery("from AudiencePayPeriod where platform_id = :platform_id order by record_effective_time desc");
        query.setParameter("platform_id", platformId);
        List<AudiencePayPeriod> payRecords = query.list();
        for(AudiencePayPeriod payRecord : payRecords) {
            putPayPeriodInCache(payCache, payRecord.getAudienceId(), payRecord.getAnchorId(), 
                    new PayPeriodObject(payRecord.getPlatformId(), payRecord.getMoney(), payRecord.getPeriodStart(), payRecord.getRecordEffectiveTime()));
        }
        return payCache;
    }
    
    public static void setAllLatestPayPeriod(ResourceManager rm, Map<Long, Map<Long, PayPeriodObject>> cache, int platformId) {
        for(Long audienceId : cache.keySet()) {
            Map<Long, PayPeriodObject> audienceCache = cache.get(audienceId);
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
        }
        rm.commit();
    }
    
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
    
    private static void putPayPeriodInCache(Map<Long, Map<Long, PayPeriodObject>> payCache, long audienceId, long anchorId, 
            PayPeriodObject payPeriod) {
        if(!payCache.containsKey(audienceId)) {
            payCache.put(audienceId, Maps.newHashMap());
        }
        Map<Long, PayPeriodObject> audiencePays = payCache.get(audienceId);
        audiencePays.put(anchorId, payPeriod);
    }
       
    public static int getDiffMoneyAndUpdatePayPeriodInCache(Map<Long, Map<Long, PayPeriodObject>> payCache, long audienceId, long anchorId, PayPeriodObject payPeriod) {
        PayPeriodObject oldPayPeriod = getPayPeriodFromCache(payCache, audienceId, anchorId);
        if(oldPayPeriod == null) {
            putPayPeriodInCache(payCache, audienceId, anchorId, payPeriod);
            return 0;
        } else if (oldPayPeriod.money < payPeriod.money) {
            putPayPeriodInCache(payCache, audienceId, anchorId, payPeriod);
            int diffMoney = payPeriod.money - oldPayPeriod.money;
            return diffMoney;
        } else if (oldPayPeriod.money > payPeriod.money) {
            putPayPeriodInCache(payCache, audienceId, anchorId, payPeriod);
            return payPeriod.money;
        } else {
            return 0;
        }
    }
    
    private static PayPeriodObject getPayPeriodFromCache(Map<Long, Map<Long, PayPeriodObject>> payCache, long audienceId, long anchorId) {
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
}
