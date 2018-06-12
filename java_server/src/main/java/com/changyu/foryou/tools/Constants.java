package com.changyu.foryou.tools;

public  class Constants {
	final static public String STATUS = "status";
	final static public String SUCCESS = "success";
	final static public String FAILURE = "failure";
	public static final String MESSAGE = "message";
	//public static final String localIp = "http://www.enjoyfu.com.cn:7777/ForyouImage";
	public static final String localIp = "http://localhost/JiMuImage"; //存放上传的图片的服务器JiMuImage为上传图片时创建的目录
    public static String appId="app_La1y14yrPa10SeHS";
    public static String apiKey="sk_live_vBNcIdIOKPBJEU9YOq3C02PU";
    
    public static final String REDISPREFIX = "orderId=";
    public static final int COUNTDELAY = 10; //服务器返回给前台的剩余时间增加10s防止前台时间计数器到了后后端还未及时更细数据

    public static final int WAIT_RCVTIME = 60;//300;  //300 商家接单时间
    public static final int WAIT_FINISH = 60;//24*60*60; //24小时自动确认收货 24*60*60
    public static final int WAIT_CANCEL  = 60;//2*24*60*60; //2天自动评价为5星  2*24*60*60
    
    public static final Long REDISSAVETIME =  365*24*60l;   //缓存一年，分钟
    
    public static short orderNow=0;
    public static short orderLater=0;
    
    public static short orderStatusCreate=0;
    public static short orderStatusCancel=1;
    public static short orderStatusRecv=2;
    public static short orderStatusFinish=3;
	
}
