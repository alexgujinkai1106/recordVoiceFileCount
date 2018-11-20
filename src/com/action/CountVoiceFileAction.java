package com.action;

import com.ctid.core.exception.ServiceException;
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
    private String resultStr;
    private String errorProvince;
    private List<String> errorProvinceList = new ArrayList<>();
    private int errorProvinceListLength = 0;
    private StringBuffer preErrorProvince = new StringBuffer();
    private Map<String,Integer> preResult = new HashMap<>();
    private String[] resultDetail;
    private Vector<Thread> vector = new Vector<>();
    ApplicationContext ctx = new ClassPathXmlApplicationContext("/com/config/spring/bean.xml");
    private TaskExecutor taskExecutor = (TaskExecutor)ctx.getBean("taskExecutor");

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getResultStr() {
        return resultStr;
    }

    public void setResultStr(String resultStr) {
        this.resultStr = resultStr;
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
        resultDetail = new String[provinceCodeArray.length];
        ArrayList<String> newProvinceCodeArray = new ArrayList<String>();
        ArrayList<Map> result = new ArrayList<>();
        final CountDownLatch downLatch = new CountDownLatch(provinceCodeArray.length);
        System.out.println(downLatch);
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
        resultStr = preResult.toString();
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
            System.out.println(downLatch);
            Map<String,Integer> provinceresult = new HashMap();
            int totle = 0;
            for(int j=1; j<=5;j++){
                /*newProvinceCodeArray.add(provinceCodeArray[i] + "00" +j);*/
                String provincecode = provinceCodeArray[i] + "00" +j;
                int count = getVoiceFileCount(provincecode);
                if (count!=0){
                    totle+=count;
                    provinceresult.put(provincecode,count);
                }
            }
            provinceresult.put(provinceCodeArray[i],totle);
            resultDetail[i] = provinceresult.toString();
            preResult.put(provinceCodeArray[i],totle);
            /*result.add(provinceresult);*/
            downLatch.countDown();
        }
    }


    public class ProvinceTask implements Runnable{
        private  String[] provinceCodeArray;
        private int i;

        public ProvinceTask(String[] provinceCodeArray, int i) {
            this.provinceCodeArray = provinceCodeArray;
            this.i = i;
        }

        @Override
        public void run(){
            Map<String,Integer> provinceresult = new HashMap();
            int totle = 0;
            for(int j=1; j<=5;j++){
                /*newProvinceCodeArray.add(provinceCodeArray[i] + "00" +j);*/
                String provincecode = provinceCodeArray[i] + "00" +j;
                int count = getVoiceFileCount(provincecode);
                if (count!=0){
                    totle+=count;
                    provinceresult.put(provincecode,count);
                }
            }
            provinceresult.put(provinceCodeArray[i],totle);
            resultDetail[i] = provinceresult.toString();
            preResult.put(provinceCodeArray[i],totle);
            /*result.add(provinceresult);*/
        }
    }

    public int getVoiceFileCount(String provincecode){
        int count = 0;
        String url = "http://180.153.61.47:18090/"+provincecode+"/count/";
        int overTime = 3000;
        String strResponse = "";
        try{
            strResponse = HttpDoGet(url, "UTF-8", overTime);
        }catch (Exception e){
            strResponse = "省份编码：" + provincecode + "不存在";
        }
        if (strResponse.equals("")){
            strResponse = "省份编码：" + provincecode + "不存在";
        }else {
            Document doc = Jsoup.parse(strResponse);
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String linkHref = link.attr("href");
                String linkText = link.text();
                if (linkText!=null && linkText!=""){
                    if(linkText.contains("LOG")){
                        if(linkText.contains(date)){
                            int last = linkText.lastIndexOf("-");
                            String number = linkText.substring(last+1,linkText.length());
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
        }
        return count;
    }

    public static String HttpDoGet(String url, String charset, int timeOut) {
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
