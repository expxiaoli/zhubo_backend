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
    private static List<Platform> platforms = Lists.newArrayList(new Platform(1, "奇秀"));
    private DatabaseCache dbCache;

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
    
    public void initDatabaseCacheAndBatchLoad(Date minTs, Date maxTs) {
        dbCache = new DatabaseCache(this, minTs, maxTs);
        dbCache.batchLoad();
    }

    private void initDatabase() {
        Configuration cfg = new Configuration().configure("hibernate.cfg.xml");
        StandardServiceRegistryBuilder sb = new StandardServiceRegistryBuilder();
        sb.applySettings(cfg.getProperties());
        StandardServiceRegistry standardServiceRegistry = sb.build();
        sessionFactory = cfg.buildSessionFactory(standardServiceRegistry);
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();
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
        return session;
    }

    public Transaction getTransaction() {
        return transaction;
    }
    
    public DatabaseCache getDatabaseCache() {
        return dbCache;
    }
    

    public void commit() {
        transaction.commit();
        session.close();
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();
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
