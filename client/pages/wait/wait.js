
import { host } from '../../config'
import {
  confirm, alert, getPrevPage, connectWebsocket
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
    var start = options.start
    this.setData({
      address: start,
    })

    var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
    connectWebsocket({
      user_id,
      success(data) {

      },
      error() {

      }
    })  
    this.initConnectWebSocket()
  },

  onShow: function () {
  },

  onReady: function () {
    this.drawProgressbg();
    this.countInterval();
    this.drawProgress();
  },

  onUnload: function () {
    // 页面关闭
    var that = this
    getPrevPage()[that.callback]()
  },

  parseTime: function(time){
    var time = time.toString();
      return time[1]?time:'0'+time;
  },

  initConnectWebSocket() {
    var that = this
    var orderId = this.orderId
    console.log("initConnectWebSocket")
    wx.onSocketOpen(function (res) {
      console.log('WebSocket连接已打开！')
      wx.setStorageSync('websocketFlag', true)
    })

    wx.onSocketError(function (res) {
      console.log('WebSocket连接打开失败，请检查！')
      wx.setStorageSync('websocketFlag', false)
    })

    wx.onSocketMessage(function (res) {
      console.log('收到消息onSocketMessage！')
      var tmp = JSON.parse(res.data)
      var { user_id } = getApp().globalData.loginInfo.userInfo

      if (tmp.type == "orderMsg" && tmp.orderId == orderId) {
        if (tmp.msg == "订单被接")
        {
          wx.showToast({
            title: '订单已被接，请耐心等待',
            icon: 'success',
            duration: 2000,
          })

          wx.redirectTo({
            url: "/pages/order/orderPassenger?callback=callback&&id=" + tmp.orderId,
          })
        }
        else if (tmp.msg == "取消订单"){
          wx.showToast({
            title: '订单自动取消',
            icon: 'success',
            duration: 2000,
          })

          wx.switchTab({
            url: "/pages/index/index",
          })
        }
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