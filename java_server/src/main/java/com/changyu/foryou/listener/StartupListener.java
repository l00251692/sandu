package com.changyu.foryou.listener;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.changyu.foryou.controller.WebSocketPushHandler;
import com.changyu.foryou.model.DSHOrder;
import com.changyu.foryou.model.Order;
import com.changyu.foryou.service.DelayService;
import com.changyu.foryou.service.OrderService;
import com.changyu.foryou.service.PayService;
import com.changyu.foryou.service.DelayService.OnDelayedListener;
import com.changyu.foryou.service.RedisService;
import com.changyu.foryou.tools.Constants;
import com.changyu.foryou.tools.ThreadPoolUtil;


@Service  
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {  
      
    private static final Logger log = Logger.getLogger(StartupListener.class);  
  
    @Autowired  
    DelayService delayService;  
    @Autowired  
    RedisService redisService;
    @Autowired
    OrderService orderService;   
    
    @Resource  
    private WebSocketPushHandler webSocketHandler;  
 
      
      
    @Override  
    public void onApplicationEvent(ContextRefreshedEvent evt) {
        log.info(">>>>>>>>>>>>系统启动完成，onApplicationEvent()");  
 
        delayService.start(new OnDelayedListener(){
            @Override  
            public void onDelayedArrived(final DSHOrder order) {  
                //异步来做  
                ThreadPoolUtil.execute(new Runnable(){  
                    public void run(){  
                        String orderId = order.getOrderId();
                        System.out.println("onDelayedArrived:orderId=" + String.valueOf(orderId));
                        short status = order.getStatus();
                        
                        System.out.println("order=" + order.toString());
						if (status == Constants.orderStatusCreate) {
							Map<String, Object> paramMap = new HashMap<String, Object>();
							paramMap.put("orderId",orderId);
							Order order = orderService.getOrderByIdWx(paramMap);
							if(order == null)
							{
								//从redis删除  
		                        redisService.delete(Constants.REDISPREFIX + orderId);  
		                        log.info("订单自动处理，删除redis："+orderId);
		                        return;
							}
							paramMap.put("orderStatus",Constants.orderStatusCancel);
							JSONArray recordes = JSON.parseArray(order.getRecords());
							JSONObject record = new JSONObject();
							record.put("status",Constants.orderStatusCancel);
							record.put("time", new Date());
							recordes.add(record);
							paramMap.put("records",recordes.toString());
							int flag = orderService.updateOrderStatus(paramMap);
							if (flag != -1 && flag != 0)
							{
								//发送消息通知用户订单已取消
								JSONObject to = new JSONObject();
								to.put("type", "orderMsg");
								to.put("orderId", orderId);
								to.put("msg", "取消订单");
								to.put("passengerId", order.getCreateUser());

								TextMessage toMsg = new TextMessage(to.toString());
								webSocketHandler.sendMessagesToUsers(toMsg);
							}
						} 
						redisService.delete(Constants.REDISPREFIX + orderId);  
                        log.info("订单自动处理，删除redis："+orderId);
                        return;
                    }  
                });  
            }  
        });  
        
        
        
        //查找需要入队的订单  
        ThreadPoolUtil.execute(new Runnable(){
            @Override  
            public void run() {  
                log.info("查找需要入队的订单");  
                //扫描redis，找到所有可能的orderId  
                List<String> keys = redisService.scan(Constants.REDISPREFIX, 10000);
                if(keys == null || keys.size() <= 0){  
                    return;  
                }  
                log.info("需要入队的订单keys："+keys);  
                //写到DelayQueue  
                for(String key : keys){  
                    DSHOrder order = redisService.get(key);  
                    log.info("读redis，key："+key);  
                    if(order != null){  
                        delayService.add(order);      
                        log.info("订单自动入队："+order.getOrderId());  
                    }  
                }   
            }  
        });   
    }  
}