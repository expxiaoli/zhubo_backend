package com.zhubo.global;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhubo.entity.Platform;
import com.zhubo.helper.ModelHelper;

public class ResourceManager {
    private SessionFactory sessionFactory;
    private Session session;
    private Transaction transaction;
    private static volatile ResourceManager instance = null;
    private static final Object lock = new Object();
    public static List<Platform> platforms = Lists.newArrayList(
            new Platform(1, "奇秀"),
            new Platform(2, "来疯"),
            new Platform(3, "我秀"),
            new Platform(4, "千帆"),
            new Platform(5, "花椒"),
            new Platform(6, "一直播"),
            new Platform(12, "映客"),
            new Platform(13, "hani"),
            new Platform(14, "huoszb")
            );
    private DatabaseCache dbCache;
    private int getSessionCount;

    public static ResourceManager generateResourceManager() {
        ResourceManager tmp = instance;
        if (tmp == null) {
            synchronized (lock) {
                tmp = instance;
                if (tmp == null) {
                    tmp = new ResourceManager();
                    instance = tmp;
                }
            }
        }
        return instance;
    }

    public ResourceManager() {
        init();
    }

    public void init() {
        initDatabase();
        initPlatform(this);
    }
    
    public void initDatabaseCache(Date minTs, Date maxTs) {
        dbCache = new DatabaseCache(this, minTs, maxTs);
    }
    
    public void loadBatchParsePageCache(int platformId) {
        dbCache.batchLoadParsePageData(platformId);
    }
    
    public void loadBatchProcessDataCache(int platformId) {
        dbCache.batchLoadProcessData(platformId);
    }
    
    public void clearParsePageCache() {
        dbCache.clearParsePageData();
    }
    
    public void clearProcessDataCache() {
        dbCache.clearProcessData();
    }

    private void initDatabase() {
        Configuration cfg = new Configuration().configure("hibernate.cfg.xml");
        StandardServiceRegistryBuilder sb = new StandardServiceRegistryBuilder();
        sb.applySettings(cfg.getProperties());
        StandardServiceRegistry standardServiceRegistry = sb.build();
        sessionFactory = cfg.buildSessionFactory(standardServiceRegistry);
        if(session == null) {
            session = sessionFactory.openSession();
        }
        if(transaction == null) {
            transaction = session.beginTransaction();
        }
        getSessionCount = 0;
    }

    private void initPlatform(ResourceManager rm) {
        for (Platform platform : platforms) {
            Platform oldPlatform = ModelHelper.getPlatform(rm, platform.getPlatformName());
            if (oldPlatform == null) {
                rm.getDatabaseSession().save(platform);
            }
        }
    }

    public Session getDatabaseSession() {
        getSessionCount++;
        if(session == null) {
            session = sessionFactory.openSession();
        }
        if(getSessionCount >= 1000) {
            commit();
            transaction = null;
            session.close();
            session = sessionFactory.openSession();
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ using this session too long, get new one");
            getSessionCount = 0;
        }
        if(transaction == null || transaction.wasCommitted()) {
            transaction = session.beginTransaction();
        }
        return session;
    }
    
    public Session getNewDatabaseSession() {
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();
        return session;
    }
    
    public synchronized void closeSessionAndTransaction() {
        if (transaction != null) {
            transaction.commit();
            transaction = null;
        }
        if (session != null) {
            session.close();
            session = null;
        }
    }
    
    public DatabaseCache getDatabaseCache() {
        return dbCache;
    }
    

    public void commit() {
        if(transaction == null || transaction.wasCommitted()) {
            transaction = session.beginTransaction();
        }
        transaction.commit();
    }

    public synchronized void close() {
        if (transaction != null) {
            transaction.commit();
            transaction = null;
        }
        if (session != null) {
            session.close();
            session = null;
        }
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
