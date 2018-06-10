//import util from '../../utils/index';
var QQMapWX = require('../../utils/qqmap-wx-jssdk.min.js');
var qqmapsdk;
qqmapsdk = new QQMapWX({
  key:'FPOBZ-UT2K2-ZFYUC-CX67E-IOOYS-7XFQ6'
});

import {
  alert,
  getCurrentAddress, reverseGeocoder,
  getPrevPage
} from '../../utils/util'

import { getMineInfo, addOrder } from '../../utils/api'

const app = getApp()
Page({
    data: {
        currentTab: 1,
        currentCost: 0,
        cart: '快车',
        navScrollLeft: 0,
        duration: 1000,
        interval: 5000,
        isLoading: true,
        color:"#cccccc",
        callCart: true,
        distance: 0,
        destination: '',
        bluraddress : '',
        index: '',
        imgUrls: [
          '../../images/assets/swiper-1.png',
          "../../images/assets/swiper-2.png"
        ]
    },
    onLoad: function(options) {
       var that = this;
       this.requestCart();
       this.requestWaitingtime();
       app.getCurrentAddress(function(address){
         console.log(JSON.stringify(address))
         if (address.addr_id) {
           address['title'] = `${address.addr} ${address.detail}`
         }
         that.setData({
           address: address['title'],
           address_detail:address
         })
       });
    },
    requestCart(e){
        // util.request({
        //     url: 'https://www.easy-mock.com/mock/5aded45053796b38dd26e970/comments#!method=get',
        //     mock: false,
        //   }).then((res)=>{
       
        //     const navData = res.data.navData;
        //     const imgUrls = res.data.imgUrls;
        //     const cost = res.data.cost
        //     this.setData({
        //         navData,
        //         imgUrls,
        //         cost
        //     })
        //   })
    },
    onShow(){
    },
    requestWaitingtime(){
        // setTimeout(() => {
        //     util.request({
        //         url: 'https://www.easy-mock.com/mock/5aded45053796b38dd26e970/comments#!method=get',
        //         mock: false,
        //         data: {
        //         }
        //       }).then((res)=>{
        //       const arr = res.data.waitingTimes;
        //     //   console.log(arr)
        //         var index = Math.floor((Math.random()*arr.length));
        //         // console.log(arr[index])
        //         this.setData({
        //         isLoading:false,
        //         waitingTimes: arr[index]
        //         })
        //       })
        // }, 1000);
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
                 address:title,
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
                destination:title
              })
            }
          })
        },
      })
    },
   
    toDaSan(e){
      const destination =this.data.destination
      const address_detail = this.data.address_detail
      const destination_detail = this.data.destination_detail
      if(destination ==''){
        wx.showModal({
           title: '目的地不能为空',
            content: '请填写或选择正确的目的地',
          })
      }else{
        //注意此接口的from和to的形式是不一样的，to是数组
        qqmapsdk.calculateDistance({
          mode: 'walking',
          from: address_detail.location,
          to:[{
            latitude: destination_detail.location.latitude,
            longitude: destination_detail.location.longitude
          }],
          success: (res)=> {
            // console.log(res.result.elements[0].distance)
            // var num1 = 8+1.9*(res.result.elements[0].distance/1000)
            // var num2= 12+1.8*(res.result.elements[0].distance/1000)
            // var num3= 16+2.9*(res.result.elements[0].distance/1000)
            // var play1 = num1.toFixed(1)
            // var play2 = num2.toFixed(1)
            // var play3 = num3.toFixed(1)
            if (res.result.elements[0].distance != -1)
            {
              this.setData({
                distance: res.result.elements[0].distance / 1000,
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
  toWait(e){
    var that = this
    var address_detail  = this.data.address_detail
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
                  console.log("getMineInfo :")
                  console.log(data)
                  if (data.phone != null && data.phone.length > 0) {
                    //提交订单到后台
                    addOrder({
                      from_add: address_detail.title,
                      from_add_detail: address_detail.address,
                      from_add_longitude: address_detail.location.longitude,
                      from_add_latitude: address_detail.location.latitude,
                      to_add: destination_detail.title,
                      to_add_detail: destination_detail.address,
                      to_add_longitude: destination_detail.location.longitude,
                      to_add_latitude: destination_detail.location.latitude,
                      time:"now",
                      success(data){
                        wx.reLaunch({
                          url: "/pages/wait/wait?orderId=" + data.orderId,
                        }),
                          wx.setTopBarText({
                            text: '等待接单'
                          })
                      },
                      error(data){
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
        else{
          alert('请在"我的"页面授权登录后使用')
        }
      }
    })
  },
    switchNav(event){
     
        this.requestWaitingtime();
       const cart = event.currentTarget.dataset.name
        let text = this.data.navData;
        this.setData({
            cart,
            isLoading:true,
            waitingTimes: ''
        })
        var cur = event.currentTarget.dataset.current; 
        var singleNavWidth = this.data.windowWindth/6;
        
        this.setData({
            navScrollLeft: (cur - 1) * singleNavWidth,
            currentTab: cur,
        })      
    },
    switchCart(e){
        const id = e.currentTarget.dataset.index;
        this.setData({
          index:id,
          
        })
       
    },
    switchTab(event){
        var cur = event.detail.current;
        var singleNavWidth =55;
        this.setData({
            currentTab: cur,
            navScrollLeft: (cur - 1) * singleNavWidth
        });
    },
    showUser(){
    // 如果全局未存手机号进入登录页
    if(app.globalData.userInfo && app.globalData.userInfo.phone){
        return
    }else{
        wx.navigateTo({
        url:  "/pages/login/login",
        })
    }
    },
    onChange(e){
        const currentCost = e.target.dataset.index;
        this.setData({
            currentCost
        })
      
    }
})