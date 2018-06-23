
import { host } from '../../config'
import {
  confirm, alert, getPrevPage
} from '../../utils/util'
import {
  cancelOrder
} from '../../utils/api'

const app = getApp()
Page({
  data: {
  progress_txt: '已等待', 
  count:0, 
  waitTimer: null,
  time: '00:00',
  websocketFlag:false,
  },
  onLoad: function (options) {
    this.orderId = options.orderId
    this.callback = options.callback || 'callback'
  },

  onShow: function () {
    this.setData({
      address: app.globalData.currentAddress.title,
    })
  },

  onReady: function () {
    this.drawProgressbg();
    this.connectWebsocket();
    this.countInterval();
    this.drawProgress();
  },

  onUnload: function () {
    // 页面关闭
    var that = this
    this.setData({
      websocketFlag: false
    })
    wx.closeSocket()
    getPrevPage()[that.callback]()
  },

  parseTime: function(time){
    var time = time.toString();
      return time[1]?time:'0'+time;
  },

  connectWebsocket: function () {
    var that = this
    var websocketFlag = this.data.websocketFlag

    console.log(" begin wx.connectSocket")
    var { user_id, user_token } = getApp().globalData.loginInfo.userInfo

    if (websocketFlag) {
      console.log("socket 已连接")
    }
    else {
      wx.connectSocket({
        url: `wss://${host}/webSocketServer?x=` + user_id + `&y=wait`,
        data: {
          z: '',
        },
        header: {
          'content-type': 'application/x-www-form-urlencoded'
        },
        method: "GET"
      })
    }


    wx.onSocketOpen(function (res) {
      console.log('WebSocket连接已打开！')
      wx.setStorageSync('websocketFlag', true)
      that.setData({
        websocketFlag: true
      })
    })
    wx.onSocketError(function (res) {
      console.log('WebSocket连接打开失败，请检查！')
      wx.setStorageSync('websocketFlag', false)
    })

    wx.onSocketMessage(function (res) {
      console.log("收到socket 信息")
      if (res.data == '连接成功') {
        console.log('连接成功')
      }
      else if (res.data == '订单已被接') {
        that.setData({
          progress_txt: "匹配成功"
        });
        wx.redirectTo({
          url: "/pages/orderService/orderService",
        });
        clearInterval(that.countTimer);
      }

    })
  },

  countInterval: function () {
    var curr = 0;
    var timer = new Date(0,0);
    var  randomTime = Math.floor(20*Math.random()) ;
    //1s为周期循环调用处理查看是否有人接单
    this.countTimer = setInterval(() => {
      
        this.setData({
              time: this.parseTime(timer.getMinutes())+":"+this.parseTime(timer.getSeconds()),
        });
        timer.setMinutes(curr/60);
        timer.setSeconds(curr%60);
        curr++;
        this.drawProgress(this.data.count / (60/2))
        this.data.count++;
        
      }, 1000)
  },
  drawProgressbg: function(){
   var ctx = wx.createCanvasContext('canvasProgressbg');
   ctx.setLineWidth(4);
   ctx.setStrokeStyle("#e5e5e5");
   ctx.setLineCap("round");
   ctx.beginPath();
   ctx.arc(110,110,100,0,2*Math.PI,false);
   ctx.stroke();
   ctx.draw();
  },
  
  drawProgress: function (step){ 
    var context = wx.createCanvasContext('canvasProgress'); 
    context.setLineWidth(4);
    context.setStrokeStyle("#fbcb02");
    context.setLineCap('round')
    context.beginPath();
      // 参数step 为绘制的圆环周长，从0到2为一周 。 -Math.PI / 2 将起始角设在12点钟位置 ，结束角 通过改变 step 的值确定
    context.arc(110, 110, 100, -Math.PI /2, step*Math.PI /2-Math.PI /2, false);
    context.stroke();
    context.draw()
  },
  toCancel(){
    var order_id = this.orderId
    var that = this
    
    confirm({
      content: `是否取消订单`,
      confirmText: '取消订单',
      ok() {
        cancelOrder({
          order_id,
          success(data) {
            getPrevPage()[that.callback]()
            wx.switchTab({
              url: "/pages/index/index",
            })
          }
        })
      }
    })
   
  },
  backIndex(){
    wx.switchTab({
      url:  "/pages/index/index",
    })
  } 
 

})