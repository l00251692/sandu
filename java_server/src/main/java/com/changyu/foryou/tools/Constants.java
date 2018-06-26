package com.changyu.foryou.tools;

public  class Constants {
	final static public String STATUS = "status";
	final static public String SUCCESS = "success";
	final static public String FAILURE = "failure";
	public static final String MESSAGE = "message";
	//public static final String localIp = "http://www.enjoyfu.com.cn:7777/ForyouImage";
	public static final String localIp = "https://localhost/JiMuImage"; //存放上传的图片的服务器JiMuImage为上传图片时创建的目录
    public static String appId="wx7b7aabca0fc1737d";
    public static String apiKey="112642920678cc1d271d78d166417c9f";
    public static String mchId="1508584831";
    public static String mchKey="LiuJingTao2333KEY251692111111111";
    //public static String notifyUrl = "https://www.ailogic.xin/pay/payNotify";
    public static String notifyUrl = "https://127.0.0.1/pay/payNotify";
 
    public static String TemplateIdPaySuccess="vQoIDJ072tRjUpGDoh0GMb1qI8DzUPgBlrpOT_3p_9g";
    public static String TemplateIdPayFail="qA_2yDLB6hlTKzsv9dY-SsFIC6QJChjTiii-kR2f-ms";
    public static String TemplateIdPayCancel="qA_2yDLB6hlTKzsv9dY-SsFIC6QJChjTiii-kR2f-ms";
    
    public static String TemplateIdWithDrawSuccess="boU1BwC-VVuIEKkeSSAMdDAL93qvohZD7AryuM5G_ps";
    
    
    public static String QQMAPKEY = "NJIBZ-FDNLD-3754C-HYIBR-J3NV7-UIBHI";
    
    public static final String REFUND_KEY_PATH = "classpath:apiclient_cert.p12";
    public static final String CERTPATH = "classpath:apiclient_cert.p12";
    
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
    public static short orderStatusDone=4;
	
}
