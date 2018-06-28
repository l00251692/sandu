var QQMapWX = require('../../utils/qqmap-wx-jssdk.min.js');
var qqmapsdk;
qqmapsdk = new QQMapWX({
  key:'FPOBZ-UT2K2-ZFYUC-CX67E-IOOYS-7XFQ6'
});
import {
  getOrderInfo, setRecvOrder
} from '../../utils/api'

import { alert, makePhoneCall, datetimeFormat, getPrevPage, connectWebsocket } from '../../utils/util'

const app = getApp();
Page({
  data: {
    scale: 14,
    hiddenLoading:false
  },
  onLoad: function (options) {
    this.id = options.id
    this.callback = options.callback || 'callback'

    // var websocketFlag = wx.getStorageSync('websocketFlag')
    // if (!websocketFlag) {
    //   var { user_id, user_token } = getApp().globalData.loginInfo.userInfo

    //   connectWebsocket({
    //     user_id,
    //     success(data) { },
    //     error() {
    //     }
    //   })
    // }

    this.loadData()
  },

  onShow(){
    this.mapCtx = wx.createMapContext("didiMap");
    this.movetoPosition();
    this.initConnectWebSocket()
  },

  onReady(){

  },
  movetoPosition: function(){
    this.mapCtx.moveToLocation();
  },

  initConnectWebSocket() {
    var that = this

    var orderId = this.id

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
        if (tmp.msg == "取消订单") {
          wx.showModal({
            title: '订单被用户取消',
            content: '在行程开始3min内用户可以取消订单',
            showCancel:false,
            success: function (res) {
              wx.redirectTo({
                url: '/pages/service/service?registType=2',
              })
            }
          })
          that.loadData()
        }
      }
    })
  },

  loadData(){
    var that = this
    var order_id = this.id

    getOrderInfo({
      order_id,
      success(data) {

        var order = data.order

        order.createTime = datetimeFormat(order.createTime)
        that.setData({
          markers: [{
            iconPath: "../../images/assets/str.png",
            id: 0,
            latitude: data.order.strLatitude,
            longitude: data.order.strLongitude,
            width: 30,
            height: 30
          }, {
            iconPath: "../../images/assets/end.png",
            id: 0,
            latitude: data.order.endLatitude,
            longitude: data.order.endLongitude,
            width: 30,
            height: 30
          }],
          polyline: [{
            points: [{
              longitude: data.order.strLongitude,
              latitude: data.order.strLatitude
            }, {
              longitude: data.order.endLongitude,
              latitude: data.order.endLatitude
            }],
            color: "#FF0000DD",
            width: 4,
            dottedLine: true
          }],
          passenger: data.passenger,
          order: order,
          hiddenLoading: true
        });
      },
      error(data) {
        alert("查看详细信息失败，请联系客服")
      }
    })
  },

  onScaleSub(e){
    if (this.data.scale > 0)
    {
      this.setData({
        scale: --this.data.scale
      })
    }

  },

  onScalePlus(e) {
    if (this.data.scale < 18)
    {
      this.setData({
        scale: ++this.data.scale
      })
    }
  },

  onRevert(e){
    this.mapCtx.moveToLocation();
  },

  bindregionchange: (e) => {

  },
  //点击merkers
  bindmarkertap(e) {
    // console.log(e.markerId)
    // wx.showActionSheet({
    //   itemList: ["A"],
    //   success: function (res) {
    //     console.log(res.tapIndex)
    //   },
    //   fail: function (res) {
    //     console.log(res.errMsg)
    //   }
    // })
  },

  onPhoneTap(e) {
    makePhoneCall(this.data.passenger.phone)
  },

  onMsgTap(e) {
    var { passenger } = this.data

    var that = this

    wx.navigateTo({
      url: '../chat/chat?fromid=' + passenger.passengerId
    })
  },

  toRecv(){
    var order_id = this.id
    var that = this

    setRecvOrder({
      order_id,
      success(data) {

        wx.showToast({
          title: '接单成功',
        })

        that.setData({
          hiddenLoading: false
        })
        getPrevPage()[that.callback]()
        that.loadData()
      },
      error() {
        that.setData({
          hiddenLoading: true,
        })

        wx.showToast({
          title: '接单失败，请联系客服处理',
        })
      }
    })

  },
  toApp(){
    wx.showToast({
      title: '暂不支持',
      icon: 'success',
      duration: 1000
    })
  },
  toEvaluation(){
    var callback = this.callback
    wx.redirectTo({
      url: "/pages/evaluation/evaluationS?callback=" + callback + "&&id=" + this.id,
    })
  },
  onReady: function () {
    wx.getLocation({
      type: "gcj02",
      success:(res)=>{
        this.setData({
          longitude:res.longitude,
          latitude: res.latitude
        })
      }
      })

  },



})