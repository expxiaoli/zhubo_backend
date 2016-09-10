package com.zhubo.global;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class ResourceManager {
	private SessionFactory sessionFactory;
	private Session session;
	private Transaction transaction;
	private static volatile ResourceManager instance = null;
	private static final Object lock = new Object();

	public static ResourceManager generateResourceManager() {
	    ResourceManager tmp = instance;
		if (tmp == null) {
		    synchronized(lock) {
		        tmp = instance;
		        if(tmp == null) {
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
		Configuration cfg = new Configuration().configure("hibernate.cfg.xml");           
        StandardServiceRegistryBuilder sb = new StandardServiceRegistryBuilder();
        sb.applySettings(cfg.getProperties());
        StandardServiceRegistry standardServiceRegistry = sb.build();               
        sessionFactory = cfg.buildSessionFactory(standardServiceRegistry);      
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();
	}

	public Session getDatabaseSession() {
		return session;
	}
	
	public Transaction getTransaction() {
		return transaction;
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
