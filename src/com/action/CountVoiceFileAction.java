package com.action;

import com.ctid.core.exception.ServiceException;
import com.hibernate.dao.ProvinceDao;
import com.hibernate.dao.ProvinceDaoImpl;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.Preparable;
import com.util.AppContextUtil;
import com.util.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.util.HttpUtil.readInputStreamToString;

/**
 * @author gujinkai <br/>
 * @ClassName: <br/>
 * @Description: TODO <br/>
 * @email gujinkai_xy@189.cn <br/>
 * @date
 */
public class CountVoiceFileAction extends ActionSupport{
    private static final long serialVersionUID = 1L;
    private String message;
    private String date;
    private int resultStr;
    private String errorProvince;
    private List<String> errorProvinceList = new ArrayList<>();
    private int errorProvinceListLength = 0;
    private StringBuffer preErrorProvince = new StringBuffer();
    private String[] preResult;
    private String[] resultDetail;
    private final int maxConnCount = 3;
    private Vector<Thread> vector = new Vector<>();
    ApplicationContext ctx = new ClassPathXmlApplicationContext("/com/config/spring/bean.xml");
    private TaskExecutor taskExecutor = (TaskExecutor)ctx.getBean("taskExecutor");
    private ProvinceDao provinceDao = new ProvinceDaoImpl();

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getResultStr() {
        return resultStr;
    }

    public void setResultStr(int resultStr) {
        this.resultStr = resultStr;
    }

    public String[] getPreResult() {
        return preResult;
    }

    public void setPreResult(String[] preResult) {
        this.preResult = preResult;
    }

    public String getErrorProvince() {
        return errorProvince;
    }

    public void setErrorProvince(String errorProvince) {
        this.errorProvince = errorProvince;
    }

    public String[] getResultDetail() {
        return resultDetail;
    }

    public void setResultDetail(String[] resultDetail) {
        this.resultDetail = resultDetail;
    }

    public List<String> getErrorProvinceList() {
        return errorProvinceList;
    }

    public void setErrorProvinceList(List<String> errorProvinceList) {
        this.errorProvinceList = errorProvinceList;
    }

    /*@Override
    public void validate(){
        if(date==null || date.matches())){
            addFieldError("日期","输入格式不对，请重新输入！");
        }
    }*/

    @SuppressWarnings("uncheck")
    @Override
    public String execute() throws Exception {
        if(date==null || date.equals("")){
            this.message = "请输入日期";
            return "failed";
        }
        String provinceCode = AppContextUtil.getProperties("PROVINCE_CODE");
        String[] provinceCodeArray = provinceCode.split(",");
        preResult = new String[provinceCodeArray.length];
        resultDetail = new String[provinceCodeArray.length];
        resultStr = provinceCodeArray.length;
        ArrayList<String> newProvinceCodeArray = new ArrayList<String>();
        ArrayList<Map> result = new ArrayList<>();
        final CountDownLatch downLatch = new CountDownLatch(provinceCodeArray.length);
        for(int i=0; i < provinceCodeArray.length;i++){
            /*Thread childThread = new Thread(new ProvinceTask(provinceCodeArray,i));
            childThread.start();
            vector.add(childThread);*/
            taskExecutor.execute(new ProvinceTaskPoor(provinceCodeArray,i,downLatch));
        }
        downLatch.await();
        /*for(Thread thread:vector){
            thread.join();
        }*/
        errorProvince = preErrorProvince.toString();
        return SUCCESS;
    }

    public class ProvinceTaskPoor implements Runnable{
        private  String[] provinceCodeArray;
        private int i;
        private CountDownLatch downLatch;

        public ProvinceTaskPoor(String[] provinceCodeArray, int i, CountDownLatch downLatch) {
            this.provinceCodeArray = provinceCodeArray;
            this.i = i;
            this.downLatch = downLatch;
        }

        @Override
        public void run(){
            try{
                runTask(provinceCodeArray,downLatch,i);
            }catch (Exception e){
                this.downLatch.countDown();
                throw new RuntimeException();
            }

        }
    }

