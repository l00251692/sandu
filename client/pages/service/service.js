// pages/order/list.js
import {
  getOrders, setRecvOrder, getRegSanInfo, getDistanceOrders, getMineProcessingOrderDriver
} from '../../utils/api'

import {
  datetimeFormat, requestPayment, alert, connectWebsocket
} from '../../utils/util'

import { host } from '../../config'

var initData = {
  websocketFlag:false,
  status:0,
  page: 0,
  hasMore: true,
  loading: false,
  list: null,

  text: "您有订单正在进行中，请处理",
  marqueePace: 1,//滚动速度
  marqueeDistance: 0,//初始滚动距离
  marquee_margin: 200,
  size: 16, //与css中定义的字体大小一致，单位为px
  rollinterval: 20, // 时间间隔
  windowWidth: 686, //显示空间宽度，与css中定义一致
  orderingId: null
}

Page({
  data: {
  },
  onLoad: function (options) {
    // 页面初始化 options为页面跳转所带来的参数
    this.registType = options.registType

    this.setData({
      registType: this.registType
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
    // this.setData({
    //   websocketFlag: false
    // })
    // wx.closeSocket()
  },
  initData(cb) {
    this.setData(initData)
    this.init()
    this.loadData(cb)
    //this.connectWebsocket() 
  },

  initConnectWebSocket() {
    var that = this

    wx.onSocketOpen(function (res) {
      wx.setStorageSync('websocketFlag', true)
    })

    wx.onSocketError(function (res) {
      wx.setStorageSync('websocketFlag', false)
    })

    wx.onSocketMessage(function (res) {
      var tmp = JSON.parse(res.data)
      if (tmp.type == "orderMsg") { //有新订单或订单被接就会刷新
        that.initData()
      }
    })
  },

  init() {
    var that = this

    getMineProcessingOrderDriver({
      success(data) {
        if (data.orderId != null && data.orderId.length > 0) {
          var length = that.data.text.length * that.data.size;//文字长度
          //var windowWidth = wx.getSystemInfoSync().windowWidth;// 屏幕宽度

          that.setData({
            length: length,
            orderingId: data.orderId,
            orderingUserType: data.userType,
            orderingStatus: data.orderStatus
          });
          //that.scrolltxt();// 第一个字消失后立即从右边出现
        }
        else{
          that.setData({
            text: "下拉刷新查看附近最新订单",
          })
        }
      }
    })
            
  },

  loadData(cb) {
    var that = this
    var {
      loading, page
    } = this.data

    if (loading) {
      cb && cb()
      return
    }

    this.setData({
      loading: true
    })

    var registType = this.registType

    if (registType == 2)
    {
      getApp().getCurrentAddress(address => {
        var { title, location, city, district } = address

        getRegSanInfo({
          success(data){
            that.setData({
              san_color:data.color,
              san_style: data.style,
              san_feature: data.feature
            })

            //获取最新的需求单信息，最多10条
            getDistanceOrders({
              page,
              city_name: city,
              district_name: district,
              my_longitude: location.longitude,
              my_latitude: location.latitude,
              success(data) {
                console.log("getDistanceOrders success")
                var { list } = data
                if (list != null && list.length > 0) {
                  list = list.map(item => {
                    item['depart_time_format'] = datetimeFormat(item.depart_time)
                    return item
                  })
                }

                that.setData({
                  loading: false,
                  address_title: title,
                  list: list,
                  hasMore: true, //修改为需要手工刷新
                  page: 0
                })
                cb && cb()
              },
              error(){
                cb && cb()
              }
            })

          }
        })
        
      })
    } 
    else{
      this.setData({
        loading: false
      })
    }  
  },

  scrolltxt: function () {
    var that = this;
    var length = that.data.length;//滚动文字的宽度
    var windowWidth = that.data.windowWidth;//屏幕宽度

    var interval = setInterval(function () {
      var maxscrollwidth = length + that.data.marquee_margin;//滚动的最大宽度，文字宽度+间距，如果需要一行文字滚完后再显示第二行可以修改marquee_margin值等于windowWidth即可
      var crentleft = that.data.marqueeDistance;
      if (crentleft < maxscrollwidth) {//判断是否滚动到最大宽度
        that.setData({
          marqueeDistance: crentleft + that.data.marqueePace
        })
      }
      else {
        //console.log("替换");
        that.setData({
          marqueeDistance: 0 // 直接重新滚动
        });
        clearInterval(interval);
        that.scrolltxt();
      }
    }, that.data.rollinterval); 

  },

  connectWebsocket: function () {
    var that = this
    var websocketFlag  = wx.getStorageSync('websocketFlag')

    var { user_id, user_token } = getApp().globalData.loginInfo.userInfo
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
    
  
    wx.onSocketOpen(function (res) {
      wx.setStorageSync('websocketFlag', true)
    })
    wx.onSocketError(function (res) {
      wx.setStorageSync('websocketFlag', false)
    })
    
    wx.onSocketMessage(function (res) {
   
    })
  },

  onRecvOrderTap(e) {
    var { id } = e.currentTarget
    var that = this
    var { list, loading } = this.data
    if (loading) {
      return;
    }

    if (this.data.orderingId != null) {
      wx.showModal({
        title: '您当前有订单在进行中',
        content: '请先处理当前进行中的订单',
      })
      return
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
        that.initData()
        wx.navigateTo({
          url: '/pages/order/orderService?callback=callback&&id=' + order_id,
        })
      },
      error(data) {
        that.setData({
          loading: false,
        })

        that.initData()
      }
    })
  },

  onProcessOrder(e) {

    var { orderingId, orderingUserType, orderingStatus } = this.data

    if (orderingId != null && orderingId.length > 0) {
      wx.navigateTo({
        url: "/pages/order/orderService?callback=callback&&id=" + orderingId,
      })
    }
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
        console.log("stopPullDownRefresh")
      })
    } else {
      wx.stopPullDownRefresh()
    }
  },

  callback() {
    this.initData()
  }
})