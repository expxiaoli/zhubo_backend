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

public class DatabaseCache {
    private static final int maxPlatformId = 1;
    
    private ResourceManager rm;
    private Date minTs;
    private Date maxTs;
    
    private Map<Integer, Map<Long, Long>> audienceAliasIdToIdMapper;
    private Map<Integer, Map<String, Long>> audienceNameToIdMapper;
    private Map<Long, Map<Long, Set<Date>>> payByMinutesDatesMapper;
    private Map<Long, Map<String, Set<Date>>> metricByMinutesDatesMapper;
    
    public DatabaseCache(ResourceManager rm, Date minTs, Date maxTs) {
        this.rm = rm;
        this.minTs = minTs;
        this.maxTs = maxTs;
    }
    
    public void batchLoad() {
        batchLoadAudienceData();
        batchLoadPayByMinutes();
        batchLoadMetricByMinutes();
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
