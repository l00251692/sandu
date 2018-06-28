package com.changyu.foryou.tools;

import java.io.BufferedReader;  
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;  
import java.io.InputStream;  
import java.io.InputStreamReader;  
import java.io.OutputStream;  
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;  
import java.net.URL;
import java.security.KeyStore;
import java.security.SignatureException;

import java.text.MessageFormat;
import java.util.ArrayList;  
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;  
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;  
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentHelper;
import org.jdom.Document;  
import org.jdom.Element;  
import org.jdom.JDOMException;  
import org.jdom.input.SAXBuilder;
import org.springframework.util.ClassUtils;

import com.alibaba.fastjson.JSONObject;



public class PayUtil {  
	
	public static long ACCESS_TOKEN_TIME = 0;
	
	public static final String CURRENCY_FEN_REGEX = "\\-?[0-9]+";
	
	public static Map<String,String> tempData = new HashMap<String, String>();
	
     /**   
     * 签名字符串   
     * @param text需要签名的字符串   
     * @param key 密钥   
     * @param input_charset编码格式   
     * @return 签名结果   
     */     
    public static String sign(String text, String key, String input_charset) {     
        text = text + "&key=" + key;     
        return DigestUtils.md5Hex(getContentBytes(text, input_charset));     
    }     
    /**   
     * 签名字符串   
     *  @param text需要签名的字符串   
     * @param sign 签名结果   
     * @param key密钥   
     * @param input_charset 编码格式   
     * @return 签名结果   
     */     
    public static boolean verify(String text, String sign, String key, String input_charset) {     
        text = text + key;     
        String mysign = DigestUtils.md5Hex(getContentBytes(text, input_charset));     
        if (mysign.equals(sign)) {     
            return true;     
        } else {     
            return false;     
        }     
    }     
    /**   
     * @param content   
     * @param charset   
     * @return   
     * @throws SignatureException   
     * @throws UnsupportedEncodingException   
     */     
    public static byte[] getContentBytes(String content, String charset) {     
        if (charset == null || "".equals(charset)) {     
            return content.getBytes();     
        }     
        try {     
            return content.getBytes(charset);     
        } catch (UnsupportedEncodingException e) {     
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);     
        }     
    }     
      
    private static boolean isValidChar(char ch) {     
        if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'))     
            return true;     
        if ((ch >= 0x4e00 && ch <= 0x7fff) || (ch >= 0x8000 && ch <= 0x952f))     
            return true;// 简体中文汉字编码     
        return false;     
    }     
    /**   
     * 除去数组中的空值和签名参数   
     * @param sArray 签名参数组   
     * @return 去掉空值与签名参数后的新签名参数组   
     */     
    public static Map<String, String> paraFilter(Map<String, String> sArray) {     
        Map<String, String> result = new HashMap<String, String>();     
        if (sArray == null || sArray.size() <= 0) {     
            return result;     
        }     
        for (String key : sArray.keySet()) {     
            String value = sArray.get(key);     
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")     
                    || key.equalsIgnoreCase("sign_type")) {     
                continue;     
            }     
            result.put(key, value);     
        }     
        return result;     
    }     
    /**   
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串   
     * @param params 需要排序并参与字符拼接的参数组   
     * @return 拼接后字符串   
     */     
    public static String createLinkString(Map<String, String> params) {     
        List<String> keys = new ArrayList<String>(params.keySet());     
        Collections.sort(keys);     
        String prestr = "";     
        for (int i = 0; i < keys.size(); i++) {     
            String key = keys.get(i);     
            String value = params.get(key);     
            if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符     
                prestr = prestr + key + "=" + value;     
            } else {     
                prestr = prestr + key + "=" + value + "&";     
            }     
        }     
        return prestr;     
    }     
    /**   
     *   
     * @param requestUrl请求地址   
     * @param requestMethod请求方法   
     * @param outputStr参数   
     */     
    public static String httpRequest(String requestUrl,String requestMethod,String outputStr){     
        // 创建SSLContext     
        StringBuffer buffer = null;     
        try{     
            URL url = new URL(requestUrl);     
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();     
            conn.setRequestMethod(requestMethod);     
            conn.setDoOutput(true);     
            conn.setDoInput(true);     
            conn.connect();     
            //往服务器端写内容     
            if(null !=outputStr){     
                OutputStream os=conn.getOutputStream();     
                os.write(outputStr.getBytes("utf-8"));     
                os.close();     
            }     
            // 读取服务器端返回的内容     
            InputStream is = conn.getInputStream();     
            InputStreamReader isr = new InputStreamReader(is, "utf-8");     
            BufferedReader br = new BufferedReader(isr);     
            buffer = new StringBuffer();     
            String line = null;     
            while ((line = br.readLine()) != null) {     
                buffer.append(line);     
            }     
        }catch(Exception e){     
            e.printStackTrace();     
        }  
        return buffer.toString();  
    }  
    
    public static String urlEncodeUTF8(String source){     
        String result=source;     
        try {     
            result=java.net.URLEncoder.encode(source, "UTF-8");     
        } catch (UnsupportedEncodingException e) {     
            // TODO Auto-generated catch block     
            e.printStackTrace();     
        }     
        return result;     
    }   
    /** 
     * 解析xml,返回第一级元素键值对。如果第一级元素有子节点，则此节点的值是子节点的xml数据。 
     * @param strxml 
     * @return 
     * @throws JDOMException 
     * @throws IOException 
     */  
    public static Map doXMLParse(String strxml) throws Exception {  
        if(null == strxml || "".equals(strxml)) {  
            return null;  
        }  
          
        Map m = new HashMap();  
        InputStream in = String2Inputstream(strxml);  
        SAXBuilder builder = new SAXBuilder();  
        Document doc = builder.build(in);  
        Element root = doc.getRootElement();  
        List list = root.getChildren();  
        Iterator it = list.iterator();  
        while(it.hasNext()) {  
            Element e = (Element) it.next();  
            String k = e.getName();  
            String v = "";  
            List children = e.getChildren();  
            if(children.isEmpty()) {  
                v = e.getTextNormalize();  
            } else {  
                v = getChildrenText(children);  
            }  
              
            m.put(k, v);  
        }  
          
        //关闭流  
        in.close();  
          
        return m;  
    }  
    /** 
     * 获取子结点的xml 
     * @param children 
     * @return String 
     */  
    public static String getChildrenText(List children) {  
        StringBuffer sb = new StringBuffer();  
        if(!children.isEmpty()) {  
            Iterator it = children.iterator();  
            while(it.hasNext()) {  
                Element e = (Element) it.next();  
                String name = e.getName();  
                String value = e.getTextNormalize();  
                List list = e.getChildren();  
                sb.append("<" + name + ">");  
                if(!list.isEmpty()) {  
                    sb.append(getChildrenText(list));  
                }  
                sb.append(value);  
                sb.append("</" + name + ">");  
            }  
        }  
          
        return sb.toString();  
    }  
    public static InputStream String2Inputstream(String str) {  
        return new ByteArrayInputStream(str.getBytes());  
    }  
    
    /**
	 * 获取接口acessToken
	 * @return
	 */
	public  static Map<String,Object> getAccessToken()throws Exception{
		Map<String,Object> resultMap = new HashMap<String, Object>();
		Long nowTime = new Date().getTime();
		//判断accessToken是否缓存 且是否过期
		if(ACCESS_TOKEN_TIME < nowTime){
			//请求接口地址
			String requestUrl = "https://api.weixin.qq.com/cgi-bin/token";

			
			//请求参数
			String parameters = MessageFormat.format(
				"grant_type=client_credential&appid={0}&secret={1}",Constants.appId, Constants.apiKey);
			
			String sr = HttpRequest.sendGet(requestUrl, parameters); 
			//解析相应内容（转换成json对象） 
			JSONObject json = JSONObject.parseObject(sr);
			 
			//获取新的有效时间 单位秒
			Long newExpiresTime = Long.valueOf(json.get("expires_in").toString()) ;
			//将access_token的有效时间更新（有效时间默认减少5分钟，避免意外）
			ACCESS_TOKEN_TIME = newExpiresTime*1000+nowTime-30000;
			//将access_token更新
			tempData.put("access_token", json.get("access_token").toString());
			resultMap.put("access_token", json.get("access_token").toString());
		}else{
			resultMap.put("access_token", tempData.get("access_token"));
		}
		return resultMap;
	}
	
	public static String refundpost(String url, String xmlParam)
	{
		StringBuilder sb = new StringBuilder();
     	try 
     	{
	        KeyStore keyStore  = KeyStore.getInstance("PKCS12");
	        FileInputStream instream = new FileInputStream(new File(Constants.REFUND_KEY_PATH));
	        try 
	        {
	        	keyStore.load(instream, Constants.mchId.toCharArray());
	        } 
	        finally 
	        {
	        	instream.close();
	        }
  
	        // 证书
	        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, Constants.mchId.toCharArray()).build();
	        // 只允许TLSv1协议
	        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,new String[] { "TLSv1" },null,SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
	        //创建基于证书的httpClient,后面要用到
	        CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(sslsf).build();
         
	        HttpPost httpPost = new HttpPost(url);//退款接口
	        StringEntity  reqEntity  = new StringEntity(xmlParam);
	        // 设置类型
	        reqEntity.setContentType("application/x-www-form-urlencoded");
	        httpPost.setEntity(reqEntity);
	        CloseableHttpResponse response = client.execute(httpPost);
	        try 
	        {
	        	HttpEntity entity = response.getEntity();
	        	System.out.println(response.getStatusLine());
	        	if (entity != null) {
	        		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(),"UTF-8"));
	        		String text="";
	        		while ((text = bufferedReader.readLine()) != null) {
		                 sb.append(text);
	        		}
		         }
		         EntityUtils.consume(entity);
	        }
	        catch(Exception e)
	        {
		         e.printStackTrace();
	        }
	        finally 
	        {
	         	try
	         	{
		            response.close();
		        } catch (IOException e) 
	         	{
		            e.printStackTrace();
		        }
	        }
     	} 
     	catch (Exception e)
     	{
		     e.printStackTrace();
		}
     	
     	return sb.toString();
     	
	}
	
	/**
     * 企业付款 参考https://blog.csdn.net/shaomiaojava/article/details/50562550
     * 
     * @description
     * @param openid
     * @param appid
     * @param mchid 商户id
     * @param nonce_str
     * @param partner_trade_no
     * @param re_user_name
     * @param jine
     * @param desc
     * @param spbill_create_ip
     * @param check_name
     * @return
     * @author Jobs
     * @throws IOException
     * @throws ClientProtocolException
     */
    public static boolean enterprisePayment(String openId, String orderId,
            String userName, String jine, String desc, String spbill_create_ip) throws Exception
    {
        boolean getSuccess = true;
        if (null != openId)
        {

            Map<String, String> packageParams = new HashMap<String, String>();
            
            String nonce_str = StringUtil.getRandomStringByLength(32);
            // appid
            packageParams.put("mch_appid" ,Constants.appId);
            // 商户id
            packageParams.put("mchid=", Constants.mchId);
            // 随机字符串
            packageParams.put("nonce_str" ,nonce_str);
            // 订单号自定义
            packageParams.put("partner_trade_no" ,orderId);

            packageParams.put("openid" ,openId);
            // 校验用户姓名选项
            /**
             * NO_CHECK：不校验真实姓名
             * FORCE_CHECK：强校验真实姓名（未实名认证的用户会校验失败，无法转账）
             * OPTION_CHECK：针对已实名认证的用户才校验真实姓名（未实名认证用户不校验，可以转账成功）
             */
            packageParams.put("check_name" ,"NO_CHECK");
            // 收款用户姓名
            packageParams.put("re_user_name" , userName);
            // 金额
            packageParams.put("amount" , jine);
            // 企业付款描述信息
            packageParams.put("desc" , desc);
            // Ip地址
            packageParams.put("spbill_create_ip" , spbill_create_ip);
            
            String prestr = PayUtil.createLinkString(packageParams);

            String mysign = PayUtil.sign(prestr, Constants.mchKey, "utf-8").toUpperCase();
  
          //拼接统一下单接口使用的xml数据，要将上一步生成的签名一起拼接进去  
            String xml = "<xml>" + "<mch_appid>" + Constants.appId + "</mch_appid>"      
                    + "<mch_id>" + Constants.mchId + "</mch_id>"   
                    + "<nonce_str>" + nonce_str + "</nonce_str>"   
                    + "<partner_trade_no>" + orderId + "</partner_trade_no>"   
                    + "<openid>" + openId + "</openid>"   
                    + "<check_name>" + "NO_CHECK" + "</check_name>"   
                    + "<re_user_name>" + userName + "</re_user_name>"   
                    + "<amount>" + jine + "</amount>"  
                    + "<desc>" + desc + "</desc>"   
                    + "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>"  
                    + "<sign>" + mysign + "</sign>"  
                    + "</xml>"; 
            
            // ZHENGSHU
            
            String zspath = ClassUtils.getDefaultClassLoader().getResource("").getPath() + Constants.CERTPATH;
            CloseableHttpClient httpclient = certificateValidation(zspath,Constants.mchId);

            HttpPost httppost = new HttpPost(
                    "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers");
            StringEntity myEntity = new StringEntity(xml, "UTF-8");
            httppost.setEntity(myEntity);
            System.out.println("executing request" + httppost.getRequestLine());

            CloseableHttpResponse response = httpclient.execute(httppost);
            System.out.println(response.getStatusLine());

            HttpEntity resEntity = response.getEntity();
            InputStreamReader reader = new InputStreamReader(
                    resEntity.getContent(), "UTF-8");
            char[] buff = new char[1024];
            int length = 0;
            StringBuffer strhuxml = new StringBuffer();
            while ((length = reader.read(buff)) != -1)
            {
                strhuxml.append(new String(buff, 0, length));
                System.out.println(new String(buff, 0, length));
            }
            // httpclient.close();
            httpclient.getConnectionManager().shutdown();
            
            Map map = PayUtil.doXMLParse(strhuxml.toString()); 
            
            String return_code = (String) map.get("return_code");//返回状态码  

            if(return_code=="SUCCESS"){ 
            	System.out.println("提现申请成功");
            	getSuccess = true;
            }
            else
            {
            	getSuccess = false;
            }
        }
        return getSuccess;
    }
    
    /**
     * 验证证书公共方法
     * 
     * @description
     * @param zfpath 证书的路径
     * @param mchid 商户id
     * @return
     * @throws Exception
     * @author Jobs
     */
    // shanghuid
    // 验证证书
    @SuppressWarnings("deprecation")
    public static CloseableHttpClient certificateValidation(String zfpath,
            String mchid) throws Exception
    {
        // 指定读取证书格式为PKCS12
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        // 证书地址
        FileInputStream instream = new FileInputStream(new File(zfpath));
        try
        {
            keyStore.load(instream, mchid.toCharArray());
        }
        finally
        {
            instream.close();
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, mchid.toCharArray()).build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext, new String[] { "TLSv1" }, null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf).build();
        return httpclient;
    }
    
    /**
     * 将分为单位的转换为元并返回金额格式的字符串 （除100）
     * 
     * @param amount
     * @return
     * @throws Exception
     */
    public static String changeF2Y(Long amount) throws Exception {
        if (!amount.toString().matches(CURRENCY_FEN_REGEX)) {
            throw new Exception("金额格式有误");
        }


        int flag = 0;
        String amString = amount.toString();
        if (amString.charAt(0) == '-') {
            flag = 1;
            amString = amString.substring(1);
        }
        StringBuffer result = new StringBuffer();
        if (amString.length() == 1) {
            result.append("0.0").append(amString);
        } else if (amString.length() == 2) {
            result.append("0.").append(amString);
        } else {
            String intString = amString.substring(0, amString.length() - 2);
            for (int i = 1; i <= intString.length(); i++) {
                if ((i - 1) % 3 == 0 && i != 1) {
                    result.append(",");
                }
                result.append(intString.substring(intString.length() - i, intString.length() - i + 1));
            }
            result.reverse().append(".").append(amString.substring(amString.length() - 2));
        }
        if (flag == 1) {
            return "-" + result.toString();
        } else {
            return result.toString();
        }
    }


    /**
     * 将分为单位的转换为元 （除100）
     * 
     * @param amount
     * @return
     * @throws Exception
     */
    public static String changeF2Y(String amount) throws Exception {
        if (!amount.matches(CURRENCY_FEN_REGEX)) {
            throw new Exception("金额格式有误");
        }
        return BigDecimal.valueOf(Long.valueOf(amount)).divide(new BigDecimal(100)).toString();
    }


    /**
     * 将元为单位的转换为分 （乘100）
     * 
     * @param amount
     * @return
     */
    public static String changeY2F(Long amount) {
        return BigDecimal.valueOf(amount).multiply(new BigDecimal(100)).toString();
    }


    /**
     * 将元为单位的转换为分 替换小数点，支持以逗号区分的金额
     * 
     * @param amount
     * @return
     */
    public static String changeY2F(String amount) {
        String currency = amount.replaceAll("\\$|\\￥|\\,", ""); // 处理包含, ￥
                                                                // 或者$的金额
        int index = currency.indexOf(".");
        int length = currency.length();
        Long amLong = 0l;
        if (index == -1) {
            amLong = Long.valueOf(currency + "00");
        } else if (length - index >= 3) {
            amLong = Long.valueOf((currency.substring(0, index + 3)).replace(".", ""));
        } else if (length - index == 2) {
            amLong = Long.valueOf((currency.substring(0, index + 2)).replace(".", "") + 0);
        } else {
            amLong = Long.valueOf((currency.substring(0, index + 1)).replace(".", "") + "00");
        }
        return amLong.toString();
    }
}  