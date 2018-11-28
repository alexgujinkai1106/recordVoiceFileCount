package com.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author gujinkai <br/>
 * @ClassName: <br/>
 * @Description: TODO <br/>
 * @email gujinkai_xy@189.cn <br/>
 * @date
 */
public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static final ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();
    static {
        try{
            //读取配置文件hibernate.cfg.xml
            Configuration cfg = new Configuration().configure();
            //创建sessionFactory
            sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
    //获取sessionFactory
    public static SessionFactory getSessionFactory(){
        return sessionFactory;
    }
    //获取ThreadLocal对象管理的Session实例
    public static Session getSession() throws HibernateException {
        Session session = (Session) threadLocal.get();
        if (session==null || !session.isOpen()){
            if (sessionFactory == null){
                rebuildSessionFactory();
            }
            //通过sessionFactory对象创建Session对象
            session = (sessionFactory != null)?sessionFactory.openSession():null;
            //将打开的Session实例保存到线程局部变量threadLocal中
            threadLocal.set(session);
        }
        return session;
    }
    //关不Session实例
    public static void closeSession() throws HibernateException {
        Session session = (Session) threadLocal.get();
        threadLocal.set(null);
        if(session != null) {
            session.close();
        }
    }
    //重建SessionFactory
    public static void rebuildSessionFactory() {
        try{
            //读取配置文件hibernate.cfg.xml
            Configuration cfg = new Configuration().configure("/hibernate.cfg.xml");
            sessionFactory = cfg.buildSessionFactory();
        }catch (Exception e){
            System.out.println("Error Createing SessionFactory");
            e.printStackTrace();
        }
    }
    //关闭缓存和连接池
    public static void shutdown() {
        getSessionFactory().close();
    }
}
