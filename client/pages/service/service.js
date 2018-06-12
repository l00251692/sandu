// pages/order/list.js
import {
  getOrders, setRecvOrder, getDistanceOrders
} from '../../utils/api'

import {
  datetimeFormat, requestPayment, alert
} from '../../utils/util'

import { host } from '../../config'

var initData = {
  websocketFlag:false,
  status:0,
  page: 0,
  hasMore: true,
  loading: false,
  list: null
}

Page({
  data: {
  },
  onLoad: function (options) {
    // 页面初始化 options为页面跳转所带来的参数
    this.registRcv = options.registRcv
    this.initData()
  },
  onReady: function () {
    // 页面渲染完成
  },
  onShow: function () {
    // 页面显示
  },
  onHide: function () {
    // 页面隐藏
  },
  onUnload: function () {
    // 页面关闭
    this.setData({
      websocketFlag: false
    })
    wx.closeSocket()
  },
  initData(cb) {
    this.setData(initData)
    this.loadData(cb)
    //this.connectWebsocket()
  },
  loadData(cb) {
    var that = this
    var {
      loading, page
    } = this.data
    if (loading) {
      return
    }

    this.setData({
      loading: true
    })

    var registRcv = this.registRcv

    if (registRcv)
    {
      getApp().getCurrentAddress(address => {
        var { title, location, city, district } = address
        console.log("getCurrentAddress:" + JSON.stringify(address))
        //获取最新的需求单信息，最多10条
        getDistanceOrders({
          page,
          city_name:city,
          district_name:district,
          my_longitude: location.longitude,
          my_latitude: location.latitude,
          success(data) {
            var { list } = data
            console.log("orderlist:" + JSON.stringify(data))
            if(list != null)
            {
              list = list.map(item => {
                item['depart_time_format'] = datetimeFormat(item.depart_time)
                return item
              })
            }

            that.setData({
              loading: false,
              address_title: title,
              list:list,
              hasMore: true, //修改为需要手工刷新
              page: 0
            })
          }
        })
      })
    }   
  },

  connectWebsocket: function () {
    var that = this
    var websocketFlag  = wx.getStorageSync('websocketFlag')

    console.log(" begin wx.connectSocket")
    var { user_id, user_token } = getApp().globalData.loginInfo.userInfo

    if (websocketFlag)
    {
      console.log("socket 已连接")
      that.setData({
        tip: '您已登录商家系统，请保持小程序不要关闭'
      })
    }
    else
    {
      wx.connectSocket({
        url: `wss://${host}/webSocketServer?x=` + user_id,
        data: {
          y: '',
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
        tip:'您已登录商家系统，请保持小程序不要关闭'
      })
    })
    wx.onSocketError(function (res) {
      console.log('WebSocket连接打开失败，请检查！')
      wx.setStorageSync('websocketFlag', false)
    })
    
    wx.onSocketMessage(function (res) {
      console.log("收到socket 信息")
      if (res.data == '连接成功')
      {
         console.log('连接成功')
      }
      else if (res.data == '新订单')
      {
          wx.playBackgroundAudio({
            //播放地址
            dataUrl: 'https://img.ailogic.xin/order.mp3',
            title: '小蓝鲸',
            coverImgUrl: ''
          })
          that.setData({
            tip: '您有新的订单，请及时处理'
          })
          that.loadData()        
      }
      
    })
  },

  onRecvOrderTap(e) {
    var { id } = e.currentTarget
    var that = this
    var { list, loading } = this.data
    if (loading) {
      return;
    }

    this.setData({
      loading: true
    })
    var { order_id } = list[id]
    setRecvOrder({
      order_id,
      success(data) {
        that.setData({
          loading: false,
        })

        wx.showToast({
          title: '接单成功',
        })
        wx.navigateTo({
          url: '/pages/order/orderService?id=' + order_id,
        })
      },
      error() {
        that.setData({
          loading: false,
        })

        wx.showToast({
          title: '接单失败，请联系客服处理',
        })
      }
    })
  },

  onRejectOrderTap(e) {
    var { id } = e.currentTarget
    var that = this
    var { list, loading } = this.data
    if (loading) {
      return;
    }

    this.setData({
      loading: true
    })
    var { order_id } = list[id]
    setRejectOrder({
      order_id,
      success(data) {
        wx.stopBackgroundAudio()
        wx.showToast({
          title: '订单已拒绝',
        })
        that.initData();
        that.setData({
          loading: false,
          tip: '您已登录商家系统，请保持小程序不要关闭'
        })
      },
      error() {
        that.setData({
          loading: false,
          tip: '订单未成功拒绝，请联系客服'
        })
      }
    })
  },
  onReachBottom(e) {
    var {
      hasMore, loading
    } = this.data
    if (getApp().globalData.loginInfo.is_login && hasMore && !loading) {
      this.loadData()
    }
  },
  onPullDownRefresh() {
    console.log("onPullDownRefresh")
    if (getApp().globalData.loginInfo.is_login) {
      wx.showNavigationBarLoading()
      this.initData(() => {
        wx.hideNavigationBarLoading()
        wx.stopPullDownRefresh()
      })
    } else {
      wx.stopPullDownRefresh()
    }
  },

  callback(loginInfo) {
    if (this.data.list) {
      this.onLoad()
    }
  },
  onShareAppMessage() {
    return {
      title: '我的订单',
      path: '/pages/order/list'
    }
  }
})