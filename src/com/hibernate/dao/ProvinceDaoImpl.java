package com.hibernate.dao;

import com.hibernate.entity.Province;
import com.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author gujinkai <br/>
 * @ClassName: <br/>
 * @Description: TODO <br/>
 * @email gujinkai_xy@189.cn <br/>
 * @date
 */
public class ProvinceDaoImpl implements ProvinceDao{

    @Override
    public String getProvinceNameByNumber(String provinceNumber) {
        Province province = null;
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try{
            province = (Province)session.get(Province.class,provinceNumber);
            tx.commit();
        }catch (Exception e){
            e.printStackTrace();
            tx.rollback();
        }finally {
            HibernateUtil.closeSession();
        }

        return province.getProvinceName();
    }

    @Override
    public String getProvinceASRByNumber(String provinceNumber) {
        Province province = null;
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try{
            province = (Province) session.get(Province.class,provinceNumber);
            tx.commit();
        }catch (Exception e){
            e.printStackTrace();
            tx.rollback();
        }finally {
            HibernateUtil.closeSession();
        }
        return province.getProvinceASR();
    }
}
