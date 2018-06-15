package com.changyu.foryou.controller;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.UUIDSerializer;
import com.changyu.foryou.service.DelayService;
import com.changyu.foryou.service.OrderService;
import com.changyu.foryou.service.PayService;
import com.changyu.foryou.service.RedisService;
import com.changyu.foryou.service.UserService;
import com.changyu.foryou.tools.Constants;
import com.changyu.foryou.tools.PayUtil;
import com.changyu.foryou.tools.StringUtil;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator;

@Controller
@RequestMapping("/pay")
public class PayController {
	
	@Autowired
	private OrderService orderService;
	@Autowired
    private UserService userService;
	@Autowired  
    private DelayService delayService;  
    @Autowired  
    private RedisService redisServie; 
    
    @Autowired
	private PayService payService;

    
    
    private static final Logger LOGGER = Logger.getLogger(PayController.class);
    
    /**
	 * 获得微信支付参数
	 * 
	 * @param phoneId
	 * @param foodId
	 * @param foodCount
	 * @param foodSpecial
	 * @return
	 */
	@RequestMapping("/getPaymentWx")
	public @ResponseBody Map<String, Object> getPaymentWx(@RequestParam String order_id,@RequestParam String user_id,@RequestParam String pay_money, HttpServletRequest request){
		//user_id就是openid
		Map<String,Object> data = new HashMap<String, Object>();
		JSONObject node = new JSONObject();;
		
		try{
            //生成的随机字符串
            String nonce_str = StringUtil.getRandomStringByLength(32);
            //商品名称  
            String body = "蜗牛";  
            //获取客户端的ip地址  
            String spbill_create_ip = StringUtil.getIpAddr(request);  
              
            //组装参数，用户生成统一下单接口的签名  
            Map<String, String> packageParams = new HashMap<String, String>(); 
            String appId = Constants.appId;
            String mchId = Constants.mchId;
            String mchKey = Constants.mchKey; //微信支付商户密钥
            String notifyUrl = Constants.notifyUrl; 
            
            packageParams.put("appid", appId);  
            packageParams.put("mch_id", mchId);//微信支付商家号
            packageParams.put("nonce_str", nonce_str);  
            packageParams.put("body", body);  
            packageParams.put("out_trade_no", order_id);//商户订单号  
            packageParams.put("total_fee", pay_money);//支付金额，这边需要转成字符串类型，否则后面的签名会失败  
            packageParams.put("spbill_create_ip", spbill_create_ip);//客户端IP  
            packageParams.put("notify_url", notifyUrl);//支付成功后的回调地址  
            packageParams.put("trade_type", "JSAPI");//支付方式  
            packageParams.put("openid", user_id);  
                 
            String prestr = PayUtil.createLinkString(packageParams); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串   
          
            //MD5运算生成签名，这里是第一次签名，用于调用统一下单接口  
            String mysign = PayUtil.sign(prestr, mchKey, "utf-8").toUpperCase();  //微信支付商户密钥为第二个参数
              
            //拼接统一下单接口使用的xml数据，要将上一步生成的签名一起拼接进去  
            String xml = "<xml>" + "<appid>" + appId + "</appid>"   
                    + "<body><![CDATA[" + body + "]]></body>"   
                    + "<mch_id>" + mchId + "</mch_id>"   
                    + "<nonce_str>" + nonce_str + "</nonce_str>"   
                    + "<notify_url>" + notifyUrl + "</notify_url>"   
                    + "<openid>" + user_id + "</openid>"   
                    + "<out_trade_no>" + order_id + "</out_trade_no>"   
                    + "<spbill_create_ip>" + spbill_create_ip + "</spbill_create_ip>"   
                    + "<total_fee>" + pay_money + "</total_fee>"  
                    + "<trade_type>" + "JSAPI" + "</trade_type>"   
                    + "<sign>" + mysign + "</sign>"  
                    + "</xml>";  
              
  
            //调用统一下单接口，并接受返回的结果  
            String result = PayUtil.httpRequest("https://api.mch.weixin.qq.com/pay/unifiedorder", "POST", xml);     
              
            // 将解析结果存储在HashMap中     
            Map map = PayUtil.doXMLParse(result);  
              
            String return_code = (String) map.get("return_code");//返回状态码  
            //TODO:微信商户申请下来后需要再调试
            //if(return_code=="SUCCESS"||return_code.equals(return_code)){  
        		node.put("signType", "MD5");
                String prepay_id = (String) map.get("prepay_id");//返回的预付单信息     
                node.put("nonceStr", nonce_str);  
                node.put("prepay_id", prepay_id);
                node.put("package", "prepay_id=11111");  
                Long timeStamp = System.currentTimeMillis() / 1000;     
                node.put("timeStamp", timeStamp + "");//这边要将返回的时间戳转化成字符串，不然小程序端调用wx.requestPayment方法会报签名错误  
                //拼接签名需要的参数  
                String stringSignTemp = "appId=" + appId + "&nonceStr=" + nonce_str + "&package=prepay_id=" + prepay_id+ "&signType=MD5&timeStamp=" + timeStamp;     
                //再次签名，这个签名用于小程序端调用wx.requesetPayment方法  
                String paySign = PayUtil.sign(stringSignTemp, mchKey, "utf-8").toUpperCase();  
                  
                node.put("paySign", paySign); 
                node.put("appid", appId);  
                
                data.put("State", "Success");
        		data.put("data", node);	
            //}  
        		
    		return data;
        }catch(Exception e)
		{  
            e.printStackTrace();  
        }  
        return null;	
	}
	
	/**
     * @return
	 * @throws Exception 
     */
	@RequestMapping("/ballanceBackWx")
    public @ResponseBody Map<String, Object> ballanceBackWx(@RequestParam String user_id,@RequestParam String money) throws Exception {
          Map<String,Object> result = new HashMap<String,Object>();
          
          String orderId = UUID.randomUUID().toString();
          
          boolean flag = PayUtil.enterprisePayment(user_id, orderId,"xxxdefined","100","提现","192.145.23.2");
          
          if(flag)
          {
        	  result.put("State", "Success");
              result.put("data", "提现成功");
          }
          else
          {
        	  result.put("State", "Fail");
              result.put("info", "提现失败，请联系客服");
          }
          
          return result;
    }

}
