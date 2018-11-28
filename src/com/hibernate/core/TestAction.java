package com.hibernate.core;/**
 * Created by alex.gu on 2018/11/22.
 */

import com.hibernate.dao.ProvinceDao;
import com.hibernate.dao.ProvinceDaoImpl;
import com.hibernate.entity.Province;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;

/**
 * @author gujinkai <br/>
 * @ClassName: <br/>
 * @Description: TODO <br/>
 * @email gujinkai_xy@189.cn <br/>
 * @date
 */
public class TestAction extends ActionSupport {

    private static final long serialVersionUID = 1L;

    @Override
    public String execute() throws Exception {
        ProvinceDao provinceDao = new ProvinceDaoImpl();
        try{
            Province province = new Province();
            String name = provinceDao.getProvinceNameByNumber("8110000");
            System.out.println(name);
        }catch (Exception e){
            e.printStackTrace();
        }
        return SUCCESS;
    }
}
