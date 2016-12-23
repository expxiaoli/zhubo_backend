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
import com.zhubo.entity.PlatformTaskRun;
import com.zhubo.entity.TaskGroupRun;
import com.zhubo.entity.TaskRun;
import com.zhubo.global.DatabaseCache.PayPeriodObject;
import com.zhubo.global.ResourceManager;

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
    
    public static Anchor getAnchor(ResourceManager rm, Long anchorId) {
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from Anchor where anchor_id = :anchor_id");
        query.setParameter("anchor_id", anchorId);
        List<Anchor> anchors = query.list();
        if(anchors.isEmpty()) {
            return null;
        } else {
            return anchors.get(0);
        }
    }
    
    public static Audience getAudience(ResourceManager rm, int platformId, Long audienceAliasId, String audienceName) {
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from Audience where (audience_alias_id = :audience_alias_id or audience_name = :audience_name) and platform_id = :platform_id");
        query.setParameter("platform_id", platformId);
        query.setParameter("audience_name", audienceName);
        query.setParameter("audience_alias_id", audienceAliasId);
        List<Audience> audiences = query.list();
        if(audiences.isEmpty()) {
            return null;
        } else {
            return audiences.get(0);
        }
    }
    
    public static Audience getAudience(ResourceManager rm, Long audienceId) {
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from Audience where audience_id = :audience_id");
        query.setParameter("audience_id", audienceId);
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
    
    public static TaskRun markParsePageTaskStart(ResourceManager rm, String className, Integer platformId, String folderPath) {
        TaskRun taskRun = new TaskRun(className, "ParsePage", platformId, folderPath, null, null, new Date());
        rm.getDatabaseSession().save(taskRun);
        rm.commit();
        return taskRun;
    }
    
    public static TaskRun markProcessDataTaskStart(ResourceManager rm, String className, Integer platformId, Date processStart, Date processEnd) {
        TaskRun taskRun = new TaskRun(className, "ProcessData", platformId, null, processStart, processEnd, new Date());
        rm.getDatabaseSession().save(taskRun);
        rm.commit();
        return taskRun;
    }    
    
    public static void markTaskSuccess(ResourceManager rm, TaskRun taskRun) {
        taskRun.setSuccessAndCompleted(true, new Date());
        rm.getDatabaseSession().update(taskRun);
        rm.commit();
    }
    
    public static TaskGroupRun markTaskGroupStart(ResourceManager rm, String dataTime) {
        TaskGroupRun tgr = new TaskGroupRun(dataTime, new Date());
        rm.getDatabaseSession().save(tgr);
        rm.commit();
        return tgr;
    }
    
    public static void markTaskGroupSuccess(ResourceManager rm, TaskGroupRun tgr) {
        tgr.setSuccessAndCompleted(true, new Date());
        rm.getDatabaseSession().update(tgr);
        rm.commit();
    }
    
    public static PlatformTaskRun markPlatformTaskRunStart(ResourceManager rm, String dataTime, int platformId) {
        PlatformTaskRun platformTaskRun = new PlatformTaskRun(platformId, dataTime, new Date());
        rm.getDatabaseSession().save(platformTaskRun);
        rm.commit();
        return platformTaskRun;
    }
    
    public static void markPlatformTaskRunSuccess(ResourceManager rm, PlatformTaskRun ptr) {
        ptr.setSuccessAndCompleted(true, new Date());
        rm.getDatabaseSession().update(ptr);
        rm.commit();
    }
}