    public void runTask(String[] provinceCodeArray,CountDownLatch downLatch,int i) throws Exception{
        long startTime = System.currentTimeMillis();
        String provinceName = "";
        provinceName = provinceDao.getProvinceNameByNumber(provinceCodeArray[i]);
        System.out.println(provinceName+"开始");
        Map<String,Integer> provinceresult = new HashMap();
        int totle = 0;
        String provinceASR = provinceDao.getProvinceASRByNumber(provinceCodeArray[i]);
        String[] provinceASRArray = provinceASR.split(",");
        for(String j : provinceASRArray){
                /*newProvinceCodeArray.add(provinceCodeArray[i] + "00" +j);*/
            String provincecode = provinceCodeArray[i] + "00" +j;
            int count = getVoiceFileCount(provincecode);
            if (count!=0){
                totle+=count;
                provinceresult.put(provincecode,count);
            }
        }
        System.out.println(provinceName+":"+totle);
        provinceresult.put(provinceCodeArray[i],totle);
        resultDetail[i] = provinceresult.toString();
        int number = i+1;
        preResult[i] = number+"."+provinceCodeArray[i]+"_"+provinceName+":"+totle;
            /*result.add(provinceresult);*/
        long endTime = System.currentTimeMillis();
        long useTime = endTime - startTime;
        System.out.println("查询"+provinceName+"用时："+ useTime + "毫秒.");
        downLatch.countDown();
    }

    public int getVoiceFileCount(String provincecode) throws Exception{
        int count = 0;
        String url = "http://180.153.61.47:18090/"+provincecode+"/count/";
        int overTime = 2000;
        String strResponse = "";
        int ifLive = 0;
        int connCount = 0;
        long startTime = System.currentTimeMillis();
        while (connCount <= maxConnCount){
            try{
                strResponse = HttpDoGet(url, "UTF-8", overTime);
                ifLive = 1;
            }catch (Exception e){
                strResponse = "省份编码：" + provincecode + "不存在";
                ifLive = 0;
            }finally {
                connCount++;
            }
        }
        long endTime = System.currentTimeMillis();
        long useTime = endTime - startTime;
        System.out.println(provincecode+"用时:"+useTime+"毫秒.");
        if (strResponse.equals("")){
            strResponse = "省份编码：" + provincecode + "不存在";
        }else {
            if (ifLive == 1){
                count = parseHTML(strResponse,provincecode,count);
            }

        }
        return count;
    }

    public int parseHTML (String strResponse,String provincecode,int count) throws Exception{
        Document doc = Jsoup.parse(strResponse);
        Elements links = doc.select("a[href]");

        for (Element link : links) {
            String linkHref = link.attr("href");
            String linkText = link.text();
            String number;
            if (linkText!=null && linkText!=""){
                if(linkText.contains("LOG")){
                    if(linkText.contains(date)){
                        int one = linkText.lastIndexOf("-");
                        int last = linkText.lastIndexOf(".");
                        if(last == -1){
                            last = linkText.length();
                        }else {
                            continue;
                        }
                        try{
                            number = linkText.substring(one+1,last);
                        }catch (Exception e){
                            System.out.println("获取数据异常");
                            number = "err";
                        }
                        if(!number.equals("err")){
                            count = Integer.valueOf(number);
                        }else {
                            errorProvinceListLength++;
                            preErrorProvince.append(provincecode);
                            preErrorProvince.append("\n");
                            errorProvinceList.add(provincecode);
                        }
                    }
                }
            }

        }
        return count;
    }

    public static String HttpDoGet (String url, String charset, int timeOut) throws Exception{
        Long startTime = System.currentTimeMillis();
        if (timeOut == 0) {
            // 默认5000，即5秒超时
            timeOut = 5000;
        }
        String result = "";
        // 读取响应输入流
        InputStream in = null;
        // 存储参数
        StringBuffer sb = new StringBuffer();
        // 编码之后的参数
        String params = "";
        try {
            String fullUrl = url;
            // 创建URL对象
            URL connURL = new URL(fullUrl);
            // 打开URL连接
            HttpURLConnection httpConn = (HttpURLConnection) connURL
                    .openConnection();
            // 设置通用属性
            httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
            // 设置连接主机超时（单位：毫秒）
            httpConn.setConnectTimeout(timeOut);
            // 设置从主机读取数据超时（单位：毫秒）
            httpConn.setReadTimeout(timeOut);
            // 建立实际的连接
            httpConn.connect();
            // 定义BufferedReader输入流来读取URL的响应,并设置编码方式
            // 读取返回的内容
            in = httpConn.getInputStream();
            result = readInputStreamToString(in, charset);
            httpConn.disconnect();
        } catch(Exception e) {
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    in = null;
                }
            }
            Long endTime = System.currentTimeMillis();
        }
        return result;
    }
}
