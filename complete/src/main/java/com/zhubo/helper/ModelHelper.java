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
import com.zhubo.global.DatabaseCache.PayPeriodObject;
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
       
}
