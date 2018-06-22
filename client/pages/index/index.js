//import util from '../../utils/index';
var QQMapWX = require('../../utils/qqmap-wx-jssdk.min.js');
var qqmapsdk;
qqmapsdk = new QQMapWX({
  key: 'FPOBZ-UT2K2-ZFYUC-CX67E-IOOYS-7XFQ6'
});

import {
  alert,
  getCurrentAddress, reverseGeocoder,
  getPrevPage
} from '../../utils/util'

import { getMineInfo, addOrder, getMineProcessingOrder } from '../../utils/api'

const app = getApp()
Page({
  data: {
    address:"我的位置",
    // currentTab: 1,
    // currentCost: 0,
    // cart: '快车',
    navScrollLeft: 0,
    duration: 1000,
    interval: 5000,
    isLoading: true,
    color: "#cccccc",
    callCart: true,
    distance: 0,
    destination: '',
    bluraddress: '',
    index: '',
    imgUrls: [
      '../../images/assets/swiper-1.png',
      "../../images/assets/swiper-2.png"
    ]
  },
  onLoad: function (options) {
    var that = this;

    app.getCurrentAddress(function (address) {
      if (address.addr_id) {
        address['title'] = `${address.addr} ${address.detail}`
      }
      that.setData({
        address: address['title'],
        address_detail: address
      })
    });

  },

  onShow() {
    this.initData();
    this.init();
  },

  initData() {
    this.setData({
      text: "您有订单正在进行中，请处理",
      marqueePace: 1,//滚动速度
      marqueeDistance: 0,//初始滚动距离
      marquee_margin: 200,
      size: 16, //与css中定义的字体大小一致，单位为px
      rollinterval: 20, // 时间间隔
      windowWidth: 686, //显示空间宽度，与css中定义一致
      orderingId: null,
      callCart:true
    })
  },

  init() {
    var that = this

    wx.getSetting({
      success: (res) => {
        if (res.authSetting['scope.userInfo']) {
          getApp().getLoginInfo(loginInfo => {
            if (loginInfo != null && loginInfo.is_login) {

              getMineProcessingOrder({
                success(data) {
                  if (data.orderId != null && data.orderId.length > 0) {
                    var length = that.data.text.length * that.data.size;//文字长度
                    //var windowWidth = wx.getSystemInfoSync().windowWidth;// 屏幕宽度

                    that.setData({
                      length: length,
                      orderingId: data.orderId,
                      orderingStatus: data.orderStatus
                    });
                    //that.scrolltxt();// 第一个字消失后立即从右边出现
                  }
                }
              })
            }
          })
        }
      }
    })
  },

  scrolltxt: function () {
    var that = this;
    var length = that.data.length;//滚动文字的宽度
    var windowWidth = that.data.windowWidth;//屏幕宽度
    // if (length > windowWidth) {
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
    // }
    // else {
    //   that.setData({ marquee_margin: "1000" });//只显示一条不滚动右边间距加大，防止重复显
    // }
  },


  onChooseFromLocation(e) {
    var that = this
    wx.chooseLocation({
      success: function (res) {
        var {
          name: title, address,
          longitude, latitude
          } = res
        var location = {
          longitude, latitude
        }
        reverseGeocoder({
          location,
          success(data) {
            console.log(data)
            that.setData({
              address_detail: Object.assign({
                title, address, location
              }, data),
              address: title,
            })
          }
        })
      },
    })
  },

  onChooseDestLocation(e) {
    var that = this

    wx.chooseLocation({
      success: function (res) {
        var {
          name: title, address,
          longitude, latitude
        } = res
        var location = {
          longitude, latitude
        }
        reverseGeocoder({
          location,
          success(data) {
            that.setData({
              destination_detail: Object.assign({
                title, address, location
              }, data),
              destination: title
            })
          }
        })
      },
    })
  },

  toDaSan(e) {
    const destination = this.data.destination
    const address_detail = this.data.address_detail
    const destination_detail = this.data.destination_detail
    if (destination == '') {
      wx.showModal({
        title: '目的地不能为空',
        content: '请填写或选择正确的目的地',
      })
    }
    else if (this.data.orderingId != null) {
      wx.showModal({
        title: '您当前有订单在进行中',
        content: '请先处理当前进行中的订单',
      })
    } else {
      //注意此接口的from和to的形式是不一样的，to是数组
      qqmapsdk.calculateDistance({
        mode: 'walking',
        from: address_detail.location,
        to: [{
          latitude: destination_detail.location.latitude,
          longitude: destination_detail.location.longitude
        }],
        success: (res) => {
          if (res.result.elements[0].distance != -1) {
            this.setData({
              distance: (res.result.elements[0].distance / 1000).toFixed(2),
            })
          }
        },
        fail: function (res) {
          console.log(res);
        }
      });
      this.setData({
        callCart: false
      })
    }
  },

  toWait(e) {
    var that = this
    var address_detail = this.data.address_detail
    var destination_detail = this.data.destination_detail
    //用户授权则登录，否则等用户点击授权
    wx.getSetting({
      success: (res) => {
        if (res.authSetting['scope.userInfo']) {
          getApp().getLoginInfo(loginInfo => {
            if (loginInfo != null && loginInfo.is_login) {
              that.setData({
                loginInfo: loginInfo,
                userInfo: loginInfo.userInfo
              })

              getMineInfo({
                success(data) {
                  if (data.phone != null && data.phone.length > 0) {
                    //提交订单到后台
                    console.log("addOrder dest=" + destination_detail.title)
                    addOrder({
                      city_name: address_detail.city,
                      district_name: address_detail.district,
                      from_add: address_detail.title.replace('(', '[').replace(')', ']'),//对于括号会乱码暂时替换方法解决
                      from_add_detail: address_detail.address,
                      from_add_longitude: address_detail.location.longitude,
                      from_add_latitude: address_detail.location.latitude,
                      to_add: destination_detail.title.replace('(', '[').replace(')', ']'),
                      to_add_detail: destination_detail.address,
                      to_add_longitude: destination_detail.location.longitude,
                      to_add_latitude: destination_detail.location.latitude,
                      time: "now",
                      success(data) {
                        wx.navigateTo({
                          url: "/pages/wait/wait?orderId=" + data.orderId,
                        }),
                          wx.setTopBarText({
                            text: '等待接单'
                          })
                      },
                      error(data) {
                        alert("系统繁忙，请稍侯")
                      }
                    })
                  }
                  else {
                    alert('请在"我的"页面绑定手机号')
                  }
                }
              })
            }
          })
        }
        else {
          alert('请在"我的"页面授权登录后使用')
        }
      }
    })
  },

  onProcessOrder(e) {

    var { orderingId, orderingStatus } = this.data

    wx.navigateTo({
      url: "/pages/order/orderPassenger?id=" + orderingId,
    })
  }
})