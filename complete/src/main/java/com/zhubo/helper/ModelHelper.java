package com.zhubo.helper;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.zhubo.entity.Anchor;
import com.zhubo.entity.Audience;
import com.zhubo.entity.Platform;
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
}
